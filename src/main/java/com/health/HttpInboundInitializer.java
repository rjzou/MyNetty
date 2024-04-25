package com.health;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

import java.util.List;

public class HttpInboundInitializer extends ChannelInitializer<SocketChannel> {

  private List<String> proxyServer;

  public HttpInboundInitializer(List<String> proxyServer) {
    this.proxyServer = proxyServer;
  }

  // 初始化通道
  @Override
  public void initChannel(SocketChannel ch) {
    ChannelPipeline p = ch.pipeline();
    p.addLast(new HttpServerCodec());
    p.addLast(new HttpObjectAggregator(1024 * 1024));
    //通过添加ChannelTrafficShapingHandler处理器来限制每个连接的最大传输速率为每秒1MB。您可以根据自己的需要调整writeLimit、readLimit和delay值。
    p.addLast(new ChannelTrafficShapingHandler(1024 * 1024*10, 1024 * 1024*10, 1000));
    p.addLast(new HttpInboundHandler(this.proxyServer));
  }
}
