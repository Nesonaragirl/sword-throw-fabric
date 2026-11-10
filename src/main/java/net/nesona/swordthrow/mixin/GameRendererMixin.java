package net.nesona.swordthrow.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.nesona.swordthrow.client.ThrowAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void swordthrow$fov(Camera camera, float tickDelta, boolean changingFov,
                                CallbackInfoReturnable<Double> cir) {
        float off = ThrowAnimation.fovOffset(tickDelta);
        if (off != 0f) {
            cir.setReturnValue(cir.getReturnValue() + off);
        }
    }
}
