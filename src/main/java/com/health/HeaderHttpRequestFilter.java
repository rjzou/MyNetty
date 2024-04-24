package com.health;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class HeaderHttpRequestFilter implements HttpRequestFilter {
  @Override
  public void filter(FullHttpRequest request, ChannelHandlerContext ctx) {
    request.headers().set("testkey", "lcl");
    System.out.printf("=========" + request.headers().get("testkey"));
  }
}
