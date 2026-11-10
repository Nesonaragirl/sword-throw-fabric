package net.nesona.swordthrow.client;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

/**
 * Our own two-bone first-person arm (upper arm + forearm) textured with the player's skin.
 * Local model space: +Y runs along the arm (shoulder -> fist). We rotate it by -90 deg around X
 * so +Y points forward (-Z in camera space); shoulder pitch/yaw/roll are applied in camera space
 * before that, and the elbow bends the forearm around the model X axis (positive = hand curls up).
 */
public final class FirstPersonRig {
    // Shoulder position in camera space (x right, y up, -z forward). Tweak these to move the arm.
    private static final double SHOULDER_X = 0.58, SHOULDER_Y = -0.75, SHOULDER_Z = 0.05;
    /** Added to the animation's shoulder pitch so the resting arm points slightly up/forward. */
    private static final float REST_PITCH = 5f;
    /** Distance from forearm pivot to the middle of the fist, in blocks. */
    private static final float FIST = 5f / 16f;

    private static final class Rig {
        final ModelPart root, upper, fore;

        Rig(boolean slim) {
            float w = slim ? 3f : 4f;
            ModelData data = new ModelData();
            ModelPartData root = data.getRoot();
            ModelPartData upper = root.addChild("upper",
                    ModelPartBuilder.create().uv(40, 16).cuboid(-w / 2f, 0f, -2f, w, 6f, 4f),
                    ModelTransform.NONE);
            upper.addChild("fore",
                    ModelPartBuilder.create().uv(40, 22).cuboid(-w / 2f, 0f, -2f, w, 6f, 4f),
                    ModelTransform.pivot(0f, 6f, 0f));
            this.root = TexturedModelData.of(data, 64, 64).createModel();
            this.upper = this.root.getChild("upper");
            this.fore = this.upper.getChild("fore");
        }
    }

    private static Rig wide, slim;

    private FirstPersonRig() {}

    public static void render(HeldItemRenderer held, AbstractClientPlayerEntity player, ItemStack stack,
                              MatrixStack m, VertexConsumerProvider vcp, int light, float tickDelta) {
        SkinTextures skin = player.getSkinTextures();
        boolean isSlim = skin.model() == SkinTextures.Model.SLIM;
        Rig rig;
        if (isSlim) {
            if (slim == null) slim = new Rig(true);
            rig = slim;
        } else {
            if (wide == null) wide = new Rig(false);
            rig = wide;
        }

        float[] p = ThrowAnimation.handPose(tickDelta);
        rig.fore.pitch = (float) Math.toRadians(p[6]);

        m.push();
        m.translate(SHOULDER_X + p[0], SHOULDER_Y + p[1], SHOULDER_Z + p[2]);
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(p[4]));
        m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p[3] + REST_PITCH));
        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(p[5]));
        m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90f));

        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntitySolid(skin.texture()));
        rig.upper.render(m, vc, light, OverlayTexture.DEFAULT_UV);

        if (!stack.isEmpty()) {
            m.push();
            rig.upper.rotate(m);
            rig.fore.rotate(m);
            m.translate(0f, FIST, 0f);
            // Cancel the rig's own rotations so the item keeps vanilla's first-person orientation.
            m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-p[6]));
            m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f));
            held.renderItem(player, stack, ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, false, m, vcp, light);
            m.pop();
        }
        m.pop();
    }
}
