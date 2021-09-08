package io.github.eatmyvenom.litematicin.utils;

import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;

public class InteractionUtils {
    
    /**
     * Check if an item can be used at the given {@code BlockPos}.
     * @param pos
     * @param mc
     * @return
     */
    public static ViewResult canSeeAndInteractWithBlock(BlockPos pos, MinecraftClient mc, Predicate<BlockState> statesToAccept) {
        Direction[] possibleDirections = Direction.values();
        
        for (int i = 0; i < possibleDirections.length; i++) {
            Vec3i vec = possibleDirections[i].getVector();
            BlockState state = mc.world.getBlockState(pos.add(vec));
            
            // You can't place water on air or a waterloggen block
            if (!statesToAccept.test(state)) continue;
            
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
        pitch = pitch * 180.0d / Math.PI;
        yaw = yaw * 180.0d / Math.PI;

        yaw += 90d;
        
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
        final ClientPlayerEntity player = mc.player;
        Rotation rotation = getNeededRotation(player, toSee, blockFace);
        
        float tickDelta = mc.getTickDelta();
        double maxDist = rotation.maxDist + 0.5f;
        
        Vec3d vec3d = player.getCameraPosVec(tickDelta);
        Vec3d vec3d2 = getRotationVector(rotation.pitch, rotation.yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDist, vec3d2.y * maxDist, vec3d2.z * maxDist);
        HitResult result = mc.world.raycast(new RaycastContext(vec3d, vec3d3,
                RaycastContext.ShapeType.OUTLINE, 
                        RaycastContext.FluidHandling.ANY, player));
        
        if (result.getType() == Type.BLOCK 
                && !(result.getPos().squaredDistanceTo(player.getX(), player.getEyeY(), player.getZ()) < rotation.maxDist * rotation.maxDist)) { // If there's a block between the player and the location
            ViewResult viewResult = ViewResult.VISIBLE;
            viewResult.pitch = rotation.pitch;
            viewResult.yaw = rotation.yaw;
            
            return viewResult;
        }
        return ViewResult.INVISIBLE;
    }
    
    /**
     * An overridden function from Minecraft. This original function was protected, so we couldn't use it.
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

    public static BlockHitResult getCorrectDirection(BlockState stateSchematic, BlockState stateClient, 
            Hand hand, BlockPos pos, MinecraftClient mc) {

        // If is blockItem, should normally be true, since this function only gets called when placing a block
        for (Direction side : Direction.values()) {
            BlockHitResult hitResult = new BlockHitResult(new Vec3d(0, 0, 0), side, pos, false);
            ItemPlacementContext ctx = new ItemPlacementContext(new ItemUsageContext(mc.player, hand, hitResult));
            if (ctx.canPlace()) {
                BlockState stateAfterPlacement = stateSchematic.getBlock().getPlacementState(ctx);
                
                if (hasCorrectRotation(stateAfterPlacement, stateSchematic))
                    return hitResult;
                else if ((stateSchematic.contains(Properties.SLAB_TYPE) && stateSchematic.get(Properties.SLAB_TYPE) == SlabType.DOUBLE)
                        || stateSchematic.contains(Properties.CANDLES))
                    return hitResult;
            }
        }
        return null;
    }
    
    public static boolean hasCorrectRotation(BlockState toCheck, BlockState correctState) {
        // If both are oriented the same
        Property<?>[] propertiesToCheck = new Property<?>[] {
            Properties.FACING,
            Properties.HORIZONTAL_FACING,
            Properties.BLOCK_HALF,
            Properties.SLAB_TYPE,
            Properties.WALL_MOUNT_LOCATION,
            Properties.AXIS
        };

        for (Property<?> property : propertiesToCheck) {
            if (toCheck.contains(property) && correctState.contains(property)) {
                if (toCheck.get(property) != correctState.get(property)) {
                    return false;
                }
            }
        }
        return true;
    }
}
