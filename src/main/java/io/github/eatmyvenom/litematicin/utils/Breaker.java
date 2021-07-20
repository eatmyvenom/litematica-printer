package io.github.eatmyvenom.litematicin.utils;

import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.malilib.event.TickHandler;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * The breaking needs to be done every tick, since the WorldUtils.easyPlaceOnUseTick (which calls our Printer)
 * is called multiple times per tick we cannot break blocks through that method. Or the speed will be twice the
 * normal speed and detectable by anti-cheats.
 * 
 *
 */
public class Breaker implements IClientTickHandler {
	
	private boolean breakingBlock = false;
	private BlockPos pos;
	
	public Breaker() {
		TickHandler.getInstance().registerClientTickHandler(this);
	}
	
	public void startBreakingBlock(BlockPos pos) {
		this.breakingBlock = true;
		this.pos = pos;
	}
	
	public boolean isBreakingBlock() {
		return this.breakingBlock;
	}

	@Override
	public void onClientTick(MinecraftClient mc) {
		if (!isBreakingBlock()) return;
		if (!Hotkeys.EASY_PLACE_ACTIVATION.getKeybind().isKeybindHeld() ||
	            !KeybindMulti.isKeyDown(KeybindMulti.getKeyCode(mc.options.keyUse))
	            || mc.player == null) { // When the use button is released, stop the breaking;
			mc.interactionManager.cancelBlockBreaking();
			return;
		}
		
		Direction side = Direction.values()[0];
		
		if (mc.interactionManager.updateBlockBreakingProgress(pos, side)) {
			mc.particleManager.addBlockBreakingParticles(pos, side);
			mc.player.swingHand(Hand.MAIN_HAND);
		}
		
		if (mc.world.getBlockState(pos).isAir()) {
			this.breakingBlock = false;
			mc.interactionManager.cancelBlockBreaking();
		}
	}
	
}
