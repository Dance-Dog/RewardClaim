package me.dancedog.rewardclaim.fetch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import lombok.Getter;
import me.dancedog.rewardclaim.RewardClaim;

/**
 * Created by DanceDog / Ben on 3/29/20 @ 9:02 AM
 */
public class Response {

  @Getter
  private final int statusCode;
  @Getter
  private final String newCookies;
  @Getter
  private final String body;

  Response(int statusCode, String newCookies, InputStream inputStream) {
    this.statusCode = statusCode;
    this.newCookies = newCookies;

    String body1;
    try {
      body1 = getStringFromInputStream(inputStream);
    } catch (IOException e) {
      body1 = null;
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        RewardClaim.printWarning("Failed to close InputStream", e, false);
        e.printStackTrace();
      }
    }
    this.body = body1;
  }

  private static String getStringFromInputStream(InputStream stream) throws IOException {
    BufferedReader bodyBufferedRead = new BufferedReader(new InputStreamReader(stream));
    StringBuilder bodyBuilder = new StringBuilder();
    String line;
    while ((line = bodyBufferedRead.readLine()) != null) {
      bodyBuilder.append(line);
    }
    return bodyBuilder.toString();
  }
}
