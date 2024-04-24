package com.health;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface HttpRequestFilter {
  void filter(FullHttpRequest request, ChannelHandlerContext ctx);
}
