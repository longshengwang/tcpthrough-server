package org.wls.tcpthrough.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.model.ConnectModel;
import org.wls.tcpthrough.model.GlobalObject;

/**
 * Created by wls on 2019/10/15.
 */
public class DataTransferHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LogManager.getLogger(DataTransferHandler.class);

    private boolean isBind = false;
    private String channelId;
    private GlobalObject globalObject;
    private Channel outChannel;

    public DataTransferHandler(GlobalObject globalObject){
        this.globalObject = globalObject;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("DATA   new data transfer connection comming -=-=-==- " + ctx.channel().remoteAddress());
        ctx.channel().read();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("Inner data client is inactive. inner address " + ctx.channel().remoteAddress());
        ctx.channel().close();
        if(outChannel != null){
            outChannel.close();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(isBind){
            outChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        ctx.channel().read();
                    } else {
                        LOG.error("Write data to outer client error! Outer remote address is " + outChannel.remoteAddress());
                        ctx.channel().close();
                        if(outChannel != null){
                            outChannel.close();
                        }

                    }
                }
            });
        } else {
//            LOG.info("Receive the inner data client msg");

            //TODO need to verify the channel id. The bad network may only send part of channel id.
            ByteBuf bf = (ByteBuf)msg;
            byte[] byteArray = new byte[bf.readableBytes()];
            bf.readBytes(byteArray);
            channelId = new String(byteArray);

            LOG.info("The coming channel id is " + channelId);

            connectOuterChannel(ctx);
        }
    }

    public void connectOuterChannel(ChannelHandlerContext ctx){
        //LOG.info("Try to connect outChannel with dataClient channel!");

        ConnectModel connectModel = globalObject.getOuterConnection(channelId);
        if(connectModel == null){
            LOG.error("The channel id " + channelId + " is not valid, someone may try to attach the data server! The channel address is local:" + ctx.channel().localAddress() + " , remote: " + ctx.channel().remoteAddress() );
            ctx.channel().close();
            return;
        }
        connectModel.getOuterHandler().setDataChannel(ctx.channel());
        outChannel = connectModel.getOutChannel();
        outChannel.read();
        isBind = true;
        LOG.info("Connect the two channel successfully.");
        ctx.channel().read();
    }


    public GlobalObject getGlobalObject() {
        return globalObject;
    }

    public void setGlobalObject(GlobalObject globalObject) {
        this.globalObject = globalObject;
    }
}
