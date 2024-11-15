package net.minecraft.client.gui;

import cn.stars.reversal.GameInstance;
import cn.stars.reversal.music.ui.TextField;
import cn.stars.reversal.music.ui.ThemeColor;
import com.google.common.base.Predicate;
import java.io.IOException;
import java.net.IDN;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

public class GuiScreenAddServer extends GuiScreen
{
    private final GuiScreen parentScreen;
    private final ServerData serverData;
    private TextField serverIPField;
    private TextField serverNameField;
    private GuiButton serverResourcePacks;
    private Predicate<String> field_181032_r = new Predicate<String>()
    {
        public boolean apply(String p_apply_1_)
        {
            if (p_apply_1_.length() == 0)
            {
                return true;
            }
            else
            {
                String[] astring = p_apply_1_.split(":");

                if (astring.length == 0)
                {
                    return true;
                }
                else
                {
                    try
                    {
                        IDN.toASCII(astring[0]);
                        return true;
                    }
                    catch (IllegalArgumentException var4)
                    {
                        return false;
                    }
                }
            }
        }
    };

    public GuiScreenAddServer(GuiScreen p_i1033_1_, ServerData p_i1033_2_)
    {
        this.parentScreen = p_i1033_1_;
        this.serverData = p_i1033_2_;
    }

    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 18, I18n.format("addServer.add", new Object[0])));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + 18, I18n.format("gui.cancel", new Object[0])));
        this.buttonList.add(this.serverResourcePacks = new GuiButton(2, this.width / 2 - 100, this.height / 4 + 72, I18n.format("addServer.resourcePack", new Object[0]) + ": " + this.serverData.getResourceMode().getMotd().getFormattedText()));
        this.serverNameField = new TextField(200, 20, GameInstance.regular16, ThemeColor.bgColor, ThemeColor.outlineColor);
        this.serverNameField.setFocused(true);
        this.serverNameField.setText(this.serverData.serverName);
        this.serverIPField = new TextField(200, 20, GameInstance.regular16, ThemeColor.bgColor, ThemeColor.outlineColor);
        this.serverIPField.setText(this.serverData.serverIP);
        ((GuiButton)this.buttonList.get(0)).enabled = !this.serverIPField.getText().isEmpty() && this.serverIPField.getText().split(":").length > 0 && !this.serverNameField.getText().isEmpty();
    }

    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 2)
            {
                this.serverData.setResourceMode(ServerData.ServerResourceMode.values()[(this.serverData.getResourceMode().ordinal() + 1) % ServerData.ServerResourceMode.values().length]);
                this.serverResourcePacks.displayString = I18n.format("addServer.resourcePack", new Object[0]) + ": " + this.serverData.getResourceMode().getMotd().getFormattedText();
            }
            else if (button.id == 1)
            {
                this.parentScreen.confirmClicked(false, 0);
            }
            else if (button.id == 0)
            {
                this.serverData.serverName = this.serverNameField.getText();
                this.serverData.serverIP = this.serverIPField.getText();
                this.parentScreen.confirmClicked(true, 0);
            }
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        this.serverNameField.keyTyped(typedChar, keyCode);
        this.serverIPField.keyTyped(typedChar, keyCode);

        if (keyCode == 15)
        {
            this.serverNameField.setFocused(!this.serverNameField.isFocused());
            this.serverIPField.setFocused(!this.serverIPField.isFocused());
        }

        if (keyCode == 28 || keyCode == 156)
        {
            this.actionPerformed((GuiButton)this.buttonList.get(0));
        }

        ((GuiButton)this.buttonList.get(0)).enabled = this.serverIPField.getText().length() > 0 && this.serverIPField.getText().split(":").length > 0 && this.serverNameField.getText().length() > 0;
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.serverIPField.mouseClicked(mouseX, mouseY, mouseButton);
        this.serverNameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        this.serverIPField.mouseDragged(mouseX, mouseY, clickedMouseButton);
        this.serverNameField.mouseDragged(mouseX, mouseY, clickedMouseButton);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, I18n.format("addServer.title"), this.width / 2, 17, 16777215);
        this.drawString(this.fontRendererObj, I18n.format("addServer.enterName"), this.width / 2 - 100, 53, 10526880);
        this.drawString(this.fontRendererObj, I18n.format("addServer.enterIp"), this.width / 2 - 100, 94, 10526880);
        this.serverNameField.draw(this.width / 2 - 100, 66, mouseX, mouseY);
        this.serverIPField.draw(this.width / 2 - 100, 106, mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
