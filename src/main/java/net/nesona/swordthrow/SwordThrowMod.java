package net.nesona.swordthrow;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class SwordThrowMod implements ModInitializer {
    public static final String MOD_ID = "swordthrow";

    public static final EntityType<ThrownSwordEntity> THROWN_SWORD = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "thrown_sword"),
            FabricEntityTypeBuilder.<ThrownSwordEntity>create(SpawnGroup.MISC, ThrownSwordEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .trackRangeBlocks(4)
                    .trackedUpdateRate(20)
                    .build());

    /** Set by the client entrypoint so common code can trigger the pull-out arm animation. */
    public static Runnable clientPullHook = () -> {};

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(ThrowPayload.ID, ThrowPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ThrowPayload.ID,
                (payload, context) -> throwSword(context.player()));
    }

    private static void throwSword(ServerPlayerEntity p) {
        ItemStack held = p.getMainHandStack();
        Item item = held.getItem();
        if (!(item instanceof SwordItem) || p.getItemCooldownManager().isCoolingDown(item)) return;

        World w = p.getWorld();
        boolean creative = p.getAbilities().creativeMode;
        ItemStack one = held.copy();
        one.setCount(1);

        ThrownSwordEntity e = new ThrownSwordEntity(w, p, one, creative);
        e.setVelocity(p, p.getPitch(), p.getYaw(), 0.0f, 2.5f, 1.0f);
        w.spawnEntity(e);
        w.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ITEM_TRIDENT_THROW,
                SoundCategory.PLAYERS, 1.0f, 1.2f);

        if (!creative) held.decrement(1);
        p.getItemCooldownManager().set(item, 10);
    }
}
