package dev.yalan.live;

import cn.stars.reversal.Reversal;
import cn.stars.reversal.module.impl.client.IRC;
import cn.stars.reversal.util.misc.ModuleInstance;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.gui.ChatFormatting;
import dev.yalan.live.events.EventLiveChannelInactive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import java.util.UUID;

@SuppressWarnings({"StringBufferReplaceableByString"})
public class LiveComponent {
    private final JsonParser parser = new JsonParser();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final LiveClient live;

    LiveComponent(LiveClient live) {
        this.live = live;
    }

    public void onLiveChannelInactive(EventLiveChannelInactive e) {
        if (mc.theWorld != null) {
            Reversal.showMsg(ChatFormatting.RED + "LiveServer disconnected...");
        }
    }

    public void handleQueryResultMinecraftProfile(UUID mcUUID, String clientId, UUID userId, String userPayloadString) {
        if (mc.theWorld == null || !ModuleInstance.getModule(IRC.class).isEnabled()) {
            return;
        }

        final JsonObject payload = parser.parse(userPayloadString).getAsJsonObject();
        LiveUser liveUser = null;

        if (mc.getNetHandler() != null) {
            final NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(mcUUID);

            if (playerInfo != null) {
                playerInfo.liveUser = liveUser = new LiveUser(clientId, userId, payload);
            }
        }

        final EntityPlayer player = mc.theWorld.getPlayerEntityByUUID(mcUUID);

        if (player != null) {
            player.liveUser = liveUser == null ? new LiveUser(clientId, userId, payload) : liveUser;
        }
    }

    public void handleChat(String channel, String payloadString) {
        if (!ModuleInstance.getModule(IRC.class).isEnabled()) {
            return;
        }

        final JsonObject payload = parser.parse(payloadString).getAsJsonObject();

        switch (channel) {
            case "LivePublic": {
                final String clientId = payload.get("clientId").getAsString();
                final String message = payload.get("message").getAsString();

                if ("Reversal".equals(clientId)) {
                    printPublicChat(clientId, message, payload.get("username").getAsString(), payload.get("rank").getAsString());
                } else {
                    printPublicChat(clientId, message, payload.get("username").getAsString(), null);
                }

                break;
            }
            case "ServerLog": {
                final String message = payload.get("message").getAsString();
                printSimpleChat(ChatFormatting.AQUA + "Server", message);

                break;
            }
            case "Broadcast": {
                final String message = payload.get("message").getAsString();
                printSimpleChat(ChatFormatting.GOLD + "Broadcast", message);

                break;
            }
        }
    }

    private void printPublicChat(String clientId, String message, String username, String rank) {
        final StringBuilder builder = new StringBuilder();

        builder.append(ChatFormatting.GRAY).append("[");
        builder.append(ChatFormatting.YELLOW).append("Live");
        builder.append(ChatFormatting.GRAY).append("-");
        builder.append(ChatFormatting.RED).append(clientId);
        builder.append(ChatFormatting.GRAY).append("] ");
        builder.append(ChatFormatting.RESET).append(username);

        if (rank != null) {
            builder.append(ChatFormatting.GRAY).append("(");
            builder.append(ChatFormatting.RESET).append(rank).append(ChatFormatting.RESET);
            builder.append(ChatFormatting.GRAY).append(")");
        }

        builder.append(ChatFormatting.GRAY).append(": ");
        builder.append(ChatFormatting.RESET).append(message);

        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(builder.toString()));
    }

    private void printSimpleChat(String sender, String message) {
        final StringBuilder builder = new StringBuilder();

        builder.append(ChatFormatting.GRAY).append("[");
        builder.append(ChatFormatting.YELLOW).append("Live");
        builder.append(ChatFormatting.GRAY).append("-");
        builder.append(ChatFormatting.RESET).append(sender);
        builder.append(ChatFormatting.GRAY).append("]: ");
        builder.append(ChatFormatting.RESET).append(message);

        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(builder.toString()));
    }

    public static String getLiveUserDisplayName(LiveUser liveUser) {
        final StringBuilder builder = new StringBuilder();

        builder.append(ChatFormatting.GRAY).append("[");
        builder.append(ChatFormatting.YELLOW).append("Live");
        builder.append(ChatFormatting.GRAY).append("-");
        builder.append(ChatFormatting.RED).append(liveUser.getClientId());
        builder.append(ChatFormatting.GRAY).append("-");
        builder.append(ChatFormatting.RESET).append(liveUser.getName());

        final String rank = liveUser.getRank();

        if (rank != null) {
            builder.append(ChatFormatting.GRAY).append("(");
            builder.append(ChatFormatting.RESET).append(rank).append(ChatFormatting.RESET);
            builder.append(ChatFormatting.GRAY).append(")");
        }

        builder.append(ChatFormatting.GRAY).append("]");

        return builder.toString();
    }
}
