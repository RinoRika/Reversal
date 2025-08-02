package dev.yalan.live.netty.codec.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;

public class RSAEncoder extends MessageToByteEncoder<ByteBuf> {
    private final Cipher cipher;
    private final RSAPublicKey key;

    public RSAEncoder(RSAPublicKey key) {
        this.key = key;

        try {
            this.cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Can't init cipher", e);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, key);
        out.ensureWritable(cipher.getOutputSize(msg.readableBytes()));

        final int written = cipher.doFinal(
            msg.nioBuffer(),
            out.nioBuffer(0, out.capacity())
        );

        msg.readerIndex(msg.readableBytes());
        out.writerIndex(written);
    }
}
