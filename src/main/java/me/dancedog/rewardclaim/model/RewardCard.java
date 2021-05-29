package me.dancedog.rewardclaim.model;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import me.dancedog.rewardclaim.RewardClaim;
import me.dancedog.rewardclaim.types.CardRarity;
import me.dancedog.rewardclaim.types.GameType;
import me.dancedog.rewardclaim.types.HousingSkull;
import me.dancedog.rewardclaim.types.RewardType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

/**
 * Created by DanceDog / Ben on 3/31/20 @ 6:51 PM
 */
public class RewardCard {

  private static final String RARITY_JSON_KEY = "rarity";
  private static final String TYPE_JSON_KEY = "reward";
  private static final String VANITYKEY_JSON_KEY = "key";
  private static final String GAMETYPE_JSON_KEY = "gameType";
  private static final String HOUSINGPKG_JSON_KEY = "package";
  private static final String INTLIST_JSON_KEY = "intlist";
  private static final String AMOUNT_JSON_KEY = "amount";

  /**
   * This card's reward rarity value
   */
  @NonNull
  @Getter
  private final CardRarity rarity;

  /**
   * The base type of this card's reward
   */
  @Getter
  private RewardType rewardType;

  /**
   * The minigame that this reward is for Only applies to coin and token rewards
   */
  @Getter
  private GameType gameType;

  /**
   * The main text to display on the bottom of this card
   */
  @Getter
  private String title;

  /**
   * The text to display under the title
   */
  @Getter
  private String subtitle;

  /**
   * A description of this card's rewardType
   */
  @Getter
  private String description;

  /**
   * The large texture to display in the center of this card
   */
  @Getter
  private ResourceLocation typeIcon;

  /**
   * The texture of the smaller icon to display next to the typeIcon Only applies to housing blocks
   * and vanity suit pieces
   */
  @Getter
  private ResourceLocation itemIcon;

  /**
   * The texture to display behind the item icon Only applies to housing blocks and vanity suit
   * pieces
   */
  @Getter
  private ResourceLocation itemIconBg;

  RewardCard(JsonObject raw) {
    if (!validate(raw)) {
      this.rarity = CardRarity.ERROR;
      return;
    }
    this.rewardType = RewardType.fromName(raw.get(TYPE_JSON_KEY).getAsString());
    this.rarity = CardRarity.fromName(raw.get(RARITY_JSON_KEY).getAsString());
    if (this.rewardType == RewardType.COINS || rewardType == RewardType.TOKENS) {
      this.gameType = GameType.fromName(raw.get(GAMETYPE_JSON_KEY).getAsString());
    }

    setUnformattedTitle(raw);
    setUnformattedSubtitle(raw);
    setUnformattedDescription(raw);
    setTypeIcon(raw);
    setItemIcons(raw);

    // Format text
    this.title = this.gameType != null
        ? I18n.format(this.title, this.gameType.getProperName())
        : I18n.format(this.title);
    this.subtitle = I18n.format(this.subtitle);
    this.description = this.gameType != null
        ? I18n.format(this.description, this.gameType.getProperName())
        : I18n.format(this.description);
  }

  /**
   * Determines this card's title text (unformatted)
   *
   * @param raw Raw JSON represented by this card
   */
  private void setUnformattedTitle(JsonObject raw) {
    if (this.rewardType == RewardType.ADD_VANITY) {
      String key = raw.get(VANITYKEY_JSON_KEY).getAsString();
      if (key.contains("suit")) {
        this.title = "vanity." + key.replaceAll("_([A-Za-z]+)$", "");
      } else {
        this.title = "vanity." + key;
      }
    } else {
      this.title = this.rewardType.getTitle();
    }
  }

  /**
   * Determines this card's subtitle text (unformatted)
   *
   * @param raw Raw JSON represented by this card
   */
  private void setUnformattedSubtitle(JsonObject raw) {
    if (this.rewardType == RewardType.ADD_VANITY) {
      String key = raw.get(VANITYKEY_JSON_KEY).getAsString();
      if (key.contains("suit")) {
        this.subtitle = "vanity.armor." + key.replaceAll("([A-Za-z]+_)(?!$)", "");
      } else {
        this.subtitle = this.title;
      }

    } else if (this.rewardType == RewardType.HOUSING_PACKAGE) {
      this.subtitle = "housing.skull." + getHousingSkull(raw).name().toLowerCase();

    } else if (raw.has(INTLIST_JSON_KEY) && !raw.has(AMOUNT_JSON_KEY)) {
      this.subtitle = String.valueOf(raw.get(INTLIST_JSON_KEY).getAsJsonArray().size());

    } else {
      this.subtitle = raw.get(AMOUNT_JSON_KEY).getAsString();
    }
  }

  /**
   * Determines this card's description text (unformatted)
   *
   * @param raw Raw JSON represented by this card
   */
  private void setUnformattedDescription(JsonObject raw) {
    if (this.rewardType == RewardType.ADD_VANITY) {
      String key = raw.get(VANITYKEY_JSON_KEY).getAsString();
      if (key.contains("suit")) {
        description = "vanity.suits";
      } else if (key.contains("emote")) {
        description = "vanity.emotes";
      } else if (key.contains("taunt")) {
        description = "vanity.gestures";
      } else {
        description = "vanity.unknown";
      }

    } else {
      description = title;
    }

    description += ".description";
  }

  /**
   * Determines this card's main reward texture
   *
   * @param raw Raw JSON represented by this card
   */
  private void setTypeIcon(JsonObject raw) {
    String textureName;
    if (this.rewardType == RewardType.ADD_VANITY) {
      String key = raw.get(VANITYKEY_JSON_KEY).getAsString();
      if (key.contains("suit")) {
        textureName = "SUIT";
      } else if (key.contains("emote")) {
        textureName = "EMOTE";
      } else if (key.contains("taunt")) {
        textureName = "GESTURE";
      } else {
        textureName = "UNKNOWN";
      }
      textureName = "VANITY_" + textureName;

    } else if (this.rewardType == RewardType.HOUSING_PACKAGE) {
      textureName = "HOUSING_" + getHousingSkull(raw).getGroup().name();

    } else if (this.rewardType == RewardType.TOKENS) {
      textureName = "TOKENS_" + gameType.name();

    } else {
      textureName = this.rewardType.name();
    }

    this.typeIcon = RewardClaim.getGuiTexture("reward_base/" + textureName + ".png");
  }

  /**
   * Determines this card's mini item texture & background
   *
   * @param raw Raw JSON represented by this card
   */
  private void setItemIcons(JsonObject raw) {
    if (this.rewardType == RewardType.ADD_VANITY) {
      String key = raw.get(VANITYKEY_JSON_KEY).getAsString();
      if (key.contains("suit")) {
        this.itemIcon = RewardClaim.getGuiTexture(
            "reward_sub/armor/" + key.replaceAll("([A-Za-z]+_)(?!$)", "").toUpperCase() + ".png");
        this.itemIconBg = RewardClaim.getGuiTexture("bg_armor.png");
      }

    } else if (this.rewardType == RewardType.HOUSING_PACKAGE) {
      this.itemIcon = getHousingSkull(raw).getResource();
      this.itemIconBg = RewardClaim.getGuiTexture("bg_housing.png");
    }
  }

  /**
   * Ensure that the data this card represents is valid
   *
   * @param raw Raw JSON represented by this card
   * @return Whether or not the passed data can be properly displayed
   */
  private static boolean validate(JsonObject raw) {
    if (raw == null) {
      return false;
    }
    if (!raw.has(RARITY_JSON_KEY) || !raw.has(TYPE_JSON_KEY)) {
      return false;
    }

    CardRarity rarity = CardRarity.fromName(raw.get(RARITY_JSON_KEY).getAsString());
    RewardType rewardType = RewardType.fromName(raw.get(TYPE_JSON_KEY).getAsString());
    if (rarity == null || rewardType == null) {
      return false;
    }
    switch (rewardType) {
      case ADD_VANITY:
        if (!raw.has(VANITYKEY_JSON_KEY)) {
          return false;
        }
        break;

      case TOKENS:
      case COINS:
        if (!raw.has(AMOUNT_JSON_KEY)) {
          return false;
        }
        if (!raw.has(GAMETYPE_JSON_KEY)) {
          return false;
        }
        if (GameType.fromName(raw.get(GAMETYPE_JSON_KEY).getAsString()) == null) {
          return false;
        }
        break;

      case HOUSING_PACKAGE:
        if (!raw.has(HOUSINGPKG_JSON_KEY)) {
          return false;
        }
        if (getHousingSkull(raw) == null) {
          return false;
        }
        break;

      default:
        if (!raw.has(AMOUNT_JSON_KEY) && !raw.has(INTLIST_JSON_KEY)) {
          return false;
        }
    }

    return true;
  }

  /**
   * Utility method to get a housing skull type from a reward object
   *
   * @param raw Raw reward object to get the skull from
   * @return The raw reward's housing skull, or null if there was no skull with that name
   */
  private static HousingSkull getHousingSkull(JsonObject raw) {
    return HousingSkull.fromName(
        raw.get(HOUSINGPKG_JSON_KEY).getAsString()
            .replaceAll("specialoccasion_reward_card_skull_|'", ""));
  }
}
