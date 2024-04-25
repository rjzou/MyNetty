package com.health;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.ReferenceCountUtil;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

//  private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);
  private final List<String> proxyServer;
  private HttpOutboundHandler handler;
  private HttpRequestFilter filter = new HeaderHttpRequestFilter();

  public HttpInboundHandler(List<String> proxyServer) {
    this.proxyServer = proxyServer;
    this.handler = new HttpOutboundHandler(this.proxyServer);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    try {
      //logger.info("channelRead流量接口请求开始，时间为{}", startTime);
      FullHttpRequest fullRequest = (FullHttpRequest) msg;
      handler.handle(fullRequest, ctx, filter);

    } catch(Exception e) {
      e.printStackTrace();
    } finally {

    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }
}


