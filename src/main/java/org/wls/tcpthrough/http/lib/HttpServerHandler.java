package org.wls.tcpthrough.http.lib;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.http.lib.entity.Param;
import org.wls.tcpthrough.http.lib.entity.Router;
import org.wls.tcpthrough.model.GlobalObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by shirukai on 2018/9/30
 * Http服务处理器
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private Map<String, Router> routers;
    private AsciiString contentType = HttpHeaderValues.APPLICATION_JSON;
    public final Logger log = LogManager.getLogger(this.getClass());
    GlobalObject globalObject;

    public HttpServerHandler(Map<String, Router> routers, GlobalObject globalObject) {
        this.routers = routers;
        this.globalObject = globalObject;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) {
        log.debug("=========================================http protocol data >> START");
        log.debug("Handler request:{}", request);
        log.debug("=========================================http protocol data <<< END");
        Object result = null;
        HttpResponseStatus status = null;
        try {
            // 初始化请求参数
            List<Object> params = new ArrayList<>(16);
            // 解析URL中的参数
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri(), Charset.forName("UTF-8"));
            // 获取请求路径
            String url = decoder.path();
            // 获取请求方法
            String method = request.method().toString();
            // 获取URL中的参数
            Map<String, List<String>> urlParams = decoder.parameters();
            // 从扫描的注解中获取Router信息
            Router router = getRoute(url);
            //判断router里是否包含url
            if (router == null) {
                throw new RuntimeException("URL does not exist!");
            }
            //判断请求方法是否匹配
            if (!router.getMethodType().contains(method)) {
                throw new RuntimeException("Method does not match!");
            }
            //获取controller里请求参数信息
            List<Param> paramInfo = router.getParams();
            if (paramInfo != null) {
                //处理URL参数
                Map<String, String> pathParams = PathUrlHandler.getParams(url, router.getUrl());
                JSONObject initJson = null;
                //处理POST请求
                if (method.equals("POST")) {
                    String contentType = request.headers().get("Content-Type");
                    //处理application/json请求
                    if (contentType.contains("application/json")) {
                        initJson = JSON.parseObject(request.content().toString(Charset.forName("UTF-8")));
                    }
                }
                JSONObject finalJson = initJson;
                paramInfo.forEach(param -> {
                    String type = param.getType();
                    String value = param.getValue();
                    // 如果参数在url里形如/test/{id}时 添加参数
                    if (type.equals("path")) {
                        params.add(pathParams.get(value));
                    }
                    // 如果参数在url里形如/test?id=124时 添加参数
                    if (type.equals("request")) {
                        params.add(urlParams.get(value).get(0));
                    }
                    // 如果参数在body里以json形如传入时 添加参数
                    if (type.equals("json")) {
                        params.add(finalJson);
                    }
                });
                // 执行router映射的方法，并获取返回结果
                result = executeMethod(router, request, channelHandlerContext, params.toArray());
                // 设置response状态为 OK
                status = HttpResponseStatus.OK;
            } else {
                // 如果没有参数，直接执行映射的方法
                result = executeMethod(router, request, channelHandlerContext);
                status = HttpResponseStatus.OK;
            }
        } catch (Exception e) {
            // 如果捕获到异常，将异常信息放到返回结果里
            result = e.getMessage();
        } finally {
            // 如果状态为null,将返回结果设置为500
            if (status == null) {
                status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
            }
        }
        // 包装response
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                status,
                Unpooled.wrappedBuffer(JSON.toJSONBytes(result)));

        // 设置response 的header
        HttpHeaders heads = response.headers();
        heads.add(HttpHeaderNames.CONTENT_TYPE, contentType + "; charset=UTF-8");
        heads.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        heads.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        // 将返回结果返回
        channelHandlerContext.write(response);


    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (null != cause) cause.printStackTrace();
        if (null != ctx) ctx.close();
    }

    /**
     * 获取Router信息
     *
     * @param url url
     * @return Router
     */
    private Router getRoute(String url) {
        AtomicReference<Router> router = new AtomicReference<>();
        routers.keySet().forEach(routerKey -> {
            if (PathUrlHandler.verify(url, routerKey)) {
                router.set(routers.get(routerKey));
            }
        });
        return router.get();
    }

    /**
     * 执行路由映射的方法
     *
     * @param router Router
     * @param params params
     * @return Object
     * @throws Exception 执行异常
     */
    private Object executeMethod(Router router, FullHttpRequest request, ChannelHandlerContext channelHandlerContext, Object... params) throws Exception {
        Class<?> cls = router.gettClass();

        Constructor clsCst= cls.getDeclaredConstructor(GlobalObject.class, ChannelHandlerContext.class, FullHttpRequest.class);
        Object obj = clsCst.newInstance(globalObject, channelHandlerContext, request);

        Method method = router.getMethod();
        return method.invoke(obj, params);
    }

}
