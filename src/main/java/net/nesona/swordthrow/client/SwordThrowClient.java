package net.nesona.swordthrow.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.nesona.swordthrow.SwordThrowMod;
import net.nesona.swordthrow.ThrowPayload;
import org.lwjgl.glfw.GLFW;

public class SwordThrowClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBinding throwKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.swordthrow.throw", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.swordthrow.main"));

        EntityRendererRegistry.register(SwordThrowMod.THROWN_SWORD, ThrownSwordRenderer::new);
        SwordThrowMod.clientPullHook = () -> ThrowAnimation.start(ThrowAnimation.Type.PULL);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity p = client.player;
            while (throwKey.wasPressed()) {
                if (p == null || ThrowAnimation.type() != ThrowAnimation.Type.NONE) continue;
                ItemStack s = p.getMainHandStack();
                if (s.getItem() instanceof SwordItem && !p.getItemCooldownManager().isCoolingDown(s.getItem())) {
                    ThrowAnimation.start(ThrowAnimation.Type.THROW, s.copy());
                }
            }
            ThrowAnimation.tick();

            if (ThrowAnimation.released() && p != null && client.world != null) {
                // The sword leaves the hand now: tell the server and play the burst.
                if (p.getMainHandStack().getItem() instanceof SwordItem) {
                    ClientPlayNetworking.send(new ThrowPayload());
                }
                Vec3d look = p.getRotationVec(1.0f);
                Vec3d pos = p.getEyePos().add(look.multiply(1.0)).add(0, -0.25, 0);
                client.world.addParticle(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y, pos.z, 0, 0, 0);
                for (int i = 0; i < 12; i++) {
                    client.world.addParticle(ParticleTypes.CRIT, pos.x, pos.y, pos.z,
                            look.x * 0.8 + p.getRandom().nextGaussian() * 0.12,
                            look.y * 0.8 + p.getRandom().nextGaussian() * 0.12,
                            look.z * 0.8 + p.getRandom().nextGaussian() * 0.12);
                }
            }
        });
    }
}
