package net.minecraft.client.gui;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import cn.stars.reversal.GameInstance;
import cn.stars.reversal.font.FontManager;
import cn.stars.reversal.util.Transformer;
import cn.stars.reversal.ui.curiosity.CuriosityTextButton;
import cn.stars.reversal.ui.gui.GuiMainMenu;
import cn.stars.reversal.util.render.RenderUtil;
import cn.stars.reversal.util.render.RoundedUtil;
import cn.stars.reversal.util.shader.RiseShaders;
import cn.stars.reversal.util.shader.base.ShaderRenderType;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IChatComponent;

import static cn.stars.reversal.GameInstance.*;

public class GuiDisconnected extends GuiScreen
{
    private String reason;
    private IChatComponent message;
    private List<String> multilineMessage;
    private final GuiScreen parentScreen;
    private int field_175353_i;
    private CuriosityTextButton reconnectButton, cancelButton;
    private CuriosityTextButton[] buttons;

    public GuiDisconnected(GuiScreen screen, String reasonLocalizationKey, IChatComponent chatComp)
    {
        this.parentScreen = screen;
        this.reason = I18n.format(reasonLocalizationKey);
        this.message = chatComp;
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    }

    public void initGui()
    {
        this.buttonList.clear();
        this.multilineMessage = this.fontRendererObj.listFormattedStringToWidth(this.message.getFormattedText(), this.width - 50);
        this.field_175353_i = (int) (this.multilineMessage.size() * regular16.height());
        reconnectButton = new CuriosityTextButton(this.width / 2 - 100, this.height / 2 + this.field_175353_i / 2 + regular16.height(), 200, 20, () -> this.mc.displayGuiScreen(new GuiMultiplayer(Transformer.transformMainMenu())), "返回主菜单", "", true, 1, 75, 5, 20);
        cancelButton = new CuriosityTextButton(this.width / 2 - 100, this.height / 2 + this.field_175353_i / 2 + regular16.height() + 25, 200, 20, () -> this.mc.displayGuiScreen(new GuiConnecting(new GuiMultiplayer(new GuiMainMenu()), mc, mc.getCurrentServerData())), "重连", "", true, 1, 90, 5, 20);
        buttons = new CuriosityTextButton[]{reconnectButton, cancelButton};
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            for (CuriosityTextButton menuButton : this.buttons) {
                if (RenderUtil.isHovered(menuButton.getX(), menuButton.getY(), menuButton.getWidth(), menuButton.getHeight(), mouseX, mouseY)) {
                    mc.getSoundHandler().playButtonPress();
                    menuButton.runAction();
                    break;
                }
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        try {
            float maxLength = Math.max(regular24Bold.width(this.reason), 300);
            this.drawDefaultBackground();
            // blur
            RiseShaders.GAUSSIAN_BLUR_SHADER.update();
            RiseShaders.GAUSSIAN_BLUR_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, NORMAL_BLUR_RUNNABLES);

            // bloom
            RiseShaders.POST_BLOOM_SHADER.update();
            RiseShaders.POST_BLOOM_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, NORMAL_POST_BLOOM_RUNNABLES);

            GameInstance.clearRunnables();

            if (this.multilineMessage != null) {
                for (String s : this.multilineMessage) {
                    maxLength = Math.max(maxLength, regular20.width(s));
                }
            }

            RoundedUtil.drawRound(width / 2f - 125, 10, 250, 40, 6, new Color(30, 30, 30, 160));
            RoundedUtil.drawRound(this.width / 2 - maxLength / 2 - 30, this.height / 2 - this.field_175353_i / 2 - regular20.height() * 2 - 10, maxLength + 60, 100 + 10 * multilineMessage.size(), 4, new Color(30, 30, 30, 160));
            RenderUtil.rect(this.width / 2 - maxLength / 2 - 30, this.height / 2 - this.field_175353_i / 2 - regular20.height() * 2 + 10, maxLength + 60, 0.5, new Color(220, 220, 220, 240));

            float finalMaxLength = maxLength;
            GameInstance.NORMAL_BLUR_RUNNABLES.add(() -> {
                RoundedUtil.drawRound(this.width / 2 - finalMaxLength / 2 - 30, this.height / 2 - this.field_175353_i / 2 - regular20.height() * 2 - 10, finalMaxLength + 60, 100 + 10 * multilineMessage.size(), 4, Color.BLACK);
                RoundedUtil.drawRound(width / 2f - 125, 10, 250, 40, 6, Color.BLACK);
            });

            for (CuriosityTextButton button : buttons) {
                button.draw(mouseX, mouseY, partialTicks);
            }

            regular24Bold.drawCenteredString(this.reason, this.width / 2, this.height / 2 - this.field_175353_i / 2 - regular20.height() * 2 - 5, new Color(220, 220, 220, 240).getRGB());
            int i = this.height / 2 - this.field_175353_i / 2;

            if (this.multilineMessage != null) {
                for (String s : this.multilineMessage) {
                    regular20.drawCenteredString(s, this.width / 2, i, new Color(220, 220, 220, 240).getRGB());
                    maxLength = Math.max(maxLength, regular20.width(s));
                    i += regular20.height();
                }
            }

            String ip = "Unknown";

            final ServerData serverData = mc.getCurrentServerData();
            if(serverData != null)
                ip = "IP: " + serverData.serverIP;

            FontManager.getSpecialIcon(80).drawString("b", width / 2f - 115, 17, new Color(220, 220, 220, 240).getRGB());
            GameInstance.regular24Bold.drawString("SERVER INFORMATION", width / 2f - 55, 18, new Color(220, 220, 220, 240).getRGB());
            GameInstance.regular20.drawString(ip, width / 2f - 55, 34, new Color(220, 220, 220, 240).getRGB());

            regular20.drawString("Open Source PVP Client By Stars.", 4, height - 30, new Color(220, 220, 220, 240).getRGB());
            regular20.drawString("https://www.github.com/RinoRika/Reversal", 4, height - 20, new Color(220, 220, 220, 240).getRGB());
            regular20.drawString(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")), 4, height - 10, new Color(220, 220, 220, 240).getRGB());

            UI_BLOOM_RUNNABLES.forEach(Runnable::run);
            UI_BLOOM_RUNNABLES.clear();

            super.drawScreen(mouseX, mouseY, partialTicks);
        } catch (StackOverflowError error) {

        }
    }
}
