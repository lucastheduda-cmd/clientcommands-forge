package net.earthcomputer.clientcommands.util;

import net.minecraft.world.item.Item;

/**
 * Upstream clientcommands supports many past Minecraft protocol versions via a real
 * multi-version compatibility layer. This Forge port only targets the current Minecraft
 * 26.2 protocol, so this class is a stub that always reports "latest version" and lets the
 * dead legacy-version branches in ported code fall through to their modern-version behavior.
 */
public final class MultiVersionCompat {
    public static final MultiVersionCompat INSTANCE = new MultiVersionCompat();

    public static final int V1_13_2 = 1;
    public static final int V1_15_2 = 2;
    public static final int V1_16 = 3;
    public static final int V1_17 = 4;
    public static final int V1_18 = 5;
    public static final int V1_18_2 = 6;
    public static final int V1_19 = 7;
    public static final int V1_20 = 8;
    public static final int V1_20_6 = 9;
    public static final int V1_21 = 10;
    public static final int V1_21_2 = 11;
    public static final int V1_21_9 = 12;
    private static final int LATEST = Integer.MAX_VALUE;

    private MultiVersionCompat() {
    }

    public int getProtocolVersion() {
        return LATEST;
    }

    public boolean doesItemExist(Item item) {
        return true;
    }
}
