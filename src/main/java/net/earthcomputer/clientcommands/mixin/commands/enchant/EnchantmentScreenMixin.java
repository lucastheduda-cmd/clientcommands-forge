package net.earthcomputer.clientcommands.mixin.commands.enchant;

import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adapted from upstream: the GUI overlay text and "add info" button (upstream's postExtract and
 * onInit injections, using the GuiGraphicsExtractor render-state API) are replaced by the
 * {@code /cenchant info} command (see CEnchantCommand), which does the same "feed the cracker the
 * current table's clues" work and prints the same status as chat text instead of an overlay. The
 * RNG-consumption hook below (onItemEnchanted) is load-bearing for cracking and is kept as-is.
 */
@Mixin(value = EnchantmentScreen.class, remap = false)
public abstract class EnchantmentScreenMixin extends AbstractContainerScreen<EnchantmentMenu> {
    public EnchantmentScreenMixin(EnchantmentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryButtonClick(II)V"))
    public void onItemEnchanted(CallbackInfoReturnable<Boolean> ci) {
        PlayerRandCracker.onEnchantedItem();
    }
}
