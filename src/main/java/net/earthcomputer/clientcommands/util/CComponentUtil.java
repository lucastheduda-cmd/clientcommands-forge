package net.earthcomputer.clientcommands.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

/**
 * Trimmed port of upstream CComponentUtil: only the click-to-run "/command" text helper used by
 * /cenchant and /ccrackrng is kept. The upstream NBT/entity-selector text-resolution machinery
 * (updateForEntity and friends) isn't needed by the commands ported so far.
 */
public final class CComponentUtil {
    private CComponentUtil() {
    }

    public static Component getCommandTextComponent(String translationKey, String command) {
        return getCommandTextComponent(Component.translatable(translationKey), command);
    }

    public static Component getCommandTextComponent(MutableComponent component, String command) {
        return component.withStyle(style -> style.applyFormat(ChatFormatting.UNDERLINE)
            .withColor(ChatFormatting.GREEN)
            .withClickEvent(new ClickEvent.RunCommand(command))
            .withHoverEvent(new HoverEvent.ShowText(Component.literal(command))));
    }
}
