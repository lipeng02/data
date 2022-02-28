package com.sansi.data.netty;

import com.sansi.data.utils.DataFormat;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class MyDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        // 创建字节数组,readableBytes获取可读字节长度
        byte[] byteArr = new byte[byteBuf.readableBytes()];
        // 复制内容到字节数组b
        byteBuf.readBytes(byteArr);

        // 直接发字节数组会被netty自动转义,出错
        String[] hexArr = DataFormat.byteArrToHexArr(byteArr);
        list.add(hexArr);
    }
}
