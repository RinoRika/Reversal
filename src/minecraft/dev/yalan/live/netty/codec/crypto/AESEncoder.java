package dev.yalan.live.netty.codec.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

public class AESEncoder extends MessageToByteEncoder<ByteBuf> {
    private final Cipher cipher;
    private final SecretKey key;
    private final byte[] aad;

    public AESEncoder(SecretKey key, byte[] aad) {
        this.key = key;
        this.aad = aad;

        try {
            this.cipher = Cipher.getInstance("AES/GCM/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Can't init cipher", e);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        final byte[] iv = new byte[12];
        ThreadLocalRandom.current().nextBytes(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        out.ensureWritable(12 + cipher.getOutputSize(msg.readableBytes()));
        out.writeBytes(iv);

        cipher.updateAAD(aad);
        final int written = cipher.doFinal(
            msg.nioBuffer(),
            out.nioBuffer(iv.length, out.capacity() - iv.length)
        );
        msg.readerIndex(msg.readableBytes());
        out.writerIndex(12 + written);
    }
}
