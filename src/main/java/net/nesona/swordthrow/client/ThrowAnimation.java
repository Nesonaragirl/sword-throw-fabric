package net.nesona.swordthrow.client;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

/**
 * Keyframed first-person animation for our own custom arm rig (see FirstPersonRig).
 * Pose = {shoulderTx, ty, tz, shoulderPitch, yaw, roll, elbow} (degrees; elbow is absolute, 0 = straight).
 */
public final class ThrowAnimation {
    public enum Type { NONE, THROW, PULL }

    public static final int THROW_LENGTH = 14;
    public static final int PULL_LENGTH = 10;
    /** Tick at which the sword leaves the hand and the throw packet is sent. */
    public static final int RELEASE_TICK = 8;
    public static final float RELEASE_T = RELEASE_TICK / (float) THROW_LENGTH;
    public static final float REST_ELBOW = 30f;

    private enum Ease { LINEAR, IN, OUT, IN_OUT }

    private record Key(float t, float tx, float ty, float tz, float rx, float ry, float rz, float elbow, Ease ease) {}

    private record CamKey(float t, float v, Ease ease) {}

    private static final Key[] THROW_KEYS = {
            new Key(0.00f,  0.00f,  0.00f,  0.00f,   0f,   0f,  0f, 30f, Ease.LINEAR),
            new Key(0.10f,  0.00f, -0.04f,  0.04f,  -6f,   4f,  0f, 38f, Ease.OUT),    // anticipation dip
            new Key(0.40f, -0.08f,  0.04f,  0.12f,  20f,  14f, -6f, 45f, Ease.IN_OUT), // draw back, elbow folds
            new Key(0.48f, -0.10f,  0.05f,  0.16f,  24f,  16f, -8f, 50f, Ease.LINEAR), // coil
            new Key(0.59f,  0.00f, -0.02f, -0.06f,   6f,  -6f,  4f,  5f, Ease.IN),     // snap: elbow straightens
            new Key(0.74f,  0.00f, -0.10f, -0.10f, -18f, -10f,  6f,  0f, Ease.OUT),    // follow-through
            new Key(1.00f,  0.00f,  0.00f,  0.00f,   0f,   0f,  0f, 30f, Ease.IN_OUT) // recover
    };

    private static final Key[] PULL_KEYS = {
            new Key(0.00f,  0.00f,  0.00f,  0.00f,   0f, 0f,  0f, 30f, Ease.LINEAR),
            new Key(0.30f,  0.02f,  0.04f,  0.18f,  12f, 0f, -6f, 60f, Ease.OUT),    // grab, draw in
            new Key(0.50f,  0.00f,  0.02f,  0.24f,  16f, 0f, -8f, 70f, Ease.LINEAR), // strain
            new Key(0.70f, -0.02f, -0.04f, -0.04f,   0f, 2f,  2f, 22f, Ease.IN),     // pops free
            new Key(1.00f,  0.00f,  0.00f,  0.00f,   0f, 0f,  0f, 30f, Ease.IN_OUT)
    };

    private static final CamKey[] THROW_PITCH = {
            new CamKey(0.00f, 0f, Ease.LINEAR), new CamKey(0.40f, -3f, Ease.IN_OUT),
            new CamKey(0.50f, -4f, Ease.LINEAR), new CamKey(0.64f, 6f, Ease.IN),
            new CamKey(0.80f, 2f, Ease.OUT), new CamKey(1.00f, 0f, Ease.IN_OUT)
    };
    private static final CamKey[] THROW_FOV = {
            new CamKey(0.00f, 0f, Ease.LINEAR), new CamKey(0.40f, -6f, Ease.IN_OUT),
            new CamKey(0.50f, -7f, Ease.LINEAR), new CamKey(0.64f, 9f, Ease.IN),
            new CamKey(0.85f, 2f, Ease.OUT), new CamKey(1.00f, 0f, Ease.IN_OUT)
    };
    private static final CamKey[] PULL_PITCH = {
            new CamKey(0.00f, 0f, Ease.LINEAR), new CamKey(0.45f, -3f, Ease.OUT),
            new CamKey(0.70f, 3f, Ease.IN), new CamKey(1.00f, 0f, Ease.IN_OUT)
    };
    private static final CamKey[] PULL_FOV = {
            new CamKey(0.00f, 0f, Ease.LINEAR), new CamKey(0.45f, -3f, Ease.OUT),
            new CamKey(0.70f, 5f, Ease.IN), new CamKey(1.00f, 0f, Ease.IN_OUT)
    };

    private static final float[] REST = {0, 0, 0, 0, 0, 0, REST_ELBOW};

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

    /** {tx, ty, tz, shoulderPitch, yaw, roll, elbow} for the current frame. */
    public static float[] handPose(float tickDelta) {
        if (type == Type.NONE) return REST;
        return samplePose(type == Type.THROW ? THROW_KEYS : PULL_KEYS, progress(tickDelta));
    }

    /** Camera pitch offset in degrees (negative = look up). */
    public static float cameraPitch(float tickDelta) {
        if (type == Type.NONE) return 0f;
        return sampleCam(type == Type.THROW ? THROW_PITCH : PULL_PITCH, progress(tickDelta));
    }

    public static float fovOffset(float tickDelta) {
        if (type == Type.NONE) return 0f;
        return sampleCam(type == Type.THROW ? THROW_FOV : PULL_FOV, progress(tickDelta));
    }

    private static float ease(Ease e, float u) {
        return switch (e) {
            case LINEAR -> u;
            case IN -> u * u;
            case OUT -> 1f - (1f - u) * (1f - u);
            case IN_OUT -> u * u * (3f - 2f * u);
        };
    }

    private static float[] samplePose(Key[] keys, float t) {
        for (int i = 1; i < keys.length; i++) {
            Key a = keys[i - 1], b = keys[i];
            if (t <= b.t || i == keys.length - 1) {
                float span = b.t - a.t;
                float u = span <= 0f ? 1f : MathHelper.clamp((t - a.t) / span, 0f, 1f);
                u = ease(b.ease, u);
                return new float[] {
                        MathHelper.lerp(u, a.tx, b.tx), MathHelper.lerp(u, a.ty, b.ty), MathHelper.lerp(u, a.tz, b.tz),
                        MathHelper.lerp(u, a.rx, b.rx), MathHelper.lerp(u, a.ry, b.ry), MathHelper.lerp(u, a.rz, b.rz),
                        MathHelper.lerp(u, a.elbow, b.elbow)
                };
            }
        }
        return REST;
    }

    private static float sampleCam(CamKey[] keys, float t) {
        for (int i = 1; i < keys.length; i++) {
            CamKey a = keys[i - 1], b = keys[i];
            if (t <= b.t || i == keys.length - 1) {
                float span = b.t - a.t;
                float u = span <= 0f ? 1f : MathHelper.clamp((t - a.t) / span, 0f, 1f);
                return MathHelper.lerp(ease(b.ease, u), a.v, b.v);
            }
        }
        return 0f;
    }
}
