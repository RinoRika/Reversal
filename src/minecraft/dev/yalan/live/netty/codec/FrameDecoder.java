package dev.yalan.live.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class FrameDecoder extends ByteToMessageDecoder {
    private int length = -1;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        while (true) {
            if (length == -1) {
                if (msg.readableBytes() < 4) {
                    return;
                }

                final int frameLength = msg.readInt();
                if (frameLength <= 0) {
                    ctx.channel().close();
                    return;
                }

                length = frameLength;
            }

            if (msg.readableBytes() >= length) {
                out.add(msg.readSlice(length).retain());
                length = -1;
            } else {
                break;
            }
        }
    }
}
