package me.dancedog.rewardclaim;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.dancedog.rewardclaim.model.Reward;
import me.dancedog.rewardclaim.model.RewardSession;
import me.dancedog.rewardclaim.types.CardRarity;
import me.dancedog.rewardclaim.types.GameType;
import me.dancedog.rewardclaim.types.HousingSkullType;
import me.dancedog.rewardclaim.types.RewardType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

/**
 * Utility methods for parsing the JSON scraped from the rewards page
 * <p>
 * Created by DanceDog / Ben on 3/23/20 @ 3:23 PM
 */
final class SessionDataParser {

  private SessionDataParser() {
  }

  private static final String VANITY_SUIT_PIECE_SUFFIX_REPLACEMENT_REGEX = "_([A-Za-z]+)$"; // Replaces "_leggings", etc
  private static final String VANITY_SUIT_PIECE_PREFIX_REPLACEMENT_REGEX = "([A-Za-z]+_)(?!$)"; // Replaces everything except "leggings", etc
  private static final String HOUSING_BLOCK_REPLACEMENT_REGEX = "specialoccasion_reward_card_skull_|'";

  static RewardSession parseRewardSessionData(JsonObject input) {
    JsonElement rawSessionJson = input.get("session_data");
    JsonElement csrfTokenElement = input.get("csrf_token");
    if (rawSessionJson == null
        || !rawSessionJson.isJsonObject()
        || csrfTokenElement == null
        || !csrfTokenElement.isJsonPrimitive()
        || rawSessionJson.getAsJsonObject().get("rewards") == null
        || !rawSessionJson.getAsJsonObject().get("rewards").isJsonArray()) {
      Mod.printWarning("Invalid reward session data", null, true);
      return null;
    }

    // Create session
    RewardSession session = new RewardSession();
    session.setId(rawSessionJson.getAsJsonObject().get("id").getAsString());
    session.setCsrfToken(csrfTokenElement.getAsString());
    session
        .setCookies(input.get("_cookie").getAsString()); // Our own value passed in through the JSON

    JsonArray rawRewardArray = rawSessionJson.getAsJsonObject().get("rewards")
        .getAsJsonArray();
    for (JsonElement item : rawRewardArray) {
      if (!item.isJsonObject()) {
        Mod.printWarning("Reward item was not a JsonObject", null, true);
        return null;
      }
      JsonObject rawRewardJson = item.getAsJsonObject();

      // Create reward card & related variables
      Reward card = new Reward();
      RewardType rewardType = RewardType.fromName(getStringFromJsonObject("reward", rawRewardJson));
      GameType gameType = GameType.fromName(getStringFromJsonObject("gameType", rawRewardJson));
      String vanityKey = getStringFromJsonObject("key", rawRewardJson).toLowerCase();
      HousingSkullType housingSkullType = HousingSkullType.UNKNOWN;
      if (rewardType == RewardType.HOUSING_PACKAGE) {
        String housingPackageName = getStringFromJsonObject("package", rawRewardJson)
            .replaceAll(HOUSING_BLOCK_REPLACEMENT_REGEX, "");
        housingSkullType = HousingSkullType.fromName(housingPackageName);
      }

      // Set card text
      card.setRarity(CardRarity.fromName(getStringFromJsonObject("rarity", rawRewardJson)));
      card.setTitle(getTitle(rewardType, gameType, vanityKey));
      card.setSubtitle(getSubtitle(rewardType, housingSkullType, vanityKey, rawRewardJson));
      card.setDescription(getDescription(rewardType, gameType, vanityKey));

      // Set card images
      card.setTypeIcon(getTypeIcon(rewardType, gameType, vanityKey, housingSkullType));
      if (rewardType == RewardType.COINS) {
        card.setGameIcon(gameType.getResource());
      }
      if (rewardType == RewardType.ADD_VANITY && vanityKey.contains("suit")) {
        card.setItemBg(Mod.getGuiTexture("bg_armor.png"));
        String armorType = vanityKey.replaceAll(VANITY_SUIT_PIECE_PREFIX_REPLACEMENT_REGEX, "")
            .toUpperCase();
        card.setItemIcon(Mod.getGuiTexture("reward_sub/armor/" + armorType + ".png"));

      } else if (rewardType == RewardType.HOUSING_PACKAGE) {
        card
            .setItemBg(Mod.getGuiTexture("bg_housing.png"));
        card.setItemIcon(housingSkullType.getResource());
      }

      session.getRewards().add(card);
    }
    return session;
  }

  /**
   * Determine a reward's title (top text on the card)
   *
   * @param rewardType Type of reward
   * @param gameType   GameType of reward (coins/tokens)
   * @param vanityKey  Vanity key of the reward (if add_vanity)
   * @return The reward's formatted title
   */
  private static String getTitle(RewardType rewardType, GameType gameType, String vanityKey) {
    String titleKey;

    if (rewardType == RewardType.COINS || rewardType == RewardType.TOKENS) {
      return I18n.format(rewardType.getTitleKey(), gameType.getProperName());

    } else if (rewardType == RewardType.ADD_VANITY) {
      titleKey = "vanity.";
      if (vanityKey.contains("suit")) {
        titleKey += vanityKey.replaceAll(VANITY_SUIT_PIECE_SUFFIX_REPLACEMENT_REGEX, "");
      } else {
        titleKey += vanityKey;
      }

    } else {
      titleKey = rewardType.getTitleKey();
    }

    return I18n.format(titleKey);
  }

  /**
   * Determine a reward's subtitle (bottom text on the card)
   *
   * @param rewardType   Type of reward
   * @param housingSkull GameType of reward (coins/tokens)
   * @param vanityKey    Vanity key of the reward (if add_vanity)
   * @param rewardJson   Original reward session JsonObject
   * @return The reward's formatted subtitle
   */
  private static String getSubtitle(RewardType rewardType, HousingSkullType housingSkull,
      String vanityKey, JsonObject rewardJson) {
    String subtitleKey;

    if (rewardType == RewardType.HOUSING_PACKAGE) {
      subtitleKey = "housing.skull." + housingSkull.name().toLowerCase();

    } else if (rewardType == RewardType.ADD_VANITY) {
      if (vanityKey.contains("suit")) {
        subtitleKey =
            "vanity.armor." + vanityKey.replaceAll(VANITY_SUIT_PIECE_PREFIX_REPLACEMENT_REGEX, "");
      } else {
        subtitleKey = "vanity." + vanityKey;
      }

    } else {
      subtitleKey = "" + rewardJson.get("amount").getAsInt();
    }

    return I18n.format(subtitleKey);
  }

  /**
   * Determine a reward's description (bottom of card's tooltip)
   *
   * @param rewardType Type of reward
   * @param gameType   gameType GameType of reward (coins/tokens)
   * @param vanityKey  Vanity key of the reward (if add_vanity)
   * @return The reward's formatted description
   */
  private static String getDescription(RewardType rewardType, GameType gameType, String vanityKey) {
    String descriptionKeyBase;

    if (rewardType == RewardType.COINS || rewardType == RewardType.TOKENS) {
      return I18n.format(rewardType.getTitleKey() + ".description", gameType.getProperName());

    } else if (rewardType == RewardType.ADD_VANITY) {
      if (vanityKey.contains("suit")) {
        descriptionKeyBase = "vanity.suits";
      } else if (vanityKey.contains("emote")) {
        descriptionKeyBase = "vanity.emotes";
      } else if (vanityKey.contains("taunt")) {
        descriptionKeyBase = "vanity.gestures";
      } else {
        descriptionKeyBase = "vanity.unknown";
      }

    } else {
      descriptionKeyBase = rewardType.getTitleKey();
    }

    return I18n.format(descriptionKeyBase + ".description");
  }

  private static ResourceLocation getTypeIcon(RewardType rewardType, GameType gameType,
      String vanityKey, HousingSkullType housingSkull) {
    ResourceLocation typeIcon;

    if (rewardType == RewardType.TOKENS) {
      if (gameType == GameType.SKYWARS || gameType == GameType.LEGACY) {
        typeIcon = Mod.getGuiTexture("reward_base/TOKENS_" + gameType.name() + ".png");
      } else {
        typeIcon = RewardType.COINS.getIcon();
      }

    } else if (rewardType == RewardType.ADD_VANITY) {
      String typeName;
      if (vanityKey.contains("suit")) {
        typeName = "SUIT";
      } else if (vanityKey.contains("emote")) {
        typeName = "EMOTE";
      } else if (vanityKey.contains("taunt")) {
        typeName = "GESTURE";
      } else {
        typeName = "UNKNOWN";
      }
      typeIcon = Mod.getGuiTexture("reward_base/VANITY_" + typeName + ".png");

    } else if (rewardType == RewardType.HOUSING_PACKAGE) {
      typeIcon = housingSkull.getGroup().getResource();
    } else {
      typeIcon = rewardType.getIcon();
    }

    return typeIcon;
  }

  private static String getStringFromJsonObject(String memberName, JsonObject jsonObject) {
    JsonElement member = jsonObject.get(memberName);
    if (member == null) {
      return "";
    }
    return member.getAsString();
  }
}