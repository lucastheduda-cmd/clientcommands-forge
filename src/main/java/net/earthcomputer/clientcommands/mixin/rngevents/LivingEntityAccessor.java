package net.earthcomputer.clientcommands.mixin.rngevents;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * See EntityAccessor: exposes a protected field upstream Fabric reaches (on an arbitrary
 * LivingEntity instance, not just {@code this}) via an Access Widener.
 */
@Mixin(value = LivingEntity.class, remap = false)
public interface LivingEntityAccessor {
    @Accessor("lastHurt")
    float callGetLastHurt();
}
