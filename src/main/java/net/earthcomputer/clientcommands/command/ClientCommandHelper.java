package net.earthcomputer.clientcommands.command;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;

/**
 * Trimmed/adapted port of upstream ClientCommandHelper. Upstream's generic Flag/exclusion
 * machinery (getFlag/withFlag, backed by a mixin-injected accessor on Fabric's client command
 * source) isn't ported: this build only needs one boolean flag on one command
 * ({@code /cenchant --simulate}), which CEnchantCommand implements directly as two sibling
 * command registrations instead of a reusable Flag abstraction.
 */
public class ClientCommandHelper {
    public static void sendError(Component error) {
        sendFeedback(Component.literal("").append(error).withStyle(ChatFormatting.RED));
    }

    public static void sendHelp(Component help) {
        sendFeedback(Component.literal("").append(help).withStyle(ChatFormatting.AQUA));
    }

    public static void sendFeedback(String translationKey, Object... args) {
        sendFeedback(Component.translatable(translationKey, args));
    }

    public static void sendFeedback(Component message) {
        Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(message);
    }

    /**
     * The {@code time} parameter (custom display duration) from upstream isn't applied here:
     * upstream sets it via {@code gui.hud.overlayMessageTime}, a private field Fabric's Access
     * Widener exposes; Forge has no equivalent wired up, so this just uses the default duration.
     */
    public static void addOverlayMessage(Component message, int time) {
        Gui gui = Minecraft.getInstance().gui;
        gui.hud.setOverlayMessage(message, false);
    }
}
