package io.github.eatmyvenom.litematicin.mixin.Litematica;

import fi.dy.masa.litematica.util.WorldUtils;
import io.github.eatmyvenom.litematicin.utils.Printer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = WorldUtils.class, remap = false)
public class WorldUtilsMixin {
    /**
     * @author joe mama
     */
    @Overwrite
    private static ActionResult doEasyPlaceAction(MinecraftClient mc)
    {
        return Printer.doPrinterAction(mc);
    }
}
