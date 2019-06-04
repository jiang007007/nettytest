package com.mashibing.nettystudy.s02;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class Server {
    public static ChannelGroup clents = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static  void main(String[] args){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(2);
        ServerBootstrap sb = new ServerBootstrap();
        try {
           ChannelFuture f = sb.group(bossGroup,workGroup).channel(NioServerSocketChannel.class).childHandler(new ServerChannelInitHandler())
                    .bind(10086).sync();
            System.out.println("server started!");
           f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

class ServerChannelInitHandler extends ChannelInitializer<SocketChannel>{

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new ServerChannelInHandler());
    }
}
class ServerChannelInHandler extends ChannelInboundHandlerAdapter{
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Server.clents.add(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = null;
        try {
//            buf = (ByteBuf) msg;
            String s = (String) msg;
            byte [] result = new  byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(),result);
            Server.clents.writeAndFlush(s);
        }catch (Exception e){

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
