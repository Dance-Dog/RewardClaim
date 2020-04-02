package me.dancedog.rewardclaim;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.dancedog.rewardclaim.model.RewardSession;
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
  private static final Pattern SESSION_JSON_PATTERN = Pattern
      .compile("window\\.appData = ['\"](.*?})['\"]");
  private static final JsonParser jsonParser = new JsonParser();

  /**
   * Parse the session data from a daily reward page
   *
   * @param document Document representing the reward page
   * @param cookie Cookie to be passed into the returned session (used to make claim request)
   * @return The session parsed from the provided page
   */
  static RewardSession parseSessionFromRewardPage(Document document, String cookie) {
    JsonObject rawSessionData;

    if (document == null || document.body() == null) {
      rawSessionData = createErrorJson("Document was null");

    } else {
      // The "props" script contains information useful the daily reward react app
      // This includes the rewards data, csrf token, i18n messages, etc
      Element propsScript = document.body().selectFirst("script");
      if (propsScript == null) {
        rawSessionData = createErrorJson("Unable to locate reward data");

      } else {
        String propsScriptContents = propsScript.data();
        rawSessionData = getRawSessionData(propsScriptContents);
        rawSessionData.addProperty("_csrf", getCsrfToken(propsScriptContents));
      }
    }
    return new RewardSession(rawSessionData, cookie);
  }

  /**
   * Extract the reward session's json data from the prop script's contents
   *
   * @param propsScriptContents String contents of the prop script element
   * @return JsonObject of the session's data, or a JsonObject containing an error message if none
   * was found
   */
  private static JsonObject getRawSessionData(String propsScriptContents) {
    Matcher rewardJsonMatcher = SESSION_JSON_PATTERN.matcher(propsScriptContents);
    if (rewardJsonMatcher.find()) {
      return jsonParser.parse(rewardJsonMatcher.group(1)).getAsJsonObject();
    } else {
      return createErrorJson("Unable to locate reward data");
    }
  }

  /**
   * Extract the csrf token from the prop script's contents
   *
   * @param propsScriptContents String contents of the prop script element
   * @return Csrf token string, or null if none was found
   */
  private static String getCsrfToken(String propsScriptContents) {
    Matcher csrfTokenMatcher = CSRF_TOKEN_PATTERN.matcher(propsScriptContents);
    if (csrfTokenMatcher.find()) {
      return csrfTokenMatcher.group(1);
    }
    return null;
  }

  /**
   * Utility method to create a JsonObject containing an error message (same format that rewards
   * page uses)
   *
   * @param errorMsg
   * @return JsonObject containing the message
   */
  private static JsonObject createErrorJson(String errorMsg) {
    JsonObject json = new JsonObject();
    json.addProperty("error", errorMsg);
    return json;
  }

  private RewardScraper() {
  }
}
