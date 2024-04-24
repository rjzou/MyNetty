package com.health;

import java.util.Arrays;

public class MyProxyServer {
  public static void main(String[] args) {

    // 对外暴露端口 8808
    String proxyPort = System.getProperty("proxyPort","8808");

    // 多个后端url返回随机路由
    // 可以自定义规则匹配url
    String proxyServers = System.getProperty("proxyServers","http://localhost:6789,http://localhost:6790");
    int port = Integer.parseInt(proxyPort);
    HttpInboundServer server = new HttpInboundServer(port, Arrays.asList(proxyServers.split(",")));
    try {
      server.run();
    }catch (Exception ex){
      ex.printStackTrace();
    }
  }
}
