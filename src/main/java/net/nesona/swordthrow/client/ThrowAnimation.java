package net.nesona.swordthrow.client;

import net.minecraft.util.math.MathHelper;

public final class ThrowAnimation {
    public enum Type { NONE, THROW, PULL }

    private static Type type = Type.NONE;
    private static int ticks, prevTicks;

    private ThrowAnimation() {}

    private static int length() {
        return type == Type.PULL ? 8 : 9;
    }

    public static void start(Type t) {
        type = t;
        ticks = 0;
        prevTicks = 0;
    }

    public static void tick() {
        if (type == Type.NONE) return;
        prevTicks = ticks;
        ticks++;
        if (ticks > length()) type = Type.NONE;
    }

    public static Type type() {
        return type;
    }

    /** 0..1 progress of the running animation. */
    public static float progress(float tickDelta) {
        if (type == Type.NONE) return 0f;
        return MathHelper.clamp(MathHelper.lerp(tickDelta, prevTicks, ticks) / length(), 0f, 1f);
    }
}
