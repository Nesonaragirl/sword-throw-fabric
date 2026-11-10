package net.nesona.swordthrow.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.nesona.swordthrow.ThrownSwordEntity;

public class ThrownSwordRenderer extends EntityRenderer<ThrownSwordEntity> {
    private final ItemRenderer itemRenderer;

    public ThrownSwordRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(ThrownSwordEntity e, float yaw, float tickDelta, MatrixStack m,
                       VertexConsumerProvider vcp, int light) {
        m.push();
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, e.prevYaw, e.getYaw()) - 90.0f));
        // Sword sprite points up-right (45 deg); rotate so the tip leads along the flight direction.
        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, e.prevPitch, e.getPitch()) - 45.0f));
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f)); // undo FIXED mode mirror
        m.scale(1.4f, 1.4f, 1.4f);
        this.itemRenderer.renderItem(e.getSwordStack(), ModelTransformationMode.FIXED, light,
                OverlayTexture.DEFAULT_UV, m, vcp, e.getWorld(), e.getId());
        m.pop();
        super.render(e, yaw, tickDelta, m, vcp, light);
    }

    @Override
    public Identifier getTexture(ThrownSwordEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }
}
