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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
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
            float y, z, rot;
            if (t < 0.35f) { // wind-up: hand goes back and up
                float w = t / 0.35f;
                z = 0.3f * w;
                y = 0.15f * w;
                rot = 30f * w;
            } else {         // thrust forward, then recover
                float v = (t - 0.35f) / 0.65f;
                float s = MathHelper.sin((float) Math.PI * v);
                z = MathHelper.lerp(v, 0.3f, 0f) - 0.6f * s;
                y = MathHelper.lerp(v, 0.15f, 0f) - 0.1f * s;
                rot = MathHelper.lerp(v, 30f, 0f) - 40f * s;
            }
            matrices.translate(0.0, y, z);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rot));
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
