package io.github.eatmyvenom.litematicin.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PumpkinBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.StonecutterBlock;

public class FacingDataStorage {

	private final Map<Class<? extends Block>, FacingData> facingMap;

	/**
	 * Setup facing data onInitialize
	 */
	public FacingDataStorage() {
		this.facingMap = new LinkedHashMap<>();
		setUpFacingData();
	}
	
	/**
     * Add facing data to the facingDataMap.
     * @param c block where the facing data belongs to.
     * @param data facing data 
     */
    private void addFD(final Class<? extends Block> c, FacingData data) {
        facingMap.put(c, data);
    }

    /**
     * Initialize all blocks that needs facingdata.
     */
    private void setUpFacingData() {
        /*
         * 0 = Normal up/down/east/west/south/north directions 1 = Horizontal directions
         * 2 = Wall Attactchable block
         * 
         * 
         * TODO: THIS CODE MUST BE CLEANED UP.
         */

        // All directions, reverse of what player is facing
        addFD(PistonBlock.class, new FacingData(0, true));
        addFD(DispenserBlock.class, new FacingData(0, true));
        addFD(DropperBlock.class, new FacingData(0, true));

        // All directions, normal direction of player
        addFD(ObserverBlock.class, new FacingData(0, false));

        // Horizontal directions, normal direction
        addFD(StairsBlock.class, new FacingData(1, false));
        addFD(DoorBlock.class, new FacingData(1, false));
        addFD(BedBlock.class, new FacingData(1, false));
        addFD(FenceGateBlock.class, new FacingData(1, false));

        // Horizontal directions, reverse of what player is facing
        addFD(ChestBlock.class, new FacingData(1, true));
        addFD(RepeaterBlock.class, new FacingData(1, true));
        addFD(ComparatorBlock.class, new FacingData(1, true));
        addFD(EnderChestBlock.class, new FacingData(1, true));
        addFD(FurnaceBlock.class, new FacingData(1, true));
        addFD(LecternBlock.class, new FacingData(1, true));
        addFD(LoomBlock.class, new FacingData(1, true));
        addFD(BeehiveBlock.class, new FacingData(1, true));
        addFD(StonecutterBlock.class, new FacingData(1, true));
        addFD(CarvedPumpkinBlock.class, new FacingData(1, true));
        addFD(PumpkinBlock.class, new FacingData(1, true));
        addFD(EndPortalFrameBlock.class, new FacingData(1, true));

        // Top/bottom placable side mountable blocks
        addFD(LeverBlock.class, new FacingData(2, false));
        addFD(AbstractButtonBlock.class, new FacingData(2, false));
     //addFD(BellBlock.class, new FacingData(2, false));
        //addFD(GrindstoneBlock.class, new FacingData(2, false));

    }

    // TODO: This must be moved to another class and not be static.
    /**
     * Get {@code FacingData} about a block by giving its blockState.
     * @param state {@code BlockState} of the block where you need facingData of.
     * @return facingData
     */
    public FacingData getFacingData(BlockState state) {
        Block block = state.getBlock();
        for (final Class<? extends Block> c : facingMap.keySet()) {
            if (c.isInstance(block)) {
                return facingMap.get(c);
            }
        }
        return null;
    }

	class FacingData {
        public int type;
        public boolean isReversed;

        FacingData(int type, boolean isrev) {
            this.type = type;
            this.isReversed = isrev;
        }
    }
}
