package cn.stars.reversal.module.impl.client;

import cn.stars.addons.rawinput.RawMouseHelper;
import cn.stars.reversal.event.impl.ValueChangedEvent;
import cn.stars.reversal.module.Category;
import cn.stars.reversal.module.Module;
import cn.stars.reversal.module.ModuleInfo;
import cn.stars.reversal.ui.atmoic.mainmenu.AtomicMenu;
import cn.stars.reversal.ui.atmoic.mainmenu.impl.MainGui;
import cn.stars.reversal.ui.atmoic.mainmenu.impl.BocchiMainGui;
import cn.stars.reversal.util.ReversalLogger;
import cn.stars.reversal.util.render.ThemeUtil;
import cn.stars.reversal.value.impl.*;
import net.minecraft.util.MouseHelper;
import net.optifine.Lang;

import java.awt.*;

@ModuleInfo(name = "ClientSettings", localizedName = "module.ClientSettings.name", description = "Some settings to change your hud.",
        localizedDescription = "module.ClientSettings.desc", category = Category.CLIENT)
public final class ClientSettings extends Module {
    public final NoteValue note1 = new NoteValue("< Color Settings >", "value.ClientSettings.note1", this);
    public final ModeValue colorType = new ModeValue("Color Type", "value.ClientSettings.colorType", this, "Rainbow", "Rainbow", "Double", "Fade", "Static");
    public final ColorValue color1 = new ColorValue("Color 1", "value.ClientSettings.color1", this, new Color(20,250,255), true);
    public final ColorValue color2 = new ColorValue("Color 2", "value.ClientSettings.color2", this, new Color(20,250,255), true);
    public final BoolValue customAlpha = new BoolValue("Custom Alpha", "value.ClientSettings.customAlpha", this, false);
    public final NumberValue alpha = new NumberValue("Alpha", "value.ClientSettings.alpha", this, 255, 0, 255, 1);
    public final NumberValue indexTimes = new NumberValue("Index Times",  "value.ClientSettings.indexTimes",this, 1, 1, 10, 0.1);
    public final NumberValue indexSpeed = new NumberValue("Index Speed", "value.ClientSettings.indexSpeed",this, 1, 1, 5, 0.1);

    public final NoteValue note2 = new NoteValue("< Specific Settings >", "value.ClientSettings.note2", this);
    public final ModeValue listAnimation = new ModeValue("List Animation", "value.ClientSettings.listAnimation", this, "Reversal", "Reversal", "Slide");
    public final BoolValue empathyGlow = new BoolValue("Empathy Glow", "value.ClientSettings.empathyGlow", this, false);
    public final NumberValue roundStrength = new NumberValue("Round Strength", "value.ClientSettings.roundStrength", this, 4f, 1f, 8f, 1f);
    public final BoolValue shaderGradient = new BoolValue("Shader Gradient", "value.ClientSettings.shaderGradient", this, false);
    public final BoolValue shaderBackground = new BoolValue("Shader Background", "value.ClientSettings.shaderBackground", this, false);

    public final NoteValue note3 = new NoteValue("< Client Settings >", "value.ClientSettings.note3", this);
    public final TextValue customName = new TextValue("Custom Name", "value.ClientSettings.customName", this, "Reversal");
    public final BoolValue localization = new BoolValue("Localization", "value.ClientSettings.localization", this, true);
    public final ModeValue language = new ModeValue("Language", "value.ClientSettings.language", this, "English", "English", "Chinese", "Chinese Traditional", "Japanese");
    public final BoolValue showNotifications = new BoolValue("Show Notifications", "value.ClientSettings.showNotifications", this, true);
    public final BoolValue hudTextWithBracket = new BoolValue("Hud Text With Bracket", "value.ClientSettings.hudTextWithBracket", this, false);
    public final BoolValue clientMsgCustomName = new BoolValue("Client Message Custom Name", "value.ClientSettings.clientMsgCustomName", this, false);
    public final BoolValue modernUISound = new BoolValue("Modern UI Sound", "value.ClientSettings.modernUISound", this, false);
    public final ModeValue mainMenu = new ModeValue("Main Menu", "value.ClientSettings.mainMenu", this, "Osu!Lazer", "Osu!Lazer", "Bocchi");

    public final NoteValue note4 = new NoteValue("< Minecraft Settings >", "value.ClientSettings.note4", this);
    public final BoolValue loadingScreenBg = new BoolValue("Loading Screen Background", "value.ClientSettings.loadingScreenBg", this, false);
    public final BoolValue rawInput = new BoolValue("Raw Input", "value.ClientSettings.rawInput", this, false);

    public ClientSettings() {
    }

    public boolean isNonAlphabeticLanguage() {
        return language.getMode().equals("Chinese") || language.getMode().equals("Japanese") || language.getMode().equals("Chinese Traditional");
    }

    @Override
    public void onUpdateAlways() {
        alpha.hidden = !customAlpha.enabled;
        language.hidden = !localization.enabled;

        checkClientModuleState();
    }

    @Override
    public void onValueChanged(ValueChangedEvent event) {
        // For Raw Input
        if (event.setting == rawInput) {
            if (rawInput.enabled) {
                mc.mouseHelper = new RawMouseHelper();
                ReversalLogger.info("Switched mc.mouseHelper to RawMouseHelper.");
            }
            else {
                mc.mouseHelper = new MouseHelper();
                ReversalLogger.info("Switched mc.mouseHelper to MouseHelper.");
            }
        }
        if (event.setting == language) {
            Lang.resourcesReloaded();
        }
        if (event.setting == customName) {
            ThemeUtil.setCustomClientName(customName.getText());
        }
        if (event.setting == mainMenu) {
            if (mainMenu.getMode().equals("Osu!Lazer")) {
                AtomicMenu.setGui(0, new MainGui());
            } else {
                AtomicMenu.setGui(0, new BocchiMainGui());
            }
        }
    }

    @Override
    public void onLoad() {
        if (rawInput.enabled) {
            mc.mouseHelper = new RawMouseHelper();
            ReversalLogger.info("Switched mc.mouseHelper to RawMouseHelper.");
        }
        else {
            mc.mouseHelper = new MouseHelper();
            ReversalLogger.info("Switched mc.mouseHelper to MouseHelper.");
        }
        customName.setText(ThemeUtil.getCustomClientName());
        if (mainMenu.getMode().equals("Osu!Lazer")) {
            AtomicMenu.setGui(0, new MainGui());
        } else {
            AtomicMenu.setGui(0, new BocchiMainGui());

        }
    }
}
