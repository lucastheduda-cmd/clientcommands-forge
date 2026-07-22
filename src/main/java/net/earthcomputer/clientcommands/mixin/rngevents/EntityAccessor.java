package net.earthcomputer.clientcommands.mixin.rngevents;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Upstream Fabric reaches {@code Entity.isInvulnerableToBase} (protected) directly, relying on
 * Fabric's Access Widener to widen it. Forge doesn't have an equivalent wired up here, so this
 * Mixin @Invoker exposes it instead (used by PlayerMixin.clientSideAttackDamage).
 */
@Mixin(value = Entity.class, remap = false)
public interface EntityAccessor {
    @Invoker("isInvulnerableToBase")
    boolean callIsInvulnerableToBase(DamageSource source);
}
