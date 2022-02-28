package com.sansi.data.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 通道初始化
 */
public class BootNettyChannelInitializer<SocketChannel> extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {

        // 服务端发送给客户端的数据格式，依照逆序执行
        ch.pipeline().addLast("encoder", new StringEncoder());

        // 服务端解析客户端的数据格式，依照顺序执行
        ch.pipeline().addLast("decoder", new MyDecoder());

        /**
         * 自定义ChannelInboundHandlerAdapter
         */
        ch.pipeline().addLast(new BootNettyChannelInboundHandlerAdapter());
    }


}
