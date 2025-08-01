package cn.stars.reversal.module.impl.hud;

import cn.stars.reversal.event.impl.Render2DEvent;
import cn.stars.reversal.event.impl.Shader3DEvent;
import cn.stars.reversal.font.FontManager;
import cn.stars.reversal.font.MFont;
import cn.stars.reversal.module.Category;
import cn.stars.reversal.module.Module;
import cn.stars.reversal.module.ModuleInfo;
import cn.stars.reversal.util.math.MathUtil;
import cn.stars.reversal.util.render.ColorUtil;
import cn.stars.reversal.util.render.ColorUtils;
import cn.stars.reversal.util.render.RenderUtil;
import cn.stars.reversal.util.render.RoundedUtil;
import cn.stars.reversal.value.impl.ColorValue;
import cn.stars.reversal.value.impl.ModeValue;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;

@ModuleInfo(name = "PlayerList", localizedName = "module.PlayerList.name", description = "Show the info of players around you", localizedDescription = "module.PlayerList.desc", category = Category.HUD)
public class PlayerList extends Module {
    public final ModeValue mode = new ModeValue("Mode", this, "Simple", "Modern", "Simple", "Empathy", "ThunderHack", "Shader");
    public final ColorValue colorValue = new ColorValue("Color", this);
    MFont psb = FontManager.getPSB(20);

    public PlayerList() {
        setX(100);
        setY(100);
        setCanBeEdited(true);
    }

    @Override
    public void onShader3D(Shader3DEvent event) {
        float x = getX() + 1;
        float y = getY() + 1;

        switch (mode.getMode()) {
            case "Modern":
                if (event.isBloom()) RenderUtil.roundedRectangle(x, y, 148, 18 + mc.theWorld.playerEntities.size() * regular18.height(), roundStrength, colorValue.getColor());
                else RenderUtil.roundedRectangle(x, y, 148, 18 + mc.theWorld.playerEntities.size() * regular18.height(), roundStrength, Color.BLACK);
                break;
            case "ThunderHack":
                RoundedUtil.drawGradientRound(x - 0.5f, y - 0.5f, 151, 21 + mc.theWorld.playerEntities.size() * regular18.height(), roundStrength,
                        ColorUtils.INSTANCE.interpolateColorsBackAndForth(3, 1000, Color.WHITE, Color.BLACK, true),
                        ColorUtils.INSTANCE.interpolateColorsBackAndForth(3, 2000, Color.WHITE, Color.BLACK, true),
                        ColorUtils.INSTANCE.interpolateColorsBackAndForth(3, 4000, Color.WHITE, Color.BLACK, true),
                        ColorUtils.INSTANCE.interpolateColorsBackAndForth(3, 3000, Color.WHITE, Color.BLACK, true));
                break;
            case "Simple":
                RenderUtil.rect(x, y, 148, 18 + mc.theWorld.playerEntities.size() * regular18.height(), Color.BLACK);
                break;
            case "Shader":
                if (event.isBloom())
                    RenderUtil.rectForShaderTheme(x, y, 148, 18 + mc.theWorld.playerEntities.size() * regular18.height(), colorValue, true);
                else
                    RenderUtil.roundedRectangle(x, y, 148, 18 + mc.theWorld.playerEntities.size() * regular18.height(), roundStrength, Color.BLACK);
                break;
            case "Empathy":
                RenderUtil.roundedRectangle(x, y, 150, 18 + mc.theWorld.playerEntities.size() * regular18.height(), 3f, ColorUtil.empathyGlowColor());
                RenderUtil.roundedRectangle(x - 0.5, y + 2.5, 1.5, psb.height() - 2.5, 1f, colorValue.getColor());
                break;
        }
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        float x = getX() + 1;
        float y = getY() + 1;

        // 背景
        switch (mode.getMode()) {
            case "Modern":
                RenderUtil.roundedRectangle(x, y, 148, 18 + mc.theWorld.playerEntities.size() * regular18.height(), roundStrength, new Color(0, 0, 0, 80));
                RenderUtil.roundedOutlineRectangle(x - 1, y - 1, 150, 20 + mc.theWorld.playerEntities.size() * regular18.height(), roundStrength, 1, colorValue.getColor());
                break;
            case "ThunderHack":
                RoundedUtil.drawGradientRound(x - 0.5f, y - 0.5f, 151, 21 + mc.theWorld.playerEntities.size() * regular18.height(), roundStrength,
                        ColorUtils.INSTANCE.interpolateColorsBackAndForth(3, 1000, Color.WHITE, Color.BLACK, true),
                        ColorUtils.INSTANCE.interpolateColorsBackAndForth(3, 2000, Color.WHITE, Color.BLACK, true),
                        ColorUtils.INSTANCE.interpolateColorsBackAndForth(3, 4000, Color.WHITE, Color.BLACK, true),
                        ColorUtils.INSTANCE.interpolateColorsBackAndForth(3, 3000, Color.WHITE, Color.BLACK, true));
                RoundedUtil.drawRound(x, y, 150, 20 + mc.theWorld.playerEntities.size() * regular18.height(), roundStrength, new Color(0, 0, 0, 220));
                break;
            case "Shader":
                RenderUtil.rectForShaderTheme(x, y, 148, 18 + mc.theWorld.playerEntities.size() * regular18.height(), colorValue, false);
                break;
            case "Simple":
                RenderUtil.rect(x, y, 148, 18 + mc.theWorld.playerEntities.size() * regular18.height(), new Color(0, 0, 0, 80));
                break;
            case "Empathy":
                RenderUtil.roundedRectangle(x, y, 150, 18 + mc.theWorld.playerEntities.size() * regular18.height(), 3f, ColorUtil.empathyColor());
                RenderUtil.roundedRectangle(x - 0.5, y + 2.5, 1.5, psb.height() - 2.5, 1f, colorValue.getColor());
                break;
        }

        regular18Bold.drawString("Player List", x + 16, y + 4.5f, new Color(250, 250, 250, 200).getRGB());
        FontManager.getIcon(20).drawString("d", x + 3, y + 5.5f, colorValue.getColor().getRGB());

        float posY = y + 18;
        for (EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
            regular18.drawString(entityPlayer.getName(), x + 4, posY, new Color(250, 250, 250, 200).getRGB());
            String hp;
            try {
                hp = MathUtil.round(entityPlayer.getHealth(), 1) + "";
                regular18.drawString(hp, x + 138 - regular18.width(hp), posY, new Color(250, 250, 250, 200).getRGB());
                FontManager.getIcon(12).drawString("s", x + 140, posY + 2, new Color(250, 250, 250, 200).getRGB());
            } catch (Exception e) {
                hp = "Unknown";
                regular18.drawString(hp, x + 145 - regular18.width(hp), posY, new Color(250, 250, 250, 200).getRGB());
            }
            posY += regular18.height();
        }

        setWidth(180);
        setHeight(20 + mc.theWorld.playerEntities.size() * regular18.height());
    }
}
