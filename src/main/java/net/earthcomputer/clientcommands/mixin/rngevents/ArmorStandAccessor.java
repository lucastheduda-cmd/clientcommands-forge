package net.earthcomputer.clientcommands.mixin.rngevents;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * See EntityAccessor: exposes a private field upstream Fabric reaches via an Access Widener.
 */
@Mixin(value = ArmorStand.class, remap = false)
public interface ArmorStandAccessor {
    @Accessor("invisible")
    boolean callIsInvisible();
}
