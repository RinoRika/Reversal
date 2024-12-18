package cn.stars.reversal.module.impl.render;

import cn.stars.reversal.event.impl.PreMotionEvent;
import cn.stars.reversal.event.impl.Render2DEvent;
import cn.stars.reversal.module.Category;
import cn.stars.reversal.module.Module;
import cn.stars.reversal.module.ModuleInfo;
import cn.stars.reversal.value.impl.NumberValue;
import cn.stars.reversal.util.player.MoveUtil;
import cn.stars.reversal.util.render.RenderUtil;
import cn.stars.reversal.util.render.ThemeType;
import cn.stars.reversal.util.render.ThemeUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

@ModuleInfo(name = "SpeedGraph", chineseName = "速度线", description = "Show your speed on a graph",
        chineseDescription = "在一个图像上渲染你的速度线", category = Category.RENDER)
public class SpeedGraph extends Module {
    private final NumberValue width = new NumberValue("Width", this, 180, 100, 300, 1);
    private final NumberValue height = new NumberValue("Height", this, 5, 1, 20, 1);
    private final NumberValue y = new NumberValue("Y Offset", this, -210, -500, 500, 1);

    private final ArrayList<Float> speeds = new ArrayList<>();
    private double lastVertices;
    private float biggestCock;

    @Override
    public void onUpdateAlwaysInGui() {
        if (lastVertices != 100) {
            synchronized (speeds) {
                speeds.clear();
                biggestCock = 0;
            }
        }

        lastVertices = 100;
    }

    @Override
    public void onPreMotion(final PreMotionEvent event) {
        if (speeds.size() > 100 - 2) {
            speeds.remove(0);
        }

        speeds.add((float) MoveUtil.getSpeed() * mc.timer.timerSpeed);

        biggestCock = -1;
        for (final float f : speeds) {
            if (f > biggestCock) {
                biggestCock = f;
            }
        }
    }

    @Override
    public void onRender2D(final Render2DEvent event) {
        final ScaledResolution sr = new ScaledResolution(mc);

        GL11.glPushMatrix();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GL11.glBegin(GL11.GL_LINES);

        if (speeds.size() > 3) {
            final float width = (float) (sr.getScaledWidth() / 2f - this.width.getValue() / 2f);

            for (int i = 0; i < speeds.size() - 1; i++) {
                RenderUtil.color(ThemeUtil.getThemeColor(i / 10f, ThemeType.GENERAL));
                final float y = (float) (speeds.get(i) * 10 * height.getValue());
                final float y2 = (float) (speeds.get(i + 1) * 10 * height.getValue());
                final float length = (float) (this.width.getValue() / (speeds.size() - 1));

                GL11.glVertex2f(width + (i * length), (float) (sr.getScaledHeight() / 2F - Math.min(y, 50) - this.y.getValue()));
                GL11.glVertex2f(width + ((i + 1) * length), (float) (sr.getScaledHeight() / 2F - Math.min(y2, 50) - this.y.getValue()));
            }
        }
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        RenderUtil.color(Color.WHITE);
        GlStateManager.resetColor();
        GL11.glPopMatrix();

    }

    private void drawRect(double left, double top, double right, double bottom) {
        if (left < right) {
            final double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            final double j = top;
            top = bottom;
            bottom = j;
        }

        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
