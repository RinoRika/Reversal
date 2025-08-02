package dev.yalan.live.netty.codec.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;

public class RSADecoder extends MessageToMessageDecoder<ByteBuf> {
    private final Cipher cipher;
    private final RSAPrivateKey key;

    public RSADecoder(RSAPrivateKey key) {
        this.key = key;

        try {
            this.cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Can't init cipher", e);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> outList) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, key);

        final ByteBuf out = ctx.channel().alloc().buffer(cipher.getOutputSize(msg.readableBytes()));

        try {
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
