package net.earthcomputer.clientcommands.util;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Arrays;
import java.util.Optional;

/**
 * Trimmed port of upstream CUtil: only the enchantment-level lookups used by the RNG-cracking
 * mixins/features are kept. The regex-fuse and item-NBT (de)serialization helpers aren't needed
 * by the commands ported so far.
 */
public final class CUtil {
    private CUtil() {
    }

    public static int getEnchantmentLevel(RegistryAccess registryAccess, ResourceKey<Enchantment> enchantment, ItemStack stack) {
        Optional<Holder.Reference<Enchantment>> enchHolder = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).get(enchantment);
        return enchHolder.isPresent() ? EnchantmentHelper.getItemEnchantmentLevel(enchHolder.get(), stack) : 0;
    }

    public static int getEnchantmentLevel(ResourceKey<Enchantment> enchantment, LivingEntity entity) {
        Optional<Holder.Reference<Enchantment>> enchHolder = entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(enchantment);
        if (enchHolder.isEmpty()) {
            return 0;
        }
        return Arrays.stream(EquipmentSlot.values()).mapToInt(slot -> entity.getItemBySlot(slot).getEnchantments().getLevel(enchHolder.get())).max().orElse(0);
    }
}
