package com.health;

import io.netty.handler.codec.http.FullHttpResponse;

public class HeaderHttpResponseFilter implements HttpResponseFilter {
  @Override
  public void filter(FullHttpResponse response) {
    response.headers().set("testkey","ml");
  }
}
