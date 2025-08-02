package dev.yalan.live.netty.codec.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class AESDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final Cipher cipher;
    private final SecretKey key;
    private final byte[] aad;

    public AESDecoder(SecretKey key, byte[] aad) {
        this.key = key;
        this.aad = aad;

        try {
            this.cipher = Cipher.getInstance("AES/GCM/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Can't init cipher", e);
        }
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> outList) throws Exception {
        final byte[] iv = new byte[12];
        msg.readBytes(iv);

        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

        final ByteBuf out = ctx.channel().alloc().buffer(cipher.getOutputSize(msg.readableBytes()));

        try {
            cipher.updateAAD(aad);
            final int written = cipher.doFinal(
                msg.nioBuffer(),
                out.nioBuffer(0, out.capacity())
            );
            msg.readerIndex(msg.readableBytes());
            out.writerIndex(written);

            outList.add(out.retain());
        } finally {
            out.release();
        }
    }
}
