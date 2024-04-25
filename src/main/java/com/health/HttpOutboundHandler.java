package com.health;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.rtsp.RtspResponseStatuses.OK;

public class HttpOutboundHandler {

  private CloseableHttpAsyncClient httpclient;
  private ExecutorService proxyService;
  private List<String> backendUrls;

  HttpResponseFilter filter = new HeaderHttpResponseFilter();
  HttpEndpointRouter router = new RandomHttpEndpointRouter();

  public HttpOutboundHandler(List<String> backends) {

    this.backendUrls = backends.stream().map(this::formatUrl).collect(Collectors.toList());

    int cores = Runtime.getRuntime().availableProcessors();
    long keepAliveTime = 1000;
    int queueSize = 2048;
    RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();//.DiscardPolicy();
    proxyService = new ThreadPoolExecutor(cores, cores,
      keepAliveTime, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(queueSize),
      new NamedThreadFactory("proxyService"), handler);

    IOReactorConfig ioConfig = IOReactorConfig.custom()
      .setConnectTimeout(1000)
      .setSoTimeout(1000)
      .setIoThreadCount(cores)
      .setRcvBufSize(32 * 1024)
      .build();

    httpclient = HttpAsyncClients.custom().setMaxConnTotal(40)
      .setMaxConnPerRoute(8)
      .setDefaultIOReactorConfig(ioConfig)
      .setKeepAliveStrategy((response, context) -> 6000)
      .build();
    httpclient.start();
  }

  private String formatUrl(String backend) {
    return backend.endsWith("/") ? backend.substring(0, backend.length() - 1) : backend;
  }

  public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, HttpRequestFilter filter) {
    String backendUrl = router.route(this.backendUrls);
    final String url = backendUrl + fullRequest.uri();

    filter.filter(fullRequest, ctx);
    proxyService.submit(() -> fetchGet(fullRequest, ctx, url));
  }

  private String parseJosnRequest(FullHttpRequest request) {
    ByteBuf jsonBuf = request.content();
    String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
    return jsonStr;
  }

  private void fetchGet(final FullHttpRequest inbound, final ChannelHandlerContext ctx, final String url) {
    System.out.println(url);
    final HttpPost httpPost = new HttpPost(url);
//    httpPost.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
    httpPost.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
    httpPost.setHeader("test", inbound.headers().get("test"));
    httpPost.setHeader("Accept", "application/json");
    httpPost.setHeader("Content-type", "application/json");

    try {
      StringEntity entity = new StringEntity(parseJosnRequest(inbound));
      httpPost.setEntity(entity);
    } catch (Exception e) {
      e.printStackTrace();
    }

    httpclient.execute(httpPost, new FutureCallback<HttpResponse>() {
      @Override
      public void completed(final HttpResponse endpointResponse) {
        try {
          handleResponse(inbound, ctx, endpointResponse);
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          ReferenceCountUtil.release(inbound);
        }
      }

      @Override
      public void failed(final Exception ex) {
        httpPost.abort();
        ex.printStackTrace();
      }

      @Override
      public void cancelled() {
        httpPost.abort();
      }
    });
  }

  private void handleResponse(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, final HttpResponse endpointResponse) throws Exception {
    FullHttpResponse response = null;
    try {

      byte[] body = EntityUtils.toByteArray(endpointResponse.getEntity());
      response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body));
      response.headers().set("Content-Type", "application/json");
      response.headers().setInt("Content-Length", Integer.parseInt(endpointResponse.getFirstHeader("Content-Length").getValue()));
      filter.filter(response);

    } catch (Exception e) {
      e.printStackTrace();
      response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
      exceptionCaught(ctx, e);
    } finally {
      if (fullRequest != null) {
        if (!HttpUtil.isKeepAlive(fullRequest)) {
          ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
          ctx.write(response);
        }
      }
      ctx.flush();
    }

  }

  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}
