package net.nesona.swordthrow.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.nesona.swordthrow.ThrownSwordEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The server kicks players with "Attempting to attack an invalid entity" when they
 * left-click any PersistentProjectileEntity. Block the attack packet client-side.
 */
@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void swordthrow$noAttackStuckSword(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (target instanceof ThrownSwordEntity) {
            ci.cancel();
        }
    }
}
