package net.earthcomputer.clientcommands.features;

import net.earthcomputer.clientcommands.command.ClientCommandHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Minimal stand-in for upstream's ServerBrandManager, which inspects the server's mod list to
 * decide when RNG-manipulation warnings are actually necessary. This port always warns the first
 * time RNG prediction/manipulation is enabled in a session, which is the safe default: enabling
 * enchant prediction or player-RNG cracking is potentially detectable by server-side anti-cheat,
 * and the player should know that before using it.
 */
public final class ServerBrandManager {
    private static boolean hasWarned = false;

    private ServerBrandManager() {
    }

    public static void rngWarning() {
        if (hasWarned) {
            return;
        }
        hasWarned = true;
        ClientCommandHelper.sendFeedback(Component.literal(
            "RNG manipulation/prediction features are potentially detectable by server-side anti-cheat. " +
                "Only use them on servers where this is allowed."
        ).withStyle(ChatFormatting.YELLOW));
    }

    public static void reset() {
        hasWarned = false;
    }
}
