package me.dancedog.rewardclaim;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Utility methods for scraping from the rewards page
 * <p>
 * Created by DanceDog / Ben on 3/23/20 @ 1:22 PM
 */
class RewardScraper {

  private static final Pattern CSRF_TOKEN_PATTERN = Pattern
      .compile("window\\.securityToken = ['\"](.*?)['\"]");
  private static final Pattern REWARD_JSON_PATTERN = Pattern
      .compile("window\\.appData = ['\"](.*?})['\"]");
  private static final JsonParser jsonParser = new JsonParser();

  /**
   * Read the reward session json from a daily reward page
   *
   * @param document Document to get the session json from
   * @return JsonObject of the session data on the provided document, or null if
   * @throws JsonParseException    If the document's session data was not valid json
   * @throws IllegalStateException If the document's session data was not a JsonObject
   */
  static JsonObject parseRewardPage(Document document) {
    if (document == null || document.body() == null) {
      return createErrorJson("Document was null");
    }
    Element infoScript = document.body().selectFirst("script");
    if (infoScript == null) {
      return createErrorJson("Unable to locate reward data");
    }
    JsonObject data = new JsonObject();

    // Get the JSON data for the reward session
    String infoScriptContents = infoScript.data();
    Matcher rewardJsonMatcher = REWARD_JSON_PATTERN.matcher(infoScriptContents);
    if (rewardJsonMatcher.find()) {
      JsonObject parsedRewardJson = jsonParser.parse(rewardJsonMatcher.group(1)).getAsJsonObject();
      JsonElement errorMessage = parsedRewardJson.get("error");
      if (errorMessage != null) {
        return createErrorJson(errorMessage.getAsString());
      }
      data.add("session_data", parsedRewardJson);
    } else {
      return createErrorJson("Unable to locate reward data");
    }

    // Get the CSRF token needed to claim the reward
    Matcher csrfTokenMatcher = CSRF_TOKEN_PATTERN.matcher(infoScriptContents);
    if (csrfTokenMatcher.find()) {
      data.addProperty("csrf_token", csrfTokenMatcher.group(1));
    } else {
      return createErrorJson("Unable to locate csrf token");
    }

    return data;
  }

  private static JsonObject createErrorJson(String errorMsg) {
    JsonObject json = new JsonObject();
    json.addProperty("error", errorMsg);
    return json;
  }

  private RewardScraper() {
  }
}
