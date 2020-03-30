package me.dancedog.rewardclaim.model;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import me.dancedog.rewardclaim.Mod;
import me.dancedog.rewardclaim.fetch.Request;
import me.dancedog.rewardclaim.fetch.Request.Method;
import me.dancedog.rewardclaim.fetch.Response;

/**
 * Created by DanceDog / Ben on 3/22/20 @ 8:52 PM
 */
@Data
public class RewardSession {

  private String id;
  private String csrfToken;
  private String adVideoId;
  private List<Reward> rewards = new ArrayList<>();
  private String cookies;

  public void claimReward(int option) {
    new Thread(() -> {
      try {
        // TODO: 3/29/20 Actually parse the activeAd id & send it in request
        String urlStr = "https://rewards.hypixel.net/claim-reward/claim"
            + "?id=" + this.id
            + "&option=" + option
            + "&_csrf=" + this.csrfToken
            + "&activeAd=" + "0"
            + "&watchedFallback=false";
        URL url = new URL(urlStr);

        Response response = new Request(url, Method.POST, this.cookies).execute();
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
          Mod.getLogger().info("Successfully claimed reward");
        } else {
          Mod.printWarning("Failed to claim reward. Server sent back a " + response.getStatusCode()
              + " status code. Received the following body:\n" + response.getBody(), null, false);
        }
      } catch (IOException e) {
        Mod.printWarning("IOException during claim reward request", e, false);
      }
    }).start();
  }
}
