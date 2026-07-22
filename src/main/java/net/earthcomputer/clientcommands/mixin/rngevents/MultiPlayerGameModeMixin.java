package net.earthcomputer.clientcommands.mixin.rngevents;

import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adapted from upstream: only the mining-damage reset-detection hook is kept. Upstream's
 * startPrediction injections exist to support the "infiniteTools" auto-item-saving feature,
 * which isn't ported (see PlayerRandCracker), so they're dropped here.
 */
@Mixin(value = MultiPlayerGameMode.class, remap = false)
public class MultiPlayerGameModeMixin {
    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public void onDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        Level level = player.level();
        ItemStack stack = player.getMainHandItem();
        if (stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES)) {
            BlockState state = level.getBlockState(pos);
            if (state.getDestroySpeed(level, pos) != 0) {
                PlayerRandCracker.onItemDamage(1, player, stack);
            }
        }
    }
}
