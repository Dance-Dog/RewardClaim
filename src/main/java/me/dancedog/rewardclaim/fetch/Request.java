package me.dancedog.rewardclaim.fetch;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import me.dancedog.rewardclaim.RewardClaim;

/**
 * Created by DanceDog / Ben on 3/29/20 @ 8:38 AM
 */
public class Request {

  private static final Map<String, String> DEFAULT_HEADERS = new ConcurrentHashMap<>();

  static {
    DEFAULT_HEADERS.put("Accept", "*/*");
    DEFAULT_HEADERS.put("Content-Length", "0");
    DEFAULT_HEADERS
        .put("User-Agent", RewardClaim.MODID + "/" + RewardClaim.VERSION + " (Minecraft Forge Modification)");
  }

  private final URL url;
  private final Method method;
  private final String cookies;

  public Request(URL url, Method method, @Nullable String cookies) {
    this.url = url;
    this.method = method;
    this.cookies = cookies;
  }

  public Response execute() throws IOException {
    HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
    connection.setRequestMethod(this.method.name());
    connection.setConnectTimeout(10000);

    // Headers
    connection.setRequestProperty("Host", url.getHost());
    for (Entry<String, String> header : DEFAULT_HEADERS.entrySet()) {
      connection.setRequestProperty(header.getKey(), header.getValue());
    }
    if (this.cookies != null) {
      connection.setRequestProperty("Cookie", this.cookies);
    }

    // Response
    int statusCode = connection.getResponseCode();
    String responseCookies = connection.getHeaderField("set-cookie");
    if (!(statusCode >= 200 && statusCode < 300)) {
      return new Response(statusCode, responseCookies, connection.getErrorStream());
    } else {
      return new Response(statusCode, responseCookies, connection.getInputStream());
    }
  }

  public enum Method {
    GET, POST
  }
}
