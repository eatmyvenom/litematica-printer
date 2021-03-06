package io.github.eatmyvenom.litematicin;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import net.fabricmc.api.ModInitializer;

public class LitematicaMixinMod implements ModInitializer {

	public static final ConfigInteger EASY_PLACE_MODE_RANGE_X      = new ConfigInteger("easyPlaceModeRangeX", 3, 0, 1024, "X Range for EasyPlace");
	public static final ConfigInteger EASY_PLACE_MODE_RANGE_Y      = new ConfigInteger("easyPlaceModeRangeY", 3, 0, 1024, "Y Range for EasyPlace");
	public static final ConfigInteger EASY_PLACE_MODE_RANGE_Z      = new ConfigInteger("easyPlaceModeRangeZ", 3, 0, 1024, "Z Range for EasyPlace");
	public static final ConfigInteger EASY_PLACE_MODE_MAX_BLOCKS   = new ConfigInteger("easyPlaceModeMaxBlocks", 3, 1, 1000000, "Max block interactions per cycle");
	public static final ConfigBoolean EASY_PLACE_MODE_BREAK_BLOCKS = new ConfigBoolean("easyPlaceModeBreakBlocks", false, "Automatically breaks blocks. Currently only works in Creative.");

	public static final ImmutableList<IConfigBase> betterList = ImmutableList.of(
			Configs.Generic.AREAS_PER_WORLD,
			//BETTER_RENDER_ORDER,
			Configs.Generic.CHANGE_SELECTED_CORNER,
			Configs.Generic.EASY_PLACE_MODE,
			Configs.Generic.EASY_PLACE_HOLD_ENABLED,
			Configs.Generic.EXECUTE_REQUIRE_TOOL,
			Configs.Generic.FIX_RAIL_ROTATION,
			Configs.Generic.LOAD_ENTIRE_SCHEMATICS,
			Configs.Generic.PASTE_IGNORE_INVENTORY,
			Configs.Generic.PICK_BLOCK_ENABLED,
			Configs.Generic.PLACEMENT_RESTRICTION,
			Configs.Generic.RENDER_MATERIALS_IN_GUI,
			Configs.Generic.RENDER_THREAD_NO_TIMEOUT,
			Configs.Generic.TOOL_ITEM_ENABLED,

			Configs.Generic.PASTE_REPLACE_BEHAVIOR,
			Configs.Generic.SELECTION_CORNERS_MODE,

			Configs.Generic.PASTE_COMMAND_INTERVAL,
			Configs.Generic.PASTE_COMMAND_LIMIT,
			Configs.Generic.PASTE_COMMAND_SETBLOCK,
			Configs.Generic.PICK_BLOCKABLE_SLOTS,
			Configs.Generic.TOOL_ITEM,

			EASY_PLACE_MODE_RANGE_X,
			EASY_PLACE_MODE_RANGE_Y,
			EASY_PLACE_MODE_RANGE_Z,
			EASY_PLACE_MODE_MAX_BLOCKS,
			EASY_PLACE_MODE_BREAK_BLOCKS

	);

	@Override
	public void onInitialize() {
		System.out.println("YeeFuckinHaw");
	}
}
