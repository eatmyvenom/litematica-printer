package io.github.eatmyvenom.litematicin.mixin.Litematica;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.malilib.config.IConfigBase;
import io.github.eatmyvenom.litematicin.LitematicaMixinMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Configs.class)
public class ConfigsMixin {
    @Redirect(method = "loadFromFile", at = @At(value = "FIELD", target = "Lfi/dy/masa/litematica/config/Configs$Generic;OPTIONS:Lcom/google/common/collect/ImmutableList;"))
    private static ImmutableList<IConfigBase> moreOptions() {
        return ImmutableList.of(
                Configs.Visuals.ENABLE_RENDERING,
                Configs.Visuals.ENABLE_SCHEMATIC_RENDERING,

                Configs.Visuals.ENABLE_AREA_SELECTION_RENDERING,
                Configs.Visuals.ENABLE_PLACEMENT_BOXES_RENDERING,
                Configs.Visuals.ENABLE_SCHEMATIC_BLOCKS,
                Configs.Visuals.ENABLE_SCHEMATIC_OVERLAY,
                Configs.Visuals.OVERLAY_REDUCED_INNER_SIDES,
                Configs.Visuals.RENDER_AREA_SELECTION_BOX_SIDES,
                Configs.Visuals.RENDER_BLOCKS_AS_TRANSLUCENT,
                Configs.Visuals.RENDER_COLLIDING_SCHEMATIC_BLOCKS,
                Configs.Visuals.RENDER_ERROR_MARKER_CONNECTIONS,
                Configs.Visuals.RENDER_ERROR_MARKER_SIDES,
                Configs.Visuals.RENDER_PLACEMENT_BOX_SIDES,
                Configs.Visuals.RENDER_PLACEMENT_ENCLOSING_BOX,
                Configs.Visuals.RENDER_PLACEMENT_ENCLOSING_BOX_SIDES,
                Configs.Visuals.RENDER_TRANSLUCENT_INNER_SIDES,
                Configs.Visuals.SCHEMATIC_OVERLAY_ENABLE_OUTLINES,
                Configs.Visuals.SCHEMATIC_OVERLAY_ENABLE_SIDES,
                Configs.Visuals.SCHEMATIC_OVERLAY_MODEL_OUTLINE,
                Configs.Visuals.SCHEMATIC_OVERLAY_MODEL_SIDES,
                Configs.Visuals.SCHEMATIC_OVERLAY_RENDER_THROUGH,
                Configs.Visuals.SCHEMATIC_OVERLAY_TYPE_EXTRA,
                Configs.Visuals.SCHEMATIC_OVERLAY_TYPE_MISSING,
                Configs.Visuals.SCHEMATIC_OVERLAY_TYPE_WRONG_BLOCK,
                Configs.Visuals.SCHEMATIC_OVERLAY_TYPE_WRONG_STATE,
                Configs.Visuals.SCHEMATIC_VERIFIER_BLOCK_MODELS,

                LitematicaMixinMod.EASY_PLACE_MODE_RANGE_X,
                LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Y,
                LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Z,
                LitematicaMixinMod.EASY_PLACE_MODE_MAX_BLOCKS,
                LitematicaMixinMod.EASY_PLACE_MODE_BREAK_BLOCKS,


                Configs.Visuals.GHOST_BLOCK_ALPHA,
                Configs.Visuals.PLACEMENT_BOX_SIDE_ALPHA,
                Configs.Visuals.SCHEMATIC_OVERLAY_OUTLINE_WIDTH,
                Configs.Visuals.SCHEMATIC_OVERLAY_OUTLINE_WIDTH_THROUGH
        );
    }
}
