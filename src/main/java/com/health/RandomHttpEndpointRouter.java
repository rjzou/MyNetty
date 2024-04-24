package com.health;

import java.util.List;
import java.util.Random;

public class RandomHttpEndpointRouter implements HttpEndpointRouter{
  @Override
  public String route(List<String> urlList) {
    Random random = new Random(System.currentTimeMillis());
    int index =  random.nextInt(urlList.size());
    return urlList.get(index);
  }
}
