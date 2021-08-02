package io.github.eatmyvenom.litematicin.utils;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;

public class InteractionUtils {
    
    /**
     * Check if an item (bucket) can be used at the given {@code BlockPos}.
     * @param pos
     * @param mc
     * @return
     */
    public static ViewResult canSeeAndInteractWithBlock(BlockPos pos, MinecraftClient mc) {
        Direction[] possibleDirections = Direction.values();
        
        for (int i = 0; i < possibleDirections.length; i++) {
            Vec3i vec = possibleDirections[i].getVector();
            BlockState state = mc.world.getBlockState(pos.add(vec));
            
            if (state.isAir() || state.contains(Properties.WATERLOGGED)) continue;
            
            ViewResult result = isVisible(mc, pos, possibleDirections[i]);
            
            if (result == ViewResult.VISIBLE) return result;
                
        }
        
        return ViewResult.INVISIBLE;
    }
    
    /**
     * Returns the needed yaw & pitch to look at a blockFace.
     * @param me
     * @param pos
     * @param blockFace
     * @return
     */
    private static Rotation getNeededRotation(PlayerEntity me, BlockPos pos, Direction blockFace)
    {
        Vec3d posD = Vec3d.ofCenter(pos);
        Vec3d to = posD.add(Vec3d.of(blockFace.getVector()).multiply(0.5d));
        
        double dirx = me.getX() - to.getX();
        double diry = me.getEyeY() - to.getY();
        double dirz = me.getZ() - to.getZ();

        double len = Math.sqrt(dirx*dirx + diry*diry + dirz*dirz);

        dirx /= len;
        diry /= len;
        dirz /= len;

        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);

        //to degree
        pitch = pitch * 180.0 / Math.PI;
        yaw = yaw * 180.0 / Math.PI;

        yaw += 90f;
        return new Rotation((float)yaw, (float)pitch, (float)len);
    }
    
    /**
     * Check if a blockFace at {@code BlockPos} is visible for the player.
     * @param player
     * @param toSee
     * @param blockFace
     * @return
     */
    private static ViewResult isVisible(MinecraftClient mc, BlockPos toSee, Direction blockFace) {
        Rotation rotation = getNeededRotation(mc.player, toSee, blockFace);
        
        float tickDelta = mc.getTickDelta();
        double maxDist = rotation.maxDist + 0.5f;
        
        Vec3d vec3d = mc.player.getCameraPosVec(tickDelta);
        Vec3d vec3d2 = getRotationVector(rotation.pitch, rotation.yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDist, vec3d2.y * maxDist, vec3d2.z * maxDist);
        HitResult result = mc.world.raycast(new RaycastContext(vec3d, vec3d3,
                RaycastContext.ShapeType.OUTLINE, 
                        RaycastContext.FluidHandling.ANY, mc.player));
        
        if (result.getType() == Type.BLOCK 
                && !(result.getPos().squaredDistanceTo(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ()) < rotation.maxDist * rotation.maxDist) // If there's a block between the player and the location
                && !mc.world.getBlockState(((BlockHitResult)result).getBlockPos()).contains(Properties.WATERLOGGED)) { // Don't place water on top of waterloggable blocks
            ViewResult viewResult = ViewResult.VISIBLE;
            viewResult.pitch = rotation.pitch;
            viewResult.yaw = rotation.yaw;
            
            return viewResult;
        }
        return ViewResult.INVISIBLE;
    }
    
    /**
     * An overriden function from Minecraft. This original function was protected, so we couldn't use it.
     * @param pitch
     * @param yaw
     * @return
     */
    private static Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
    }
    
    /**
     * A class created to carry variables between functions.
     *
     */
    private static class Rotation {
        public float yaw, pitch, maxDist;
        
        public Rotation(float yaw, float pitch, float maxDist) {
            this.pitch = pitch;
            this.yaw = yaw;
            this.maxDist = maxDist;
        }
    }
    // TODO something wrong with the rotation

    /**
     * A slightly modified version of {@code ClientPlayerInteractionManager}.{@code interactItem(PlayerEntity player, World world, Hand hand)}
     * that allows us to change the players rotation (yaw & pitch). 
     * @param mc
     * @param hand
     * @param result This contains the rotation
     * @return Whether or not the interact succeeded
     */
    public static ActionResult interactItem(MinecraftClient mc, Hand hand, ViewResult result) {
        if (mc.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) {
            System.out.println("Spectator");
            return ActionResult.PASS;
         } else {
             /* mc.interactionManager.syncSelectedSlot() in inaccessible
             *  To workaround this, we call interactBlock() and give a blockpos outside the worlborder,
             *  this will imediately return but the syncSelectedSlot is called.
             */
             mc.interactionManager.interactBlock(null, null, null, new BlockHitResult(null, null, 
                     new BlockPos(mc.world.getWorldBorder().getBoundEast()+3, 0, mc.world.getWorldBorder().getBoundSouth() + 3), false));
             // TODO Bug: when the player move's the block is placed at the wrong place
             mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(),
                     mc.player.getZ(), result.yaw, result.pitch, mc.player.isOnGround()));
             mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(hand));
            ItemStack itemStack = mc.player.getStackInHand(hand);
            if (mc.player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
                System.out.println("Cooldown");
               return ActionResult.PASS;
            } else {
               TypedActionResult<ItemStack> typedActionResult = itemStack.use(mc.world, mc.player, hand);
               ItemStack itemStack2 = (ItemStack)typedActionResult.getValue();
               if (itemStack2 != itemStack) {
                   mc.player.setStackInHand(hand, itemStack2);
               }

               return typedActionResult.getResult();
            }
         }
    }

}
