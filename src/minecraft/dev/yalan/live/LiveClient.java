package dev.yalan.live;

import dev.yalan.live.events.EventLiveConnectionStatus;
import dev.yalan.live.netty.LiveProto;
import dev.yalan.live.netty.codec.FrameDecoder;
import dev.yalan.live.netty.codec.FrameEncoder;
import dev.yalan.live.netty.codec.crypto.RSADecoder;
import dev.yalan.live.netty.codec.crypto.RSAEncoder;
import dev.yalan.live.netty.handler.LiveHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import net.minecraft.client.Minecraft;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

public class LiveClient {
    public static LiveClient INSTANCE;

    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("Live-Worker"));
    public final LiveComponent liveComponent = new LiveComponent(this);
    public final AtomicBoolean isConnecting = new AtomicBoolean();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final RSAPrivateKey rsaPrivateKey;
    private final RSAPublicKey rsaPublicKey;

    public LiveUser liveUser;
    private Channel channel;

    public LiveClient() {
        try {
            rsaPrivateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode("MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDvglyC13gs5pUwzdPpFOd4V1apvdjJHMoqrnuiJBEFuWdBWqSliVDKDEzhI07ZsMeZccjn6rklD0R3fjaaki/WktG0N17roaluQnxG6IVx3LYHqlrT6KLhUpByS3facNPivS9YcQTNAlzvvH7tzLLMZLHl8MEdY2XjYRnpr2Di0Mk32fGWpNrlGSxZjdlV3vv1WK0uNo+cVvG+CHCh4F9Ru88s6PDKEx+1+GFMGaKvzaGTHCSMvT/JKH24EbkWMGTDxGQpKkkpvX+v7s6qXpv5IwpRHjhKYpNOZ+MjmWcQU0qfHx0YXSv5hhwm9azgWnYZjJ0OFs4JnOYRiLx3jgRVAgMBAAECggEAUKcQagsO7+fR83ZMVt/wNmkKyOwiNU8ZkRfikC4fAVN2vt48NXYxG2ja9rGCGvJZtIVJhzdWk3E5uBdrYc+6hkI4lbxTnXt556RB30rrrPUK4zftkBB10PSRqbtaJ9f4shDNAbZFTJfwHdbW01MsHoIGGg1hqnjPVatcI4IoWRgY37w0RpQ3/iAnYQVwSUY2UXepNxC4Qn5/ai98S4ZT2TD7A5lnGp8LGflhBruMrrYRO0OWuPCx4prxJYOzjwXa8GI76FVgV40OOEGKRv3hGuHMwK+a7xlCm05gDZN1AfjN1CjHA+yi0YrMCt5qOTRDxtPUEmIzUB4h6t6S9keXvQKBgQDv0Sey8Bgu8lQxAoe8gBgKcz6doeCkSF+jjaf/LQPjN3cU8VG65OemnlBaJVylukG28ExaCMb/+cw9PRJIAz/snbHrWe8fR9v0L5PWpkVGAy+WbJ0txgK1hxXmcrwjVZ8cijOIKiDD6fseki+djukKYAN6pIT+Nz5Yj1jkaO8UUwKBgQD/q+OmH/vls1y9Dr8bB19z/Q/VTs9g/uf7n7zay7nviiQDXSrziwNXHajZ/cWjC3BJOuDAGqn+NWhLqFUSaOPxGVVXqWlwj9di0Q8oIGau73TDDozC7+F/4bvPPiJzodvdqNMXmKTMNoAIjAiYpKTg6B07iRVk19DwlX7Ctv/vtwKBgFumXTufUj6u0cohI2rAsW9c97MaaFWgaxyASJHWzEOLsPDKAOgiDv24RxEMcknL0s1tLGDENfkhqBJHQrNOuqFc6t/88MvNlJvPivpfdN363bbpL3CM2gAx7gayFoCSekpX7rW9E+SLiTL3v0bBlQIoC2xVb5YP2r3RmrFAAQ6NAoGAENRlxdexIyutXRyNY+AWXnI4CpiHeiENcDjRm61xbd0wBbCqVSG/dIqVRNQ5oEPufkOL69lVe1BUrzZDc1TIPGSog365XBl72htc3g1T9Qv6KaTZizNzKpHXZQr4BZiP+oVUb+cyYebsgqOFJVchK1TcJ8EcUWkLberJT1PwgP0CgYBZBEpqtiNNaSU3ptkulB561dJ1SR1dqNZZmg5Bvv3nmh5LWK5jY0AKih7X4+oibTnBki5QDHQ88mkOB6NsNEuFTZ58wiVKVd4NQDd2dhXfNX9Z/HdqY4sOgu1od0n8b9yqv0Uly3AZJQ92Wu2ReQqzkEO/TFUq/MW2tMfT/LQl2w==")));
            rsaPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAujJkxcTlTpwjbwiMh8FZWn7mUFNc9cNbV0b6bbM2VpASj7OuWdLWfP/UT4uNOgYxKmiWlSepG6vq9HgmEpHDAmEBQvtAOPDcUCHMtpTXdHwnjw7LhfsjT6VokiKjnwAvOlBVfhoTQzM6MJrMA77YWI6MwJXfmGFD1ViXXEiZquaDpmGKn0pPjqhG6CBNBSd+F5fPkkz9pyhp2VYbBx++/4whO+PKBG3likBlz6LkRN5Ybrnf4JBwZUPKcSAfeC8I38EQJEpVU20tpYRYeKcPqEXeY1t9ZN330d9upQqSHt2CItrBRXdAV5b0oqXfvBemnOGfV2/zb8ArOHl8sFlbmQIDAQAB")));
        } catch (Exception e) {
            throw new RuntimeException("Can't init LiveClient", e);
        }
    }

    public void connect() {
        if (isOpen() || isConnecting.get()) {
            return;
        }

        isConnecting.set(true);

        final Bootstrap bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline()
                                .addLast("frame_decoder", new FrameDecoder())
                                .addLast("rsa_decoder", new RSADecoder(rsaPrivateKey))
                                .addLast("frame_encoder", new FrameEncoder())
                                .addLast("rsa_encoder", new RSAEncoder(rsaPublicKey))
                                .addLast("live_handler", new LiveHandler(LiveClient.this));
                    }
                });

        bootstrap.connect("irc.6667890.xyz", 11711).addListener((ChannelFutureListener) future -> {
            isConnecting.set(false);

            if (future.isSuccess()) {
                LiveProto.sendPacket(future.channel(), LiveProto.createHandshake());
            }

            mc.addScheduledTask(() -> {
                if (future.isSuccess()) {
                    channel = future.channel();
                }

                new EventLiveConnectionStatus(future.isSuccess(), future.cause()).call();
            });
        });
    }

    public void sendPacket(LiveProto.LivePacket packet) {
        if (isActive()) {
            LiveProto.sendPacket(channel, packet);
        }
    }

    public void close() {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }

        workerGroup.shutdownGracefully();
    }

    public boolean isOpen() {
        return channel != null && channel.isOpen();
    }

    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    public boolean isAuthenticated() {
        return liveUser != null;
    }
}
