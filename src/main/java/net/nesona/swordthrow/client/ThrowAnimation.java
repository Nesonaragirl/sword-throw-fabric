package net.nesona.swordthrow.client;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public final class ThrowAnimation {
    public enum Type { NONE, THROW, PULL }

    public static final int THROW_LENGTH = 12;
    public static final int PULL_LENGTH = 9;
    public static final int RELEASE_TICK = 4;
    public static final float RELEASE_T = 0.35f;

    private static Type type = Type.NONE;
    private static int ticks, prevTicks;
    private static boolean released;
    private static ItemStack stack = ItemStack.EMPTY;

    private ThrowAnimation() {}

    private static int length() {
        return type == Type.PULL ? PULL_LENGTH : THROW_LENGTH;
    }

    public static void start(Type t) {
        start(t, ItemStack.EMPTY);
    }

    public static void start(Type t, ItemStack held) {
        type = t;
        stack = held;
        ticks = 0;
        prevTicks = 0;
        released = false;
    }

    public static void tick() {
        released = false;
        if (type == Type.NONE) return;
        prevTicks = ticks;
        ticks++;
        if (type == Type.THROW && ticks == RELEASE_TICK) released = true;
        if (ticks > length()) {
            type = Type.NONE;
            stack = ItemStack.EMPTY;
        }
    }

    public static Type type() {
        return type;
    }

    public static ItemStack heldStack() {
        return stack;
    }

    /** True for exactly one tick: the moment the sword leaves the hand. */
    public static boolean released() {
        return released;
    }

    public static float progress(float tickDelta) {
        if (type == Type.NONE) return 0f;
        return MathHelper.clamp(MathHelper.lerp(tickDelta, prevTicks, ticks) / length(), 0f, 1f);
    }

    /** Camera pitch offset in degrees (negative = look up). */
    public static float cameraPitch(float tickDelta) {
        if (type == Type.NONE) return 0f;
        float t = progress(tickDelta);
        if (type == Type.THROW) {
            if (t < RELEASE_T) return -4f * (t / RELEASE_T);
            float v = (t - RELEASE_T) / (1f - RELEASE_T);
            return MathHelper.lerp(v, -4f, 0f) + 7f * MathHelper.sin((float) Math.PI * v);
        }
        return -3f * MathHelper.sin((float) Math.PI * t);
    }

    /** FOV offset in degrees: zoom in on wind-up, punch out on release. */
    public static float fovOffset(float tickDelta) {
        if (type == Type.NONE) return 0f;
        float t = progress(tickDelta);
        if (type == Type.THROW) {
            if (t < RELEASE_T) return -5f * (t / RELEASE_T);
            float v = (t - RELEASE_T) / (1f - RELEASE_T);
            return MathHelper.lerp(v, -5f, 0f) + 10f * MathHelper.sin((float) Math.PI * v);
        }
        return 4f * MathHelper.sin((float) Math.PI * t);
    }
}
