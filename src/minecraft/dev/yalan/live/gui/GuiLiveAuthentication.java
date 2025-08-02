package dev.yalan.live.gui;

import cn.stars.reversal.util.misc.FileUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import dev.yalan.live.LiveClient;
import dev.yalan.live.events.*;
import dev.yalan.live.netty.LiveProto;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class GuiLiveAuthentication extends GuiScreen {
    private static final Logger logger = LogManager.getLogger("GuiAuthentication");
    private static final File accountDataFile = new File(FileUtil.REVERSAL_PATH, "LiveAccount.dat");
    private static String savedUsername = "";
    private static String savedPassword = "";

    static {
        try {
            loadAccountData();
        } catch (Exception e) {
            logger.error("Can't load live account data", e);
        }
    }

    private final GuiScreen parentScreen;
    private GuiTextField username;
    private GuiTextField password;
    private GuiButton loginButton;

    public String status;

    public GuiLiveAuthentication(GuiScreen parentScreen) {
        this(parentScreen, ChatFormatting.YELLOW + "Pending...");
    }

    public GuiLiveAuthentication(GuiScreen parentScreen, String status) {
        this.parentScreen = parentScreen;
        this.status = status;
    }

    public void onLiveChannelInactive(EventLiveChannelInactive e) {
        loginButton.enabled = true;
        username.setEnabled(true);
        password.setEnabled(true);
    }

    public void onLiveChannelException(EventLiveChannelException e) {
        this.status = e.getCause().toString();
    }

    public void onLiveConnectionStatus(EventLiveConnectionStatus e) {
        if (e.getCause() != null) {
            this.status = e.getCause().toString();
        }
    }

    public void onLiveGenericMessage(EventLiveGenericMessage e) {
        this.status = e.getMessage();
    }

    public void onLiveAuthenticationResult(EventLiveAuthenticationResult e) {
        this.status = (e.isSuccess() ? EnumChatFormatting.GREEN : EnumChatFormatting.RED) + e.getMessage();
        this.loginButton.enabled = true;
        this.username.setEnabled(true);
        this.password.setEnabled(true);

        if (e.isSuccess()) {
            try {
                saveAccountData();
            } catch (Exception ex) {
                logger.error("Can't save account data", ex);
            }
        }
    }

    @Override
    public void initGui() {
        final int halfWidth = width / 2;
        final int halfHeight = height / 2;
        final String previousUsername;
        final String previousPassword;
        final boolean previousLoginButtonEnabled;

        if (this.username != null) {
            previousUsername = this.username.getText();
        } else {
            previousUsername = savedUsername;
        }

        if (this.password != null) {
            previousPassword = this.password.getText();
        } else {
            previousPassword = savedPassword;
        }

        if (this.loginButton != null) {
            previousLoginButtonEnabled = this.loginButton.enabled;
        } else {
            previousLoginButtonEnabled = true;
        }

        this.username = new GuiTextField(0, mc.fontRendererObj, halfWidth - 83, halfHeight - 35, 166, 20);
        this.username.setText(previousUsername);
        this.password = new GuiTextField(1, mc.fontRendererObj, halfWidth - 83, halfHeight, 166, 20);
        this.password.setText(previousPassword);
        this.buttonList.add(this.loginButton = new GuiButton(0, halfWidth - 83, halfHeight + 25, 80, 20, "Login"));
        this.buttonList.add(new GuiButton(1, halfWidth + 3, halfHeight + 25, 80, 20, "Register"));
        this.buttonList.add(new GuiButton(2, halfWidth - 83, halfHeight + 50, 80, 20, "Connect"));
        this.buttonList.add(new GuiButton(3, halfWidth + 3, halfHeight + 50, 80, 20, "Back"));
        this.loginButton.enabled = previousLoginButtonEnabled;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: {
                if (LiveClient.INSTANCE.isActive()) {
                    loginButton.enabled = false;
                    username.setEnabled(false);
                    password.setEnabled(false);
                    LiveClient.INSTANCE.sendPacket(LiveProto.createAuthentication(username.getText(), password.getText(), "undefined"));
                }

                break;
            }
            case 1: {
                Desktop.getDesktop().browse(URI.create("https://irc.zedware.pp.ua/html?name=RegisterWithEmail"));
                break;
            }
            case 2: {
                LiveClient.INSTANCE.connect();
                break;
            }
            case 3: {
                mc.displayGuiScreen(parentScreen);

                break;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        username.drawTextBox();
        password.drawTextBox();

        final int halfWidth = width / 2;
        final int halfHeight = height / 2;

        mc.fontRendererObj.drawCenteredString("Southside Authentication", halfWidth, halfHeight - 80, -1);
        mc.fontRendererObj.drawCenteredString(status, halfWidth, halfHeight - 65, -1);
        mc.fontRendererObj.drawString("LiveServer: " + getLiveConnectionStatus(), 2, height - mc.fontRendererObj.FONT_HEIGHT, -1);
        mc.fontRendererObj.drawString("Username: ", halfWidth - 82, halfHeight - 45, -1);
        mc.fontRendererObj.drawString("Password: ", halfWidth - 82, halfHeight - 10, -1);
    }

    private String getLiveConnectionStatus() {
        if (LiveClient.INSTANCE.isActive()) {
            return ChatFormatting.GREEN + "Connected";
        }

        if (LiveClient.INSTANCE.isConnecting.get()) {
            return ChatFormatting.YELLOW + "Connecting...";
        }

        return ChatFormatting.RED + "No connection";
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        username.textboxKeyTyped(typedChar, keyCode);
        password.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        username.mouseClicked(mouseX, mouseY, mouseButton);
        password.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private static void loadAccountData() throws Exception {
        if (!accountDataFile.exists()) {
            return;
        }

        final byte[] data = FileUtils.readFileToByteArray(accountDataFile);
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        final SecretKey key = new SecretKeySpec(Base64.getDecoder().decode("z2SSbtrapztLIPpZxCDBzA=="), "AES");
        final byte[] iv = new byte[12];

        System.arraycopy(data, 0, iv, 0, 12);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        cipher.updateAAD(accountDataFile.getAbsolutePath().getBytes(StandardCharsets.UTF_8));

        final byte[] out = cipher.doFinal(data, iv.length, data.length - iv.length);
        final String[] split = new String(out, StandardCharsets.UTF_8).split(System.lineSeparator());

        savedUsername = split[0];
        savedPassword = split[1];
    }

    private void saveAccountData() throws Exception {
        final byte[] data = (username.getText() + System.lineSeparator() + password.getText()).getBytes(StandardCharsets.UTF_8);
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        final SecretKey key = new SecretKeySpec(Base64.getDecoder().decode("z2SSbtrapztLIPpZxCDBzA=="), "AES");
        final SecureRandom secureRandom = new SecureRandom();
        final byte[] iv = new byte[12];

        secureRandom.nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        cipher.updateAAD(accountDataFile.getAbsolutePath().getBytes(StandardCharsets.UTF_8));

        final byte[] out = new byte[12 + cipher.getOutputSize(data.length)];
        System.arraycopy(iv, 0, out, 0, iv.length);
        cipher.doFinal(data, 0, data.length, out, 12);

        FileUtils.writeByteArrayToFile(accountDataFile, out);
    }
}
