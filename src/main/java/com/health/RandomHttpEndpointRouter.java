package com.health;

import cn.hutool.core.util.RandomUtil;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomHttpEndpointRouter implements HttpEndpointRouter {
  private AtomicInteger index = new AtomicInteger(0);

  @Override
  public String route(List<String> urlList) {
    String t;
    if (index.get() >= urlList.size()) {
      index = new AtomicInteger(0);
    }
    t = urlList.get(index.get());
    //轮询+1
    index.getAndIncrement();
    return t;
  }
}
