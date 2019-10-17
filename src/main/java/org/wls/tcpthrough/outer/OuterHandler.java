package org.wls.tcpthrough.outer;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import org.wls.tcpthrough.Tools;
import org.wls.tcpthrough.model.ConnectModel;
import org.wls.tcpthrough.model.GlobalObject;
import org.wls.tcpthrough.model.ManagerProtocolBuf.ManagerResponse;
import org.wls.tcpthrough.model.ResponseType;


/**
 * Created by wls on 2019/10/15.
 */
public class OuterHandler extends ChannelInboundHandlerAdapter {

    private boolean isBind = false;
    private String channelId;
    private GlobalObject globalObject;
    private Channel dataChannel;
    private Channel manageChannel;

    public OuterHandler(Channel manageChannel, GlobalObject globalObject){
        this.manageChannel = manageChannel;
        this.globalObject = globalObject;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("OUThandle 有新连接来了");
        channelId = Tools.uuid();
        while (globalObject.isOuterChannelUUIDExist(channelId)) {
            channelId = Tools.uuid();
        }
//        System.out.println("OUThandle 最终 channel id 是 " + channelId);

        ConnectModel connectModel = new ConnectModel(channelId, this, ctx.channel());
        globalObject.putOuterConnection(channelId, connectModel);
        ManagerResponse response = ManagerResponse.newBuilder()
                .setType(ResponseType.NEW_CONN_RESPONSE.get())
                .setChannel(channelId)
                .build();
        manageChannel.writeAndFlush(response).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()){
//                    System.out.println("OUT 告诉 client channel id 成功");
                } else {
//                    System.out.println("OUT 告诉 client channel id 失败");
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().close();
        globalObject.deleteOuterConnection(channelId);
        if (dataChannel != null) {
            dataChannel.close();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        dataChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
//                    System.out.println("OUT 成功写数据到 data client");
                    ctx.channel().read();
                } else {
                    System.out.println("OUT 写数据到client ERROR");
                }
            }
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("OUT read COMPLETE");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        System.out.println("OUT exceptionCaught");
    }

    //    public Channel getDataChannel() {
//        return dataChannel;
//    }

    public void setDataChannel(Channel dataChannel) {
        this.dataChannel = dataChannel;
    }

//    public String getChannelId() {
//        return channelId;
//    }
}
