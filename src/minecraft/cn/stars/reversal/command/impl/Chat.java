package cn.stars.reversal.command.impl;

import cn.stars.reversal.Reversal;
import cn.stars.reversal.command.Command;
import cn.stars.reversal.command.api.CommandInfo;
import cn.stars.reversal.ui.notification.NotificationType;
import dev.yalan.live.LiveClient;
import dev.yalan.live.netty.LiveProto;
import net.minecraft.client.resources.I18n;

@CommandInfo(name = "Chat", description = "IRC Chat", syntax = ".chat <message>", aliases = "chat")
public final class Chat extends Command {

    @Override
    public void onCommand(final String command, final String[] args) {
        if (LiveClient.INSTANCE.isAuthenticated()) {
            final StringBuilder message = new StringBuilder();

            for (int i = 0; i < args.length; i++) {
                message.append(args[i]);

                if (i + 1 != args.length) {
                    message.append(' ');
                }
            }

            LiveClient.INSTANCE.sendPacket(LiveProto.createChat(
                message.toString().replace("&", "§")
            ));
        } else {
            Reversal.notificationManager.registerNotification(I18n.format("command.Chat.notConnected"), I18n.format("command.title"), NotificationType.WARNING);
            Reversal.showMsg(I18n.format("command.Chat.notConnected"));
        }
    }
}
