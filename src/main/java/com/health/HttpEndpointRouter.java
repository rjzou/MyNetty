package com.health;

import java.util.List;

public interface HttpEndpointRouter {
  String route(List<String> urlList);
}
