package net.nesona.swordthrow.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.nesona.swordthrow.client.ThrowAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    // Approximate shoulder position in camera space; the arm swings around this point.
    private static final double PX = 0.5, PY = -0.55, PZ = 0.1;

    /** Vanilla's bare-arm renderer (normally only used when the hand is empty). */
    @Shadow
    protected abstract void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                                 int light, float equipProgress, float swingProgress, Arm arm);

    /** Keep the sword in hand until the release frame, even if the stack was already consumed. */
    @ModifyVariable(method = "renderFirstPersonItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack swordthrow$keepSword(ItemStack item, AbstractClientPlayerEntity player, float tickDelta,
                                          float pitch, Hand hand) {
        if (hand == Hand.MAIN_HAND && ThrowAnimation.type() == ThrowAnimation.Type.THROW) {
            ItemStack held = ThrowAnimation.heldStack();
            if (!held.isEmpty() && ThrowAnimation.progress(tickDelta) < ThrowAnimation.RELEASE_T) {
                return held;
            }
        }
        return item;
    }

    /**
     * Vanilla draws only the item (no arm) when you hold a sword. While the throw/pull animation
     * runs, draw the arm too so the animation is visible. When the hand is empty vanilla already
     * draws the arm, so skip it then.
     */
    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"))
    private void swordthrow$drawArm(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                                    float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                                    VertexConsumerProvider vcp, int light, CallbackInfo ci) {
        if (hand != Hand.MAIN_HAND || item.isEmpty() || player.isInvisible()) return;
        if (ThrowAnimation.type() == ThrowAnimation.Type.NONE) return;
        matrices.push();
        this.renderArmHoldingItem(matrices, vcp, light, equipProgress, swingProgress, player.getMainArm());
        matrices.pop();
    }

    /** Whole-arm keyframed motion, applied around the hand/arm/item render. */
    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("HEAD"))
    private void swordthrow$begin(float tickDelta, MatrixStack matrices,
                                  VertexConsumerProvider.Immediate vcp, ClientPlayerEntity player,
                                  int light, CallbackInfo ci) {
        matrices.push();
        if (ThrowAnimation.type() == ThrowAnimation.Type.NONE) return;
        float[] p = ThrowAnimation.handPose(tickDelta);

        matrices.translate(p[0], p[1], p[2]);
        matrices.translate(PX, PY, PZ);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(p[4]));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p[3]));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(p[5]));
        matrices.translate(-PX, -PY, -PZ);
    }

    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("RETURN"))
    private void swordthrow$end(float tickDelta, MatrixStack matrices,
                                VertexConsumerProvider.Immediate vcp, ClientPlayerEntity player,
                                int light, CallbackInfo ci) {
        matrices.pop();
    }
}
