package com.mashibing.nettystudy.s02;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.CountDownLatch;

public class Client {
    private static class SingletonHolder{
        static final Client instance = new Client();
    }
    public static Client getInstance(){
        return SingletonHolder.instance;
    }


    private EventLoopGroup group;
    private Bootstrap b;
    private ChannelFuture f;
    private ClientInitializer  clientInitializer;
    private Client(){

        clientInitializer= new ClientInitializer();
        group = new NioEventLoopGroup();
        b = new Bootstrap();
        b.group(group).channel(NioServerSocketChannel.class)
        .handler(clientInitializer);

    }

    public void connect(){
        try {
            this.f= b.connect("localhost",10086);
            f.sync();
            System.out.println("连接成功,可以数据交互");
        }catch (Exception e){
          e.printStackTrace();
        } finally {

        }
    }

    public ChannelFuture getF() {
        if (f == null){
            this.connect();
        }
        if (!f.channel().isActive()){
            connect();
        }
        return f;
    }
    //发送消息
    public String senMsg(String data){
        ChannelFuture cf = getInstance().getF();
        cf.channel().writeAndFlush(data);


        return clientInitializer.getServerResult();
    }
}


class ClientInitializer extends ChannelInitializer<SocketChannel>{

    private ClientHandler handler;


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        handler = new ClientHandler();
        ch.pipeline().addLast(handler);
    }


    public String getServerResult(){
        return handler.getResult();
    }
    public void resetLathc(CountDownLatch lathc) {

    }

}

class ClientHandler extends ChannelInboundHandlerAdapter{

    private String result;

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        ByteBuf buf = Unpooled.copiedBuffer("hello".getBytes());
        ctx.writeAndFlush(buf);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        result = (String) msg;
        ByteBuf buf = null;
        try {
            buf = (ByteBuf)msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
          result= new String(bytes);
        } finally {
            if(buf != null) ReferenceCountUtil.release(buf);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }



    public String getResult(){
        return result;
    }
}
