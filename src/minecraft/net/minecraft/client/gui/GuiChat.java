package net.minecraft.client.gui;

import cn.stars.reversal.Reversal;
import cn.stars.reversal.font.FontManager;
import cn.stars.reversal.font.MFont;
import cn.stars.reversal.module.Category;
import cn.stars.reversal.module.Module;
import cn.stars.reversal.module.impl.hud.TargetHUD;
import cn.stars.reversal.util.render.RenderUtil;
import cn.stars.reversal.util.render.RenderUtils;
import cn.stars.reversal.util.shader.round.RoundedUtils;
import com.google.common.collect.Lists;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GuiChat extends GuiScreen
{
    MFont gs = FontManager.getPSR(18);
    private static final Logger logger = LogManager.getLogger();
    private String historyBuffer = "";
    private int sentHistoryCursor = -1;
    private boolean playerNamesFound;
    private boolean waitingOnAutocomplete;
    private int autocompleteIndex;
    private List<String> foundPlayerNames = Lists.<String>newArrayList();
    protected GuiTextField inputField;
    private String defaultInputFieldText = "";

    public GuiChat()
    {
    }

    public GuiChat(String defaultText)
    {
        this.defaultInputFieldText = defaultText;
    }

    public void initGui()
    {
        TargetHUD.target = mc.thePlayer;
        Keyboard.enableRepeatEvents(true);
        this.sentHistoryCursor = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
        this.inputField = new GuiTextField(0, this.fontRendererObj, 4, this.height - 12, this.width - 4, 12);
        this.inputField.setMaxStringLength(32767);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused(true);
        this.inputField.setText(this.defaultInputFieldText);
        this.inputField.setCanLoseFocus(false);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for(Module m : Reversal.moduleManager.moduleList){
            m.setDragging(false);
        }
    }

    public void onGuiClosed()
    {
        TargetHUD.target = null;
        for(Module m : Reversal.moduleManager.moduleList){
            m.setDragging(false);
        }
        Keyboard.enableRepeatEvents(false);
        this.mc.ingameGUI.getChatGUI().resetScroll();
    }

    public void updateScreen()
    {
        this.inputField.updateCursorCounter();
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        this.waitingOnAutocomplete = false;

        if (keyCode == 15)
        {
            this.autocompletePlayerNames();
        }
        else
        {
            this.playerNamesFound = false;
        }

        if (keyCode == 1)
        {
            this.mc.displayGuiScreen((GuiScreen)null);
        }
        else if (keyCode != 28 && keyCode != 156)
        {
            if (keyCode == 200)
            {
                this.getSentHistory(-1);
            }
            else if (keyCode == 208)
            {
                this.getSentHistory(1);
            }
            else if (keyCode == 201)
            {
                this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().getLineCount() - 1);
            }
            else if (keyCode == 209)
            {
                this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().getLineCount() + 1);
            }
            else
            {
                this.inputField.textboxKeyTyped(typedChar, keyCode);
            }
        }
        else
        {
            String s = this.inputField.getText().trim();

            if (s.length() > 0)
            {
                this.sendChatMessage(s);
            }

            this.mc.displayGuiScreen((GuiScreen)null);
        }
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if (i != 0)
        {
            if (i > 1)
            {
                i = 1;
            }

            if (i < -1)
            {
                i = -1;
            }

            if (!isShiftKeyDown())
            {
                i *= 7;
            }

            this.mc.ingameGUI.getChatGUI().scroll(i);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (mouseButton == 0)
        {
            IChatComponent ichatcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

            if (this.handleComponentClick(ichatcomponent))
            {
                return;
            }
        }
        for(Module m : Reversal.moduleManager.moduleList) {
            if(m.isEnabled() && m.getModuleInfo().category().equals(Category.HUD) && m.isCanBeEdited()) {

                boolean isInside = RenderUtils.isInside(mouseX, mouseY, m.getX() + m.getAdditionalWidth(), m.getY() + m.getAdditionalHeight(), m.getWidth(), m.getHeight()) &&
                        Reversal.moduleManager.moduleList.stream().filter(m2 -> m2.isEnabled() && m2.getModuleInfo().category().equals(Category.HUD) && mouseX >= m2.getX() + m.getAdditionalWidth() && mouseX <= m2.getX() + m.getAdditionalWidth() + m2.getWidth() && mouseY >= m2.getY() + m.getAdditionalHeight() && mouseY <= m2.getY() + m.getAdditionalHeight() + m2.getHeight()).findFirst().get().equals(m);

                if(isInside) {
                    m.setDragging(true);
                    m.setDraggingX(m.getX() - mouseX);
                    m.setDraggingY(m.getY() - mouseY);
                }
            }
        }
        this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void setText(String newChatText, boolean shouldOverwrite)
    {
        if (shouldOverwrite)
        {
            this.inputField.setText(newChatText);
        }
        else
        {
            this.inputField.writeText(newChatText);
        }
    }

    public void autocompletePlayerNames()
    {
        if (this.playerNamesFound)
        {
            this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());

            if (this.autocompleteIndex >= this.foundPlayerNames.size())
            {
                this.autocompleteIndex = 0;
            }
        }
        else
        {
            int i = this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false);
            this.foundPlayerNames.clear();
            this.autocompleteIndex = 0;
            String s = this.inputField.getText().substring(i).toLowerCase();
            String s1 = this.inputField.getText().substring(0, this.inputField.getCursorPosition());
            this.sendAutocompleteRequest(s1, s);

            if (this.foundPlayerNames.isEmpty())
            {
                return;
            }

            this.playerNamesFound = true;
            this.inputField.deleteFromCursor(i - this.inputField.getCursorPosition());
        }

        if (this.foundPlayerNames.size() > 1)
        {
            StringBuilder stringbuilder = new StringBuilder();

            for (String s2 : this.foundPlayerNames)
            {
                if (stringbuilder.length() > 0)
                {
                    stringbuilder.append(", ");
                }

                stringbuilder.append(s2);
            }

            this.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(stringbuilder.toString()), 1);
        }

        this.inputField.writeText((String)this.foundPlayerNames.get(this.autocompleteIndex++));
    }

    private void sendAutocompleteRequest(String p_146405_1_, String p_146405_2_)
    {
        if (!p_146405_1_.isEmpty())
        {
            BlockPos blockpos = null;

            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                blockpos = this.mc.objectMouseOver.getBlockPos();
            }

            this.mc.thePlayer.sendQueue.addToSendQueue(new C14PacketTabComplete(p_146405_1_, blockpos));
            this.waitingOnAutocomplete = true;
        }
    }

    public void getSentHistory(int msgPos)
    {
        int i = this.sentHistoryCursor + msgPos;
        int j = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
        i = MathHelper.clamp_int(i, 0, j);

        if (i != this.sentHistoryCursor)
        {
            if (i == j)
            {
                this.sentHistoryCursor = j;
                this.inputField.setText(this.historyBuffer);
            }
            else
            {
                if (this.sentHistoryCursor == j)
                {
                    this.historyBuffer = this.inputField.getText();
                }

                this.inputField.setText(this.mc.ingameGUI.getChatGUI().getSentMessages().get(i));
                this.sentHistoryCursor = i;
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        RenderUtil.roundedRect(1, this.height - 14, this.width - 2, this.height, 5, new Color(0,0,0,120));
        this.inputField.drawTextBox();
        IChatComponent ichatcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if (ichatcomponent != null && ichatcomponent.getChatStyle().getChatHoverEvent() != null)
        {
            this.handleComponentHover(ichatcomponent, mouseX, mouseY);
        }
        for(Module m : Reversal.moduleManager.moduleList) {
            if(m.isEnabled() && m.getModuleInfo().category().equals(Category.HUD) && m.isCanBeEdited()) {

                boolean isInside = RenderUtils.isInside(mouseX, mouseY, m.getX() + m.getAdditionalWidth(), m.getY() + m.getAdditionalHeight(), m.getWidth(), m.getHeight()) &&
                        Reversal.moduleManager.moduleList.stream().filter(m2 -> m2.isEnabled() && m2.getModuleInfo().category().equals(Category.HUD) && mouseX >= m2.getX() + m.getAdditionalWidth() && mouseX <= m2.getX() + m.getAdditionalWidth() + m2.getWidth() && mouseY >= m2.getY() + m.getAdditionalHeight() && mouseY <= m2.getY() + m.getAdditionalHeight() + m2.getHeight()).findFirst().get().equals(m);
                m.editOpacityAnimation.setAnimation(isInside ?  255 : 0, 10);

                RoundedUtils.drawRoundOutline(m.getX() + m.getAdditionalWidth() - 4, m.getY() + m.getAdditionalHeight() - 4, (m.getWidth()) + 8, (m.getHeight()) + 8, 6, 1, new Color(255, 255, 255, 0), new Color(255, 255, 255, (int) m.editOpacityAnimation.getValue()));

                String info = m.getModuleInfo().name() + " (" + m.getX() + "," + m.getY() + ")";
                if (isInside) {
                    gs.drawString(info, m.getX() + m.getAdditionalWidth() + m.getWidth() - gs.getWidth(info), m.getY() + m.getAdditionalHeight() + m.getHeight() - 7, new Color(255, 255, 255, (int) m.editOpacityAnimation.getValue()).getRGB());
                }
                if(m.isDragging()) {
                    m.setX(mouseX + m.getDraggingX());
                    m.setY(mouseY + m.getDraggingY());
                }
            }
        }
    }

    public void onAutocompleteResponse(String[] p_146406_1_)
    {
        if (this.waitingOnAutocomplete)
        {
            this.playerNamesFound = false;
            this.foundPlayerNames.clear();

            for (String s : p_146406_1_)
            {
                if (!s.isEmpty())
                {
                    this.foundPlayerNames.add(s);
                }
            }

            String s1 = this.inputField.getText().substring(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false));
            String s2 = StringUtils.getCommonPrefix(p_146406_1_);

            if (!s2.isEmpty() && !s1.equalsIgnoreCase(s2))
            {
                this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());
                this.inputField.writeText(s2);
            }
            else if (!this.foundPlayerNames.isEmpty())
            {
                this.playerNamesFound = true;
                this.autocompletePlayerNames();
            }
        }
    }

    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
