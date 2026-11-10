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
            while (throwKey.wasPressed()) {
                ClientPlayerEntity p = client.player;
                if (p == null) continue;
                ItemStack s = p.getMainHandStack();
                if (s.getItem() instanceof SwordItem && !p.getItemCooldownManager().isCoolingDown(s.getItem())) {
                    ClientPlayNetworking.send(new ThrowPayload());
                    ThrowAnimation.start(ThrowAnimation.Type.THROW, s.copy());
                }
            }
            ThrowAnimation.tick();
        });
    }
}
