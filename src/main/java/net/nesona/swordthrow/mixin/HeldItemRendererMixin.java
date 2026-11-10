package net.nesona.swordthrow.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.nesona.swordthrow.client.FirstPersonRig;
import net.nesona.swordthrow.client.ThrowAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    /**
     * While a throw/pull animation runs, skip vanilla's main-hand rendering and draw our own
     * animated arm rig (with the sword in its fist) instead.
     */
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void swordthrow$rig(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                                float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                                VertexConsumerProvider vcp, int light, CallbackInfo ci) {
        if (hand != Hand.MAIN_HAND || player.isInvisible() || player.getMainArm() != Arm.RIGHT) return;
        ThrowAnimation.Type type = ThrowAnimation.type();
        if (type == ThrowAnimation.Type.NONE) return;

        ItemStack shown;
        if (type == ThrowAnimation.Type.THROW) {
            // The sword stays in the fist until the release frame, even if the server already consumed it.
            shown = ThrowAnimation.progress(tickDelta) < ThrowAnimation.RELEASE_T
                    ? ThrowAnimation.heldStack() : ItemStack.EMPTY;
        } else {
            shown = item;
        }
        FirstPersonRig.render((HeldItemRenderer) (Object) this, player, shown, matrices, vcp, light, tickDelta);
        ci.cancel();
    }
}
