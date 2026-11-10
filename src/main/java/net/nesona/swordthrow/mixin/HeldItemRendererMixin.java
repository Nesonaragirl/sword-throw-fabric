package net.nesona.swordthrow.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
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
            if (!held.isEmpty() && ThrowAnimation.progress(tickDelta) < ThrowAnimation.RELEASE_T + 0.05f) {
                return held;
            }
        }
        return item;
    }

    /** Whole-arm motion: hooked on renderItem so it always applies, regardless of item/hand state. */
    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("HEAD"))
    private void swordthrow$begin(float tickDelta, MatrixStack matrices,
                                  VertexConsumerProvider.Immediate vcp, ClientPlayerEntity player,
                                  int light, CallbackInfo ci) {
        matrices.push();
        ThrowAnimation.Type type = ThrowAnimation.type();
        if (type == ThrowAnimation.Type.NONE) return;
        float t = ThrowAnimation.progress(tickDelta);

        if (type == ThrowAnimation.Type.THROW) {
            float y, z, rot, side;
            float r = ThrowAnimation.RELEASE_T;
            if (t < r) { // wind-up: arm cocks back and up
                float w = t / r;
                w = w * w * (3f - 2f * w);
                z = 0.5f * w;
                y = 0.3f * w;
                rot = 55f * w;
                side = 14f * w;
            } else {     // violent thrust forward, then recover
                float v = (t - r) / (1f - r);
                float s = MathHelper.sin((float) Math.PI * v);
                z = MathHelper.lerp(v, 0.5f, 0f) - 1.1f * s;
                y = MathHelper.lerp(v, 0.3f, 0f) - 0.25f * s;
                rot = MathHelper.lerp(v, 55f, 0f) - 80f * s;
                side = MathHelper.lerp(v, 14f, 0f);
            }
            matrices.translate(0.0, y, z);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rot));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side));
        } else { // PULL: arm yanks back toward the player
            float s = MathHelper.sin((float) Math.PI * t);
            matrices.translate(0.0, 0.1f * s, 0.7f * s);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30f * s));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-12f * s));
        }
    }

    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("RETURN"))
    private void swordthrow$end(float tickDelta, MatrixStack matrices,
                                VertexConsumerProvider.Immediate vcp, ClientPlayerEntity player,
                                int light, CallbackInfo ci) {
        matrices.pop();
    }
}
