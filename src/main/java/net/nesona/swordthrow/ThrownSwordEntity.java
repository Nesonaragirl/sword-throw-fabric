package net.nesona.swordthrow;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class ThrownSwordEntity extends PersistentProjectileEntity {
    private static final TrackedData<ItemStack> STACK =
            DataTracker.registerData(ThrownSwordEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Boolean> STUCK =
            DataTracker.registerData(ThrownSwordEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private boolean creativeThrow;

    public ThrownSwordEntity(EntityType<? extends ThrownSwordEntity> type, World world) {
        super(type, world);
        this.setSound(SoundEvents.ITEM_TRIDENT_HIT_GROUND);
    }

    public ThrownSwordEntity(World world, LivingEntity owner, ItemStack stack, boolean creative) {
        super(SwordThrowMod.THROWN_SWORD, owner, world, stack, null);
        this.setSound(SoundEvents.ITEM_TRIDENT_HIT_GROUND);
        this.setSwordStack(stack);
        this.creativeThrow = creative;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(STACK, ItemStack.EMPTY);
        builder.add(STUCK, false);
    }

    public ItemStack getSwordStack() {
        return this.dataTracker.get(STACK);
    }

    public void setSwordStack(ItemStack stack) {
        this.dataTracker.set(STACK, stack);
    }

    public boolean isStuck() {
        return this.dataTracker.get(STUCK);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            this.dataTracker.set(STUCK, this.inGround);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult result) {
        Entity target = result.getEntity();
        float dmg = 1.0f;
        AttributeModifiersComponent comp = getSwordStack()
                .getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        for (AttributeModifiersComponent.Entry en : comp.modifiers()) {
            if (en.attribute().equals(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
                dmg += (float) en.modifier().value();
            }
        }
        Entity owner = this.getOwner();
        DamageSource src = this.getDamageSources().thrown(this, owner == null ? this : owner);
        target.damage(src, dmg);

        if (!this.getWorld().isClient) {
            ItemStack s = getSwordStack().copy();
            if (s.isDamageable()) {
                s.setDamage(s.getDamage() + 1);
                if (s.getDamage() >= s.getMaxDamage()) {
                    this.discard();
                    return;
                }
            }
            setSwordStack(s);
        }
        this.setVelocity(this.getVelocity().multiply(-0.01, -0.1, -0.01));
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0f, 1.0f);
    }

    /** Right-click a stuck sword to pull it out. */
    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND || !isStuck()) return ActionResult.PASS;
        if (this.getWorld().isClient) {
            SwordThrowMod.clientPullHook.run();
            return ActionResult.SUCCESS;
        }
        if (!creativeThrow) {
            player.getInventory().offerOrDrop(getSwordStack().copy());
        }
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT_GROUND, 1.0f, 1.5f);
        this.discard();
        return ActionResult.CONSUME;
    }

    @Override
    public boolean canHit() {
        return isStuck() && this.isAlive();
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    /** Never despawn while stuck. */
    @Override
    protected void age() {}

    /** No automatic pickup: swords must be pulled out by hand. */
    @Override
    public void onPlayerCollision(PlayerEntity player) {}

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(Items.IRON_SWORD);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put("Sword", getSwordStack().encode(this.getRegistryManager()));
        nbt.putBoolean("CreativeThrow", creativeThrow);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        ItemStack fallback = getItemStack().copy();
        if (nbt.contains("Sword")) {
            setSwordStack(ItemStack.fromNbt(this.getRegistryManager(), nbt.get("Sword")).orElse(fallback));
        } else {
            setSwordStack(fallback);
        }
        creativeThrow = nbt.getBoolean("CreativeThrow");
    }
}
