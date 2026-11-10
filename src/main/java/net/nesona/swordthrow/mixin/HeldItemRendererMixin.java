package net.nesona.swordthrow.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.nesona.swordthrow.client.ThrowAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    /** Keep the sword in hand until the release point, even if the stack was already consumed. */
    @ModifyVariable(method = "renderFirstPersonItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack swordthrow$keepSword(ItemStack item, AbstractClientPlayerEntity player, float tickDelta,
                                          float pitch, Hand hand) {
        if (hand == Hand.MAIN_HAND && ThrowAnimation.type() == ThrowAnimation.Type.THROW) {
            ItemStack held = ThrowAnimation.heldStack();
            if (!held.isEmpty() && ThrowAnimation.progress(tickDelta) < 0.5f) {
                return held;
            }
        }
        return item;
    }

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void swordthrow$begin(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                                  float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                                  VertexConsumerProvider vcp, int light, CallbackInfo ci) {
        matrices.push();
        if (hand != Hand.MAIN_HAND) return;

        ThrowAnimation.Type type = ThrowAnimation.type();
        if (type == ThrowAnimation.Type.NONE) return;
        float t = ThrowAnimation.progress(tickDelta);

        if (type == ThrowAnimation.Type.THROW) {
            float y, z, rot, side;
            if (t < 0.35f) { // wind-up: hand goes back and up
                float w = t / 0.35f;
                z = 0.35f * w;
                y = 0.2f * w;
                rot = 40f * w;
                side = 10f * w;
            } else {         // thrust forward, then recover
                float v = (t - 0.35f) / 0.65f;
                float s = MathHelper.sin((float) Math.PI * v);
                z = MathHelper.lerp(v, 0.35f, 0f) - 0.8f * s;
                y = MathHelper.lerp(v, 0.2f, 0f) - 0.15f * s;
                rot = MathHelper.lerp(v, 40f, 0f) - 60f * s;
                side = MathHelper.lerp(v, 10f, 0f);
            }
            matrices.translate(0.0, y, z);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rot));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side));
        } else { // PULL: hand jerks back toward the player
            float s = MathHelper.sin((float) Math.PI * t);
            matrices.translate(0.0, 0.05f * s, 0.45f * s);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20f * s));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-8f * s));
        }
    }

    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"))
    private void swordthrow$end(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                                float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                                VertexConsumerProvider vcp, int light, CallbackInfo ci) {
        matrices.pop();
    }
}
