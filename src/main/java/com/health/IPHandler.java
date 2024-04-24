package com.health;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public class IPHandler extends SimpleChannelInboundHandler<HttpObject> {
  public static final IPHandler INSTANCE = new IPHandler();

  @Override
  public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
    if (msg instanceof HttpRequest) {
      HttpRequest mReq = (HttpRequest) msg;
      // 从请求头获取 nginx 反代设置的客户端真实 IP
      String clientIP = mReq.headers().get("X-Real-IP");
      // 如果为空则使用 netty 默认获取的客户端 IP
      if (clientIP == null) {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        clientIP = insocket.getAddress().getHostAddress();
      }
      ctx.channel().attr(Attributes.IP).set(clientIP);
    }
    // 传递给下一个 handler
    ctx.fireChannelRead(msg);
  }
}
