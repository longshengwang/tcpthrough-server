package org.wls.tcpthrough.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import org.wls.tcpthrough.model.ConnectModel;
import org.wls.tcpthrough.model.GlobalObject;

/**
 * Created by wls on 2019/10/15.
 */
public class DataTransferHandler extends ChannelInboundHandlerAdapter {

    private boolean isBind = false;
    private String channelId;
    private GlobalObject globalObject;
    private Channel outChannel;

    public DataTransferHandler(GlobalObject globalObject){
        this.globalObject = globalObject;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("DATA   new data transfer connection comming -=-=-==- " + ctx.channel().remoteAddress());

        ctx.channel().read();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("DATA   Channel不可用了：" + ctx.channel().remoteAddress());
        ctx.channel().close();
        if(outChannel != null){
            outChannel.close();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("DATA    有新数据来来 isBInd:" + isBind);
        if(isBind){
//            System.out.println("DATA     开始接受数据==========》》》  ");
            outChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        ctx.channel().read();
                    } else {
                        System.out.println("WRITE DATA ERROR");
                    }
                }
            });
        } else {
//            System.out.println("DATA 有新的连接过来了");
            ByteBuf bf = (ByteBuf)msg;
            System.out.println(bf);
            byte[] byteArray = new byte[bf.readableBytes()];
            bf.readBytes(byteArray);
            channelId = new String(byteArray);
//            System.out.println("DATA 新的连接：" +channelId+ "");
            connectOuterChannel(ctx);
        }
    }

    public void connectOuterChannel(ChannelHandlerContext ctx){
        ConnectModel connectModel = globalObject.getOuterConnection(channelId);
        connectModel.getOuterHandler().setDataChannel(ctx.channel());
        outChannel = connectModel.getOutChannel();
        outChannel.read();
        isBind = true;
        System.out.println("DATA connectOuterChannel finish");
        ctx.channel().read();
    }


    public GlobalObject getGlobalObject() {
        return globalObject;
    }

    public void setGlobalObject(GlobalObject globalObject) {
        this.globalObject = globalObject;
    }
}
