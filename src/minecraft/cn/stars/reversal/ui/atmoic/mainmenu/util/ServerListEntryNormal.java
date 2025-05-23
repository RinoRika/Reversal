package cn.stars.reversal.ui.atmoic.mainmenu.util;

import cn.stars.reversal.GameInstance;
import cn.stars.reversal.ui.atmoic.mainmenu.AtomicMenu;
import cn.stars.reversal.ui.atmoic.mainmenu.impl.MultiPlayerGui;
import cn.stars.reversal.ui.atmoic.mainmenu.impl.misc.AddServerGui;
import cn.stars.reversal.ui.atmoic.mainmenu.impl.misc.YesNoGui;
import cn.stars.reversal.util.animation.rise.Animation;
import cn.stars.reversal.util.animation.rise.Easing;
import cn.stars.reversal.util.render.RenderUtil;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class ServerListEntryNormal implements GuiListExtended.IGuiListEntry, GameInstance
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation UNKNOWN_SERVER = new ResourceLocation("textures/misc/unknown_server.png");
    private static final ResourceLocation SERVER_SELECTION_BUTTONS = new ResourceLocation("textures/gui/server_selection.png");
    private final MultiPlayerGui owner;
    private final Minecraft mc;
    private final ServerData server;
    private final ResourceLocation serverIcon;
    private String field_148299_g;
    private DynamicTexture field_148305_h;
    private long field_148298_f;
    private final Animation hoverAnimation = new Animation(Easing.EASE_OUT_EXPO, 1000);
    private final Animation selectAnimation = new Animation(Easing.EASE_OUT_EXPO, 1000);
    private final Animation deleteAnimation = new Animation(Easing.EASE_OUT_EXPO, 1000);
    private final Animation editAnimation = new Animation(Easing.EASE_OUT_EXPO, 1000);


    protected ServerListEntryNormal(MultiPlayerGui p_i45048_1_, ServerData serverIn)
    {
        this.owner = p_i45048_1_;
        this.server = serverIn;
        this.mc = Minecraft.getMinecraft();
        this.serverIcon = new ResourceLocation("servers/" + serverIn.serverIP + "/icon");
        this.field_148305_h = (DynamicTexture)this.mc.getTextureManager().getTexture(this.serverIcon);
    }

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        if (!this.server.field_78841_f)
        {
            this.server.field_78841_f = true;
            this.server.pingToServer = -2L;
            this.server.serverMOTD = "";
            this.server.populationInfo = "";
            this.owner.serverPinger.ping(this.server);
        }

        hoverAnimation.run(RenderUtil.isHovered(x - 2, y - 2 , listWidth, slotHeight + 4, mouseX, mouseY) ? 100 : 0);
        RenderUtil.roundedRectangle(x - 2, y - 2, listWidth, slotHeight + 4, 2, new Color(20, 20, 20, (int) hoverAnimation.getValue()));
        if (owner.serverListSelector.isSelected(slotIndex)) {
            selectAnimation.run(150);
        } else {
            selectAnimation.run(0);
        }

        deleteAnimation.run(RenderUtil.isHovered(x - 25 + listWidth + 1, y - 5 + slotHeight / 2f, 16, 16, mouseX, mouseY) ? 255 : 155);
        editAnimation.run(RenderUtil.isHovered(x - 50 + listWidth + 1, y - 5 + slotHeight / 2f, 16, 16, mouseX, mouseY) ? 255 : 155);

        RenderUtil.roundedRectangle(x - 2, y - 2, listWidth, slotHeight + 4, 2, new Color(20, 20, 20, (int) selectAnimation.getValue()));

        boolean flag = this.server.version > 47;
        boolean flag1 = this.server.version < 47;
        boolean flag2 = flag || flag1;
        regular20Bold.drawString(this.server.serverName, x + 40, y + 2, Color.WHITE.getRGB());
        psr16.drawString("[" + this.server.serverIP + "]", x + 45 + regular20Bold.width(this.server.serverName), y + 3, Color.GRAY.getRGB());
        List<String> list = mc.fontRendererObj.listFormattedStringToWidth(this.server.serverMOTD, listWidth - 32 - 2);

        float iconPos = x + 60 + regular20Bold.width(this.server.serverName) + 5 + psr16.width("[" + this.server.serverIP + "]");
        for (int i = 0; i < Math.min(list.size(), 2); ++i)
        {
            regular16.drawString(list.get(i), x + 40, y + 14 + regular16.height() * i,  new Color(220, 220, 220, 250).getRGB());
            iconPos = Math.max(iconPos, x + 60 + regular16.width(list.get(i)));
        }

        psm16.drawString(this.server.populationInfo, x + listWidth - psm16.width(this.server.populationInfo) - 18, y + 2.5,  new Color(220, 220, 220, 250).getRGB());

        atomic24.drawString("A", iconPos, y + 12, new Color(250,250,250, (int)(selectAnimation.getValue() * 1.6)).getRGB());

        atomic24.drawString("B", x - 20 + listWidth, y + slotHeight / 2f, new Color(255,255,255, (int) deleteAnimation.getValue()).getRGB());
        atomic24.drawString("C", x - 45 + listWidth, y + slotHeight / 2f, new Color(255,255,255, (int) editAnimation.getValue()).getRGB());

        float finalIconPos = iconPos;
        AtomicMenu.POST_POSTPROCESSING_QUEUE.add(() -> {
            if (owner.serverListSelector.isSelected(slotIndex)) {
                regular20Bold.drawString(this.server.serverName, x + 40, y + 2, new Color(255,255,255, (int)(selectAnimation.getValue() * 1.6)).getRGB());
            }

            atomic24.drawString("A", finalIconPos, y + 12, new Color(255,255,255, (int)(selectAnimation.getValue() * 1.6)).getRGB());

            atomic24.drawString("B", x - 20 + listWidth, y + slotHeight / 2f, new Color(255,255,255, (int) deleteAnimation.getValue()).getRGB());
            atomic24.drawString("C", x - 45 + listWidth, y + slotHeight / 2f, new Color(255,255,255, (int) editAnimation.getValue()).getRGB());
        });

        if (Mouse.isButtonDown(0)) {
            if (RenderUtil.isHovered(x - 25 + listWidth + 1, y - 5 + slotHeight / 2f, 16, 16, mouseX, mouseY) && deleteAnimation.getValue() > 200) {
                String s4 = this.getServerData().serverName;
                if (s4 != null)
                {
                    owner.deletingServer = true;
                    String s = I18n.format("selectServer.deleteQuestion");
                    String s1 = "'" + s4 + "' " + I18n.format("selectServer.deleteWarning");
                    YesNoGui guiyesno = new YesNoGui(2, owner.serverListSelector.getSelectedIndex(), s, s1);
                    AtomicMenu.setMiscGui(guiyesno);
                    AtomicMenu.switchGui(8);
                }
            }
            if (RenderUtil.isHovered(x - 50 + listWidth + 1, y - 5 + slotHeight / 2f, 16, 16, mouseX, mouseY) && editAnimation.getValue() > 200) {
                owner.editingServer = true;
                ServerData serverdata = this.getServerData();
                owner.selectedServer = new ServerData(serverdata.serverName, serverdata.serverIP, false);
                owner.selectedServer.copyFrom(serverdata);
                AtomicMenu.setMiscGui(new AddServerGui(owner.selectedServer));
                AtomicMenu.switchGui(8);
            }
        }
        int k;
        String s = null;
        int l;
        String s1;

        if (flag2)
        {
            k = 0;
            l = 5;
            s1 = flag ? "客户端过老!" : "服务器过老!";
            s = this.server.playerList;
        }
        else if (this.server.field_78841_f && this.server.pingToServer != -2L)
        {
            k = 0;
            if (this.server.pingToServer < 0L)
            {
                l = 5;
            }
            else if (this.server.pingToServer < 150L)
            {
                l = 0;
            }
            else if (this.server.pingToServer < 300L)
            {
                l = 1;
            }
            else if (this.server.pingToServer < 600L)
            {
                l = 2;
            }
            else if (this.server.pingToServer < 1000L)
            {
                l = 3;
            }
            else
            {
                l = 4;
            }

            if (this.server.pingToServer < 0L)
            {
                s1 = "(no connection)";
            }
            else
            {
                s1 = this.server.pingToServer + "ms";
                s = this.server.playerList;
            }
        }
        else
        {
            k = 1;
            l = (int)(Minecraft.getSystemTime() / 100L + (slotIndex * 2L) & 7L);

            if (l > 4)
            {
                l = 8 - l;
            }

            s1 = "Pinging...";
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(Gui.icons);
        Gui.drawModalRectWithCustomSizedTexture(x + listWidth - 15, y, (float)(k * 10), (float)(176 + l * 8), 10, 8, 256.0F, 256.0F);

        if (this.server.getBase64EncodedIconData() != null && !this.server.getBase64EncodedIconData().equals(this.field_148299_g))
        {
            this.field_148299_g = this.server.getBase64EncodedIconData();
            this.prepareServerIcon();
            this.owner.getServerList().saveServerList();
        }

        if (this.field_148305_h != null)
        {
            this.drawTextureAt(x, y, this.serverIcon);
        }
        else
        {
            this.drawTextureAt(x, y, UNKNOWN_SERVER);
        }

        int i1 = mouseX - x;
        int j1 = mouseY - y;

        if (i1 >= listWidth - 15 && i1 <= listWidth - 5 && j1 >= 0 && j1 <= 8)
        {
            this.owner.setHoveringText(s1);
        }
        else if (i1 >= listWidth - psm16.width(this.server.populationInfo) - 15 - 2 && i1 <= listWidth - 15 - 2 && j1 >= 0 && j1 <= 8)
        {
            this.owner.setHoveringText(s);
        }

        if (this.mc.gameSettings.touchscreen || isSelected)
        {
            this.mc.getTextureManager().bindTexture(SERVER_SELECTION_BUTTONS);
            Gui.drawRect(x, y, x + 32, y + 32, -1601138544);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int k1 = mouseX - x;
            int l1 = mouseY - y;

            if (this.func_178013_b())
            {
                if (k1 < 32 && k1 > 16)
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                }
                else
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }

            if (this.owner.func_175392_a(this, slotIndex))
            {
                if (k1 < 16 && l1 < 16)
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                }
                else
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }

            if (this.owner.func_175394_b(this, slotIndex))
            {
                if (k1 < 16 && l1 > 16)
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                }
                else
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }
        }
    }

    protected void drawTextureAt(int p_178012_1_, int p_178012_2_, ResourceLocation p_178012_3_)
    {
        this.mc.getTextureManager().bindTexture(p_178012_3_);
        GlStateManager.enableBlend();
        Gui.drawModalRectWithCustomSizedTexture(p_178012_1_, p_178012_2_, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        GlStateManager.disableBlend();
    }

    private boolean func_178013_b()
    {
        return true;
    }

    private void prepareServerIcon()
    {
        if (this.server.getBase64EncodedIconData() == null)
        {
            this.mc.getTextureManager().deleteTexture(this.serverIcon);
            this.field_148305_h = null;
        }
        else
        {
            ByteBuf bytebuf = Unpooled.copiedBuffer((CharSequence)this.server.getBase64EncodedIconData(), Charsets.UTF_8);
            ByteBuf bytebuf1 = Base64.decode(bytebuf);
            BufferedImage bufferedimage;
            label101:
            {
                try
                {
                    bufferedimage = TextureUtil.readBufferedImage(new ByteBufInputStream(bytebuf1));
                    Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                    Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                    break label101;
                }
                catch (Throwable throwable)
                {
                    logger.error("Invalid icon for server " + this.server.serverName + " (" + this.server.serverIP + ")", throwable);
                    this.server.setBase64EncodedIconData((String)null);
                }
                finally
                {
                    bytebuf.release();
                    bytebuf1.release();
                }

                return;
            }

            if (this.field_148305_h == null)
            {
                this.field_148305_h = new DynamicTexture(bufferedimage.getWidth(), bufferedimage.getHeight());
                this.mc.getTextureManager().loadTexture(this.serverIcon, this.field_148305_h);
            }

            bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), this.field_148305_h.getTextureData(), 0, bufferedimage.getWidth());
            this.field_148305_h.updateDynamicTexture();
        }
    }

    public boolean mousePressed(int slotIndex, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_)
    {
        if (p_148278_5_ <= 32)
        {
            if (p_148278_5_ < 32 && p_148278_5_ > 16 && this.func_178013_b())
            {
                this.owner.selectServer(slotIndex);
                this.owner.connectToSelected();
                return true;
            }

            if (p_148278_5_ < 16 && p_148278_6_ < 16 && this.owner.func_175392_a(this, slotIndex))
            {
                this.owner.func_175391_a(this, slotIndex, GuiScreen.isShiftKeyDown());
                return true;
            }

            if (p_148278_5_ < 16 && p_148278_6_ > 16 && this.owner.func_175394_b(this, slotIndex))
            {
                this.owner.func_175393_b(this, slotIndex, GuiScreen.isShiftKeyDown());
                return true;
            }
        }

        this.owner.selectServer(slotIndex);

        if (Minecraft.getSystemTime() - this.field_148298_f < 250L)
        {
            this.owner.connectToSelected();
        }

        this.field_148298_f = Minecraft.getSystemTime();
        return false;
    }

    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
    }

    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }

    public ServerData getServerData()
    {
        return this.server;
    }
}
