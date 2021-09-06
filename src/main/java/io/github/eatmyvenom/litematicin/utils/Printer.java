package io.github.eatmyvenom.litematicin.utils;

import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.ACCURATE_BLOCK_PLACEMENT;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_BREAK_BLOCKS;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_DELAY;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_FLUIDS;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_MAX_BLOCKS;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_PAPER;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_RANGE_X;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Y;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Z;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.collect.ImmutableMap;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager.PlacementPart;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement.RequiredEnabled;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.RayTraceUtils.RayTraceWrapper;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.LayerRange;
import fi.dy.masa.malilib.util.SubChunkPos;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.block.SignBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Printer {
	
    private static final List<PositionCache> positionCache = new ArrayList<>();

    private static FacingDataStorage facingDataStorage = new FacingDataStorage();
    
    /**
     * For now this function tries to equip the correct item for placing the block.
     * @param closest Not used.
     * @param mc {@code MinecraftClient} for gathering information and accessing the clientPlayer.
     * @param preference {@code BlockState} of how block should be after placing.
     * @param pos {@code BlockPos} of block you want to place.
     * @return true if correct item is in hand
     */
    @Environment(EnvType.CLIENT)
    public static boolean doSchematicWorldPickBlock(boolean closest, MinecraftClient mc, BlockState preference,
            BlockPos pos, ItemStack stack) {

        if (stack.isEmpty() == false) {
            PlayerInventory inv = mc.player.getInventory();

            if (mc.player.getAbilities().creativeMode) {
                // BlockEntity te = world.getBlockEntity(pos);

                // The creative mode pick block with NBT only works correctly
                // if the server world doesn't have a TileEntity in that position.
                // Otherwise it would try to write whatever that TE is into the picked
                // ItemStack.
                // if (GuiBase.isCtrlDown() && te != null && mc.world.isAir(pos)) {
                // ItemUtils.storeTEInStack(stack, te);
                // }

                // InventoryUtils.setPickedItemToHand(stack, mc);

                // NOTE: I dont know why we have to pick block in creative mode. You can simply
                // just set the block
                
                mc.interactionManager.clickCreativeStack(stack, 36 + inv.selectedSlot);

                return true;
            } else {
               
                int slot = inv.getSlotWithStack(stack);
                boolean shouldPick = inv.selectedSlot != slot;
                boolean canPick = (slot != -1) && slot < 36 && (EASY_PLACE_MODE_PAPER.getBooleanValue() ? slot < maxSlotId : true);

                if (shouldPick && canPick) {
                    InventoryUtils.setPickedItemToHand(stack, mc);
                    return true;
                    //return InteractionUtils.setPickedItemToHand(stack, mc);
                } else if (!shouldPick) {
                    return true;
                } else if (slot == -1 && Configs.Generic.PICK_BLOCK_SHULKERS.getBooleanValue()) {
                	slot = InventoryUtils.findSlotWithBoxWithItem(mc.player.playerScreenHandler, stack, false);
                	if (slot != -1) {
                		ItemStack boxStack = mc.player.playerScreenHandler.slots.get(slot).getStack();
                        InventoryUtils.setPickedItemToHand(boxStack, mc);
                        return true;
                	}
                }
            }
        }

        return false;
    }

    /**
     * Not supported.
     * @param mc {@code MinecraftClient}
     * @return null
     */
    public static ActionResult doAccuratePlacePrinter(MinecraftClient mc) {
	return null;
    }
    
    // For printing delay
    private static long lastPlaced = new Date().getTime();
    private static Breaker breaker = new Breaker();
    
    // For height datapacks
    public static int worldBottomY = 0;
    public static int worldTopY = 256;
    
    // Paper anti-cheat values
    private static final int maxReachCreative = 6;
    private static final int maxReachSurvival = 6;
    private static final int maxSlotId = 9;
    private static final int maxDistance = 8;
    // This one is hard to determine, since paper starts to be suspicious when he receives more then 8 packets in a tick.
    // Value found by pure testing, and can probably be optimized even further
    private static final double minimumDelay = 0.1D;
    private static final int paperMaxInteractsPerFunctionCall = 1; // Otherwise to much packets at once

    // Water replacement
    public static BlockState waterReplacementBlock = Blocks.AIR.getDefaultState();
    
    /**
     * Trying to place or break a block.
     * @param mc {@code MinecraftClient} for accessing the playerclient and managers...
     * @return {@code ActionResult} returns how well the placing/breaking went.
     */
    @Environment(EnvType.CLIENT)
    public static ActionResult doPrinterAction(MinecraftClient mc) {
    	if (breaker.isBreakingBlock()) return ActionResult.SUCCESS; // Don't place blocks while we're breaking one
    	if (new Date().getTime() < lastPlaced + 1000.0 * (EASY_PLACE_MODE_PAPER.getBooleanValue() ? minimumDelay : EASY_PLACE_MODE_DELAY.getDoubleValue())) return ActionResult.PASS; // Check delay between blockplace's

    	// Configs
    	int rangeX = EASY_PLACE_MODE_RANGE_X.getIntegerValue();
        int rangeY = EASY_PLACE_MODE_RANGE_Y.getIntegerValue();
        int rangeZ = EASY_PLACE_MODE_RANGE_Z.getIntegerValue();
        int maxReach = Math.max(Math.max(rangeX,rangeY),rangeZ);
        boolean breakBlocks = EASY_PLACE_MODE_BREAK_BLOCKS.getBooleanValue();
        boolean CanUseProtocol = ACCURATE_BLOCK_PLACEMENT.getBooleanValue();
        // Paper anti-cheat implementation
        if (EASY_PLACE_MODE_PAPER.getBooleanValue()) {
            if (mc.player.getAbilities().creativeMode) {
                rangeX = maxReachCreative; 
                rangeY = maxReachCreative;
                rangeZ = maxReachCreative;
            } else {
                rangeX = maxReachSurvival; 
                rangeY = maxReachSurvival;
                rangeZ = maxReachSurvival;
            }
            maxReach = maxDistance;
            EASY_PLACE_MODE_DELAY.setDoubleValue(minimumDelay);
        }
        
    	
    	// Get the block the player is currently looking at
        RayTraceWrapper traceWrapper = RayTraceUtils.getGenericTrace(mc.world, mc.player, maxReach, true);
        if (traceWrapper == null) {
            return ActionResult.FAIL;
        }
        BlockHitResult trace = traceWrapper.getBlockHitResult();
        BlockPos tracePos = trace.getBlockPos();
        int posX = tracePos.getX();
        int posY = tracePos.getY();
        int posZ = tracePos.getZ();
        
        // Get all PlacementParts nearby the player's lookAtBlock
        SubChunkPos cpos = new SubChunkPos(tracePos);
        List<PlacementPart> list = DataManager.getSchematicPlacementManager().getAllPlacementsTouchingSubChunk(cpos);

        if (list.isEmpty()) {
            return ActionResult.PASS;
        }
        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;
        int minX = 0;
        int minY = 0;
        int minZ = 0;

        // Setting min and max x,y,z
        boolean foundBox = false;
        for (PlacementPart part : list) {
            IntBoundingBox pbox = part.getBox();
            if (pbox.containsPos(tracePos)) {

                ImmutableMap<String, Box> boxes = part.getPlacement()
                        .getSubRegionBoxes(RequiredEnabled.PLACEMENT_ENABLED);

                for (Box box : boxes.values()) {

                    final int boxXMin = Math.min(box.getPos1().getX(), box.getPos2().getX());
                    final int boxYMin = Math.min(box.getPos1().getY(), box.getPos2().getY());
                    final int boxZMin = Math.min(box.getPos1().getZ(), box.getPos2().getZ());
                    final int boxXMax = Math.max(box.getPos1().getX(), box.getPos2().getX());
                    final int boxYMax = Math.max(box.getPos1().getY(), box.getPos2().getY());
                    final int boxZMax = Math.max(box.getPos1().getZ(), box.getPos2().getZ());

                    if (posX < boxXMin || posX > boxXMax || posY < boxYMin || posY > boxYMax || posZ < boxZMin
                            || posZ > boxZMax)
                        continue;
                    minX = boxXMin;
                    maxX = boxXMax;
                    minY = boxYMin;
                    maxY = boxYMax;
                    minZ = boxZMin;
                    maxZ = boxZMax;
                    foundBox = true;

                    break;
                }

                break;
            }
        }

        if (!foundBox) {
            return ActionResult.PASS;
        }

        LayerRange range = DataManager.getRenderLayerRange(); // get renderingRange
        Direction[] facingSides = Direction.getEntityFacingOrder(mc.player);
        Direction primaryFacing = facingSides[0];
        Direction horizontalFacing = primaryFacing; // For use in blocks with only horizontal rotation

        int index = 0;
        while (horizontalFacing.getAxis() == Direction.Axis.Y && index < facingSides.length) {
            horizontalFacing = facingSides[index++];
        }

        World world = SchematicWorldHandler.getSchematicWorld();

        /*
         * TODO: THIS IS REALLY BAD IN TERMS OF EFFICIENCY. I suggest using some form of
         * search with a built in datastructure first Maybe quadtree? (I dont know how
         * MC works)
         */

        int maxInteract = EASY_PLACE_MODE_PAPER.getBooleanValue() ? paperMaxInteractsPerFunctionCall : EASY_PLACE_MODE_MAX_BLOCKS.getIntegerValue();
        int interact = 0;
        boolean hasPicked = false;
        Text pickedBlock = null;

        // Ensure the positions are within the box and within range of the block the player is looking at
        int fromX = Math.max(posX - rangeX, minX);
        int fromY = Math.max(posY - rangeY, minY);
        int fromZ = Math.max(posZ - rangeZ, minZ);

        int toX = Math.min(posX + rangeX, maxX);
        int toY = Math.min(posY + rangeY, maxY);
        int toZ = Math.min(posZ + rangeZ, maxZ);

        // Ensure the Y is between the bottom and top of the world
        toY = Math.max(Math.min(toY,worldTopY),worldBottomY);
        fromY = Math.max(Math.min(fromY, worldTopY),worldBottomY); 

        // Ensure the positions are within the player's range
        fromX = Math.max(fromX,mc.player.getBlockX() - rangeX);
        fromY = Math.max(fromY,mc.player.getBlockY() - rangeY);
        fromZ = Math.max(fromZ,mc.player.getBlockZ() - rangeZ);

        toX = Math.min(toX,mc.player.getBlockX() + rangeX);
        toY = Math.min(toY,mc.player.getBlockY() + rangeY);
        toZ = Math.min(toZ,mc.player.getBlockZ() + rangeZ);
        
        
        for (int x = fromX; x <= toX; x++) {
            for (int y = fromY; y <= toY; y++) {
                for (int z = fromZ; z <= toZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    
                    BlockState stateSchematic = world.getBlockState(pos);
                    BlockState stateClient = mc.world.getBlockState(pos);
                    
                    if (stateSchematic == stateClient)
                        continue;
                    
                	// Offset to player
                    double dx = mc.player.getX() - x - 0.5;
                    double dy = mc.player.getY() - y - 0.5;
                    double dz = mc.player.getZ() - z - 0.5;
                    
                    // Another check if its within reach
                    if (dx * dx + dy * dy + dz * dz > maxReach * maxReach)
                    	continue;

                    // Paper anti-cheat
                    if (EASY_PLACE_MODE_PAPER.getBooleanValue()) {
                        double paperDx = mc.player.getX() - x;
                        double paperDy = mc.player.getEyeY() - y;
                        double paperDz = mc.player.getZ() - z;
                        double reachDistance = paperDx * paperDx + paperDy * paperDy + paperDz * paperDz;
                        
                        if (reachDistance > ((mc.player.getAbilities().creativeMode) ? maxReachCreative * maxReachCreative : maxReachSurvival * maxReachSurvival))
                        	continue;
                    }
                    
                    if (range.isPositionWithinRange(pos) == false) // Check if block is rendered
                        continue;
                    
                    // Block breaking
                    if (breakBlocks && stateSchematic != null && !stateClient.isAir()) {
                        if (!stateClient.getBlock().getName().equals(stateSchematic.getBlock().getName()) && dx * dx + Math.pow(dy + 1.5,2) + dz * dz <= maxReach * maxReach) {
                            if (stateClient.getBlock() instanceof FluidBlock) {
                                if (stateClient.get(FluidBlock.LEVEL) == 0) {
                                    // Some manipulation with blockStates to reach the placement code
                                    stateClient = Blocks.AIR.getDefaultState();
                                    // When air, should automatically continue;
                                    stateSchematic = waterReplacementBlock;
                                }
                            } else if (mc.player.getAbilities().creativeMode) {
                        		mc.interactionManager.attackBlock(pos, Direction.DOWN);
                                interact++;

                                if (interact >= maxInteract) {
                                	lastPlaced = new Date().getTime();
                                    return ActionResult.SUCCESS;
                                }
                        	} else if (stateClient.getBlock().getHardness() != -1.0f) { // For survival, (don't break unbreakable blocks)
                        	    // When breakInstantly, a single attack is more then enough, (paper doesn't allow this)
                				if (stateClient.getBlock().getHardness() == 0 && !EASY_PLACE_MODE_PAPER.getBooleanValue()) {
                				    mc.interactionManager.attackBlock(pos, Direction.DOWN);
                				    return ActionResult.SUCCESS;
                				}
                            	breaker.startBreakingBlock(pos, mc);
                            	return ActionResult.SUCCESS;
                        	}
                        }
                    }
                    
                    // Skip non source fluids & air
                    if (stateSchematic.isAir() 
                            || (stateSchematic.contains(FluidBlock.LEVEL) && stateSchematic.get(FluidBlock.LEVEL) != 0)) 
                        continue;
                    
                    // If there's already a block of the same type, but it needs some more clicks (e.g. repeaters, half slabs, ...)
                    if (stateClient != stateSchematic && stateClient.getBlock() == stateSchematic.getBlock()) {
                        
                        /*
                         * Sometimes, blocks have other states like the delay on a repeater. So, this
                         * code clicks the block until the state is the same I don't know if Schematica
                         * does this too, I just did it because I work with a lot of redstone
                         */
                    	// stateClient.isAir() is already checked in the printCheckCancel
                        if (!mc.player.isSneaking() && !isPositionCached(pos, true)) {
                            Block cBlock = stateClient.getBlock();
                            Block sBlock = stateSchematic.getBlock();

                            if (cBlock.getName().equals(sBlock.getName())) {
                                Direction facingSchematic = fi.dy.masa.malilib.util.BlockUtils
                                        .getFirstPropertyFacingValue(stateSchematic);
                                Direction facingClient = fi.dy.masa.malilib.util.BlockUtils
                                        .getFirstPropertyFacingValue(stateClient);
                                
                                // If both block face the same direction
                                if (facingSchematic == facingClient) {
                                    // Instead of a while loop, we use a for loop so it wouldn't get stuck
                                    int maxClickTimes = 10;
                                    int clickTimes = 0;
                                    Direction side = Direction.NORTH;
                                    
                                    while (stateClient != stateSchematic && clickTimes < maxClickTimes) {
                                        Hand hand = Hand.MAIN_HAND;

                                        Vec3d hitPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

                                        BlockHitResult hitResult = new BlockHitResult(hitPos, side, pos, false);

                                        mc.interactionManager.interactBlock(mc.player, mc.world, hand, hitResult);
                                        
                                        interact++; // Global clicked blocks / tick
                                        clickTimes++; // Clicked this block / tick
                                        stateClient = mc.world.getBlockState(pos); 
                                        
                                        /*
                                         * When the maxInteract is reached/exceeded && the block is clicked enough times
                                         * it reaches this line and returns without caching the block
                                         */
                                        if (interact > maxInteract)
                                            break;
                                    }

                                    if (stateClient == stateSchematic) {
                                        cacheEasyPlacePosition(pos, true);
                                        lastPlaced = new Date().getTime();
                                        return ActionResult.SUCCESS;
                                    }
                                  
                                }
                            }
                        }
                        continue;
                    }
                    // If block is already placed (=> is already placed)
                    if (isPositionCached(pos, false)) continue;
                    
                    // If the player has the required item in his inventory or is in creative
                    ItemStack stack = MaterialCache.getInstance().getRequiredBuildItemForState(stateSchematic, world, pos);
                    
                    // The function above does not take waterloggable blocks into account
                    if (stateSchematic.getBlock() instanceof Waterloggable && stateSchematic.get(Properties.WATERLOGGED) && stateClient.getBlock() == stateSchematic.getBlock())
                        stack = new ItemStack(Items.WATER_BUCKET);

                    if (stack.isEmpty() == false && (mc.player.getAbilities().creativeMode || mc.player.getInventory().getSlotWithStack(stack) != -1)) {
                        
                        Block sBlock = stateSchematic.getBlock();
                        
                        if (stateSchematic == stateClient)
                            continue;
                        
                        // If the item is a block
                        if (stack.getItem() instanceof BlockItem) {
                            // Block placing, when the correct block is already placed, but the state is incorrect, continue. (e.g. a powered rail that is not powered, we can't do anything about it, or the schematic is incomplete or the redstone isn't placed yet.
                            if (stateClient.getBlock() == stateSchematic.getBlock())
                                continue;
                            
                            // When gravity block, check if there's a block underneath
                            if (sBlock instanceof FallingBlock) {
                                BlockPos Offsetpos = new BlockPos(x, y-1, z);
                        		BlockState OffsetstateSchematic = world.getBlockState(Offsetpos);
                        		BlockState OffsetstateClient = mc.world.getBlockState(Offsetpos);
                        		
                                if (FallingBlock.canFallThrough(OffsetstateClient) || (breakBlocks && !OffsetstateClient.getBlock().getName().equals(OffsetstateSchematic.getBlock().getName())) )
                                    continue;
                            } 
    
                            if ((stateSchematic.getBlock() instanceof DoorBlock
                                    && stateSchematic.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
                                    || (stateSchematic.getBlock() instanceof BedBlock
                                            && stateSchematic.get(BedBlock.PART) == BedPart.HEAD))
                                                    continue;
                            
                            // Exception for signs (edge case)
                            if (stateSchematic.getBlock() instanceof SignBlock
                                    && !(stateSchematic.getBlock() instanceof WallSignBlock)) {
                                if ((MathHelper.floor((double) ((180.0F + mc.player.getYaw()) * 16.0F / 360.0F) + 0.5D)
                                        & 15) != stateSchematic.get(SignBlock.ROTATION))
                                    continue;
                            }
                            
                            // This should prevent the printer from placing torches and ... in water
                            if (!stateSchematic.getBlock().canPlaceAt(stateSchematic, mc.world, pos)) continue;
                           
                            /*
                             * Blocks to test:
                             * Sign
                             * Ladder
                             * tripwire
                             * torch
                             * Trapdoor
                             * 
                             */
                            
                            BlockHitResult hitResult = InteractionUtils.getCorrectDirection(stateSchematic, stateClient, Hand.MAIN_HAND, pos, mc);
                            
                            if (hitResult == null)
                                continue;
                            
                            // If player hasn't the correct item in his hand yet
                            // Depending on the maxInteracts, it tries to place the same block types in one function call
                            if (!hasPicked) {
                                if (doSchematicWorldPickBlock(true, mc, stateSchematic, pos, stack) == false) // When wrong item in hand
                                    return ActionResult.FAIL;
                                hasPicked = true;
                                pickedBlock = stateSchematic.getBlock().getName();
                            } else if (pickedBlock != null && !pickedBlock.equals(stateSchematic.getBlock().getName()))
                                continue;
                            
                            
                            Hand hand = EntityUtils.getUsedHandForItem(mc.player, stack);

                            // Go to next block if a wrong item is in the player's hand
                            // It will place the same block per function call
                            if (hand == null)
                                continue;
                            
                            // Can be null, block items do not use this, only necessary when using carpetProtocol
                            Vec3d hitPos = new Vec3d(0.5d, 0.5d, 0.5d);
                            // Carpet Accurate Placement protocol support, plus BlockSlab support
                            if(CanUseProtocol && IsBlockSupportedCarpet(stateSchematic.getBlock())) {
                                hitPos = applyCarpetProtocolHitVec(pos,stateSchematic,hitPos);
                            }
                            
                            // System.out.printf("pos: %s side: %s, hit: %s\n", pos, side, hitPos);
                            // pos, side, hitPos
                            ActionResult actionResult = mc.interactionManager.interactBlock(mc.player, mc.world, hand, hitResult);
                            
                            if (!actionResult.isAccepted()) 
                                continue;
                            
                            if (actionResult.shouldSwingHand())
                                   mc.player.swingHand(hand);
                            
                            // Ugly workaround, only cache when the block doesn't need to be waterlogged
                            if (!(stateSchematic.getBlock() instanceof Waterloggable && stateSchematic.get(Properties.WATERLOGGED))) {
                                // Mark that this position has been handled (use the non-offset position that is checked above)
                                cacheEasyPlacePosition(pos, false);
                            }
                            interact++;
                            
                            // Place multiple slabs/pickles at once, since this is one block
                            // Disadvantage: you can exceed the maxInteract.
                            if (stateSchematic.getBlock() instanceof SlabBlock
                                    && stateSchematic.get(SlabBlock.TYPE) == SlabType.DOUBLE) {
                                stateClient = mc.world.getBlockState(pos);

                                if (stateClient.getBlock() instanceof SlabBlock
                                        && stateClient.get(SlabBlock.TYPE) != SlabType.DOUBLE) {
                                    hitResult = InteractionUtils.getCorrectDirection(stateSchematic, stateClient, hand, pos, mc);
                                    mc.interactionManager.interactBlock(mc.player, mc.world, hand, hitResult);
                                    interact++;
                                }
                            }
                            else if (stateSchematic.getBlock() instanceof SeaPickleBlock
                                    && stateSchematic.get(SeaPickleBlock.PICKLES)>1) {
                                stateClient = mc.world.getBlockState(pos);
                                if (stateClient.getBlock() instanceof SeaPickleBlock
                                        && stateClient.get(SeaPickleBlock.PICKLES) < stateSchematic.get(SeaPickleBlock.PICKLES)) {
                                    hitResult = InteractionUtils.getCorrectDirection(stateSchematic, stateClient, hand, pos, mc);
                                    mc.interactionManager.interactBlock(mc.player, mc.world, hand, hitResult);
                                    interact++;
                                }
                            }
                            
                        } else if (EASY_PLACE_MODE_FLUIDS.getBooleanValue()) { // If its an item
                            // TODO remove some of the duplicate code
                            // TODO support more items
                            ViewResult result = ViewResult.INVISIBLE;
                            
                            // Currently only water/lava blocks placement is supported neighbor 
                            if (stateSchematic.getBlock() instanceof FluidBlock) {
                                // Water can only be placed if the neighbor is a solid block -> not air and not a waterloggable block
                                result = InteractionUtils.canSeeAndInteractWithBlock(pos, mc,
                                        (neighborState) -> !neighborState.isAir() && !neighborState.contains(Properties.WATERLOGGED));
                            }else if (stateSchematic.contains(Properties.WATERLOGGED)) {
                                
                                // Waterloggable block only visible when neighbor is air.
                                result = InteractionUtils.canSeeAndInteractWithBlock(pos, mc,
                                        (state) -> state.isAir());
                            }
                            
                            if (result == ViewResult.INVISIBLE)
                                continue;
                            
                            // If player hasn't the correct item in his hand yet
                            // Depending on the maxInteracts, it tries to place the same block types in one function call
                            if (!hasPicked) {
                                if (doSchematicWorldPickBlock(true, mc, stateSchematic, pos, stack) == false) // When wrong item in hand
                                    return ActionResult.FAIL;
                                hasPicked = true;
                                pickedBlock = stateSchematic.getBlock().getName();
                            } else if (pickedBlock != null && !pickedBlock.equals(stateSchematic.getBlock().getName()))
                                continue;
                            
                            Hand hand = EntityUtils.getUsedHandForItem(mc.player, stack);
                            
                            // Go to next block if a wrong item is in the player's hand
                            // It will place the same block per function call
                            if (hand == null)
                                continue;
                            
                            // Set player's rotation to fake rotation
                            float previousYaw = mc.player.getYaw();
                            float previousPitch = mc.player.getPitch();
                            
                            mc.player.setYaw(result.yaw);
                            mc.player.setPitch(result.pitch);

                            ActionResult actionResult = mc.interactionManager.interactItem(mc.player, mc.world, hand);
                            
                            // Set rotation back to original
                            mc.player.setYaw(previousYaw);
                            mc.player.setPitch(previousPitch);
                            
                            if (!actionResult.isAccepted())
                                  continue;

                            if (actionResult.shouldSwingHand())
                               mc.player.swingHand(hand);
                           
                            // Mark that this position has been handled (use the non-offset position that is checked above)
                            cacheEasyPlacePosition(pos, false);
                            interact++;
                        }
                        
                        if (interact >= maxInteract) {
                            lastPlaced = new Date().getTime();
                            return ActionResult.SUCCESS;
                        }
                    }
                }
            }
        }

        // If not exceeded maxInteract but placed a few blocks
        if (interact > 0) {
        	lastPlaced = new Date().getTime();
        	return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }

    // Possible same as WorldUtils.applyCarpetProtocolHitVec
    /**
     * Apply hit vectors (used to be Carpet hit vec protocol, but I think it is
     * uneccessary now with orientation/states programmed in)
     * 
     * @param pos
     * @param state
     * @param hitVecIn
     * @return
     */
    public static Vec3d applyHitVec(BlockPos pos, BlockState state, Vec3d hitVecIn, Direction side) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        double dx = hitVecIn.getX();
        double dy = hitVecIn.getY();
        double dz = hitVecIn.getZ();
        Block block = state.getBlock();

        /*
         * I dont know if this is needed, just doing to mimick client According to the
         * MC protocol wiki, the protocol expects a 1 on a side that is clicked
         */
        if (side == Direction.UP) {
            dy = 1;
        } else if (side == Direction.DOWN) {
            dy = 0;
        } else if (side == Direction.EAST) {
            dx = 1;
        } else if (side == Direction.WEST) {
            dx = 0;
        } else if (side == Direction.SOUTH) {
            dz = 1;
        } else if (side == Direction.NORTH) {
            dz = 0;
        }

        if (block instanceof StairsBlock) {
            if (state.get(StairsBlock.HALF) == BlockHalf.TOP) {
                dy = 0.9;
            } else {
                dy = 0;
            }
        } else if (block instanceof SlabBlock && state.get(SlabBlock.TYPE) != SlabType.DOUBLE) {
            if (state.get(SlabBlock.TYPE) == SlabType.TOP) {
                dy = 0.9;
            } else {
                dy = 0;
            }
        } else if (block instanceof TrapdoorBlock) {
            if (state.get(TrapdoorBlock.HALF) == BlockHalf.TOP) {
                dy = 0.9;
            } else {
                dy = 0;
            }
        }
        return new Vec3d(x + dx, y + dy, z + dz);
    }
    
    /**
     * 
     * @param pos
     * @param useClicked
     * @return true when the {@code pos} is cached (if not {@code useClicked}, or if the block is cached and the block is a clickable one (repeater, ...)
     */
    public static boolean isPositionCached(BlockPos pos, boolean useClicked) {
        long currentTime = System.nanoTime();
        boolean cached = false;

        for (int i = 0; i < positionCache.size(); ++i) {
            PositionCache val = positionCache.get(i);
            boolean expired = val.hasExpired(currentTime);

            if (expired) {
                positionCache.remove(i);
                --i;
            } else if (val.getPos().equals(pos)) {

                // Item placement and "using"/"clicking" (changing delay for repeaters) are
                // diffferent
                if (!useClicked || val.hasClicked) {
                    cached = true;
                }

                // Keep checking and removing old entries if there are a fair amount
                if (positionCache.size() < 16) {
                    break;
                }
            }
        }

        return cached;
    }

    /**
     * Cache a placed block for an amount of time.
     * @param pos
     * @param useClicked
     */
    private static void cacheEasyPlacePosition(BlockPos pos, boolean useClicked) {
        PositionCache item = new PositionCache(pos, System.nanoTime(), useClicked ? 1000000000 : 2000000000);
        // TODO: Create a separate cache for clickable items, as this just makes
        // duplicates
        if (useClicked)
            item.hasClicked = true;
        positionCache.add(item);
    }
    
    public static Vec3d applyCarpetProtocolHitVec(BlockPos pos, BlockState state, Vec3d hitVecIn)
    {
        double dx = pos.getX();
        double dy = pos.getY();
        double dz = pos.getZ();
        double x = hitVecIn.x;
        double y = hitVecIn.y;
        double z = hitVecIn.z;
        Block block = state.getBlock();
        Direction facing = fi.dy.masa.malilib.util.BlockUtils.getFirstPropertyFacingValue(state);
        final int propertyIncrement = 32;
        double relX = hitVecIn.x - pos.getX();

        if (facing != null)
        {
            x = pos.getX() + relX + 2 + (facing.getId() * 2);
        }
        if (block instanceof RepeaterBlock)
        {
            x += ((state.get(RepeaterBlock.DELAY))) * propertyIncrement;
        }
        else if (block instanceof TrapdoorBlock && state.get(TrapdoorBlock.HALF) == BlockHalf.TOP)
        {
            x += propertyIncrement;
        }
        else if (block instanceof ComparatorBlock && state.get(ComparatorBlock.MODE) == ComparatorMode.SUBTRACT)
        {
            x += propertyIncrement;
        }
        else if (block instanceof TrapdoorBlock && state.get(TrapdoorBlock.HALF) == BlockHalf.TOP)
        {
            x += propertyIncrement;
        }
        else if (block instanceof StairsBlock && state.get(StairsBlock.HALF) == BlockHalf.TOP)
        {
            x += propertyIncrement;
        }
        else if (block instanceof SlabBlock && state.get(SlabBlock.TYPE) != SlabType.DOUBLE)
        {
            //x += 10; // Doesn't actually exist (yet?)

            // Do it via vanilla
            if (state.get(SlabBlock.TYPE) == SlabType.TOP)
            {
                y = pos.getY() + 0.9;
            }
            else
            {
                y = pos.getY();
            }
        }

        return new Vec3d(dx+x,dy+ y,dz+ z);
    }
	
	private static Boolean IsBlockSupportedCarpet(Block SchematicBlock){
    	if (SchematicBlock instanceof GlazedTerracottaBlock || SchematicBlock instanceof ObserverBlock 
    	        || SchematicBlock instanceof RepeaterBlock || SchematicBlock instanceof TrapdoorBlock 
    	        || SchematicBlock instanceof ComparatorBlock || SchematicBlock instanceof DispenserBlock 
    	        || SchematicBlock instanceof PistonBlock || SchematicBlock instanceof StairsBlock) {
    	    return true;
    	}
    	return false;
	}
    
    public static class PositionCache {
        private final BlockPos pos;
        private final long time;
        private final long timeout;
        public boolean hasClicked = false;

        private PositionCache(BlockPos pos, long time, long timeout) {
            this.pos = pos;
            this.time = time;
            this.timeout = timeout;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public boolean hasExpired(long currentTime) {
            return currentTime - this.time > this.timeout;
        }
    }
}
