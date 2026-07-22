package net.earthcomputer.clientcommands;

import net.earthcomputer.clientcommands.features.EnchantmentCracker;
import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.earthcomputer.clientcommands.features.ServerBrandManager;
import net.minecraft.util.Mth;

/**
 * Simplified stand-in for upstream's Configs class. Upstream persists these via the
 * "betterconfig" Fabric library with a full annotation-driven config screen; that library isn't
 * available on Forge, so this port keeps everything as plain in-memory static fields with the
 * same defaults and validation. Config values reset on game restart. Only the fields actually
 * used by the ported /cenchant and /ccrackrng commands are kept.
 */
public final class Configs {
    private Configs() {
    }

    public static EnchantmentCracker.CrackState enchCrackState = EnchantmentCracker.CrackState.UNCRACKED;
    public static PlayerRandCracker.CrackState playerCrackState = PlayerRandCracker.CrackState.UNCRACKED;

    public static boolean enchantingPrediction = false;
    public static void setEnchantingPrediction(boolean enchantingPrediction) {
        Configs.enchantingPrediction = enchantingPrediction;
        if (enchantingPrediction) {
            ServerBrandManager.rngWarning();
        } else {
            EnchantmentCracker.resetCracker();
        }
    }

    public static boolean playerRNGMaintenance = true;

    public static boolean toolBreakWarning = false;

    private static int maxEnchantItemThrows = 64 * 256;
    public static int getMaxEnchantItemThrows() {
        return maxEnchantItemThrows;
    }
    public static void setMaxEnchantItemThrows(int maxEnchantItemThrows) {
        Configs.maxEnchantItemThrows = Mth.clamp(maxEnchantItemThrows, 0, 1000000);
    }

    private static int minEnchantBookshelves = 0;
    public static int getMinEnchantBookshelves() {
        return minEnchantBookshelves;
    }
    public static void setMinEnchantBookshelves(int minEnchantBookshelves) {
        Configs.minEnchantBookshelves = Mth.clamp(minEnchantBookshelves, 0, 15);
        Configs.maxEnchantBookshelves = Math.max(Configs.maxEnchantBookshelves, Configs.minEnchantBookshelves);
    }

    private static int maxEnchantBookshelves = 15;
    public static int getMaxEnchantBookshelves() {
        return maxEnchantBookshelves;
    }
    public static void setMaxEnchantBookshelves(int maxEnchantBookshelves) {
        Configs.maxEnchantBookshelves = Mth.clamp(maxEnchantBookshelves, 0, 15);
        Configs.minEnchantBookshelves = Math.min(Configs.minEnchantBookshelves, Configs.maxEnchantBookshelves);
    }

    private static int minEnchantLevels = 1;
    public static int getMinEnchantLevels() {
        return minEnchantLevels;
    }
    public static void setMinEnchantLevels(int minEnchantLevels) {
        Configs.minEnchantLevels = Mth.clamp(minEnchantLevels, 1, 30);
        Configs.maxEnchantLevels = Math.max(Configs.maxEnchantLevels, Configs.minEnchantLevels);
    }

    private static int maxEnchantLevels = 30;
    public static int getMaxEnchantLevels() {
        return maxEnchantLevels;
    }
    public static void setMaxEnchantLevels(int maxEnchantLevels) {
        Configs.maxEnchantLevels = Mth.clamp(maxEnchantLevels, 1, 30);
        Configs.minEnchantLevels = Math.min(Configs.minEnchantLevels, Configs.maxEnchantLevels);
    }

    private static int maxEnchantSlot = 3;
    public static int getMaxEnchantSlot() {
        return maxEnchantSlot;
    }
    public static void setMaxEnchantSlot(int maxEnchantSlot) {
        Configs.maxEnchantSlot = Mth.clamp(maxEnchantSlot, 1, 3);
    }

    public static float itemThrowsPerTick = 1;
}
