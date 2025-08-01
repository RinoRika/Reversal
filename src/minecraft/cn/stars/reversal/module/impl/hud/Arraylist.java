package cn.stars.reversal.module.impl.hud;

import cn.stars.reversal.module.Category;
import cn.stars.reversal.module.Module;
import cn.stars.reversal.module.ModuleInfo;
import cn.stars.reversal.value.impl.BoolValue;
import cn.stars.reversal.value.impl.ColorValue;
import cn.stars.reversal.value.impl.ModeValue;
import net.minecraft.client.gui.ScaledResolution;

@ModuleInfo(name = "Arraylist", localizedName = "module.Arraylist.name", description = "Show the modules you enabled",
        localizedDescription = "module.Arraylist.desc", category = Category.HUD)
public class Arraylist extends Module {
    public final ModeValue mode = new ModeValue("Mode", this, "Simple", "Minecraft", "Modern", "Simple", "Empathy", "ThunderHack", "Shader");
    public final ColorValue colorValue = new ColorValue("Color", this);
    public final BoolValue noRenderModules = new BoolValue("No Render Modules", this, false);
    final ScaledResolution SR = new ScaledResolution(mc);
    final float offset = 6;
    final float arraylistX = SR.getScaledWidth() - offset;

    public Arraylist() {
        setCanBeEdited(true);
        setX((int) arraylistX);
    }
}
