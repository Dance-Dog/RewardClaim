package me.dancedog.rewardclaim.types;

import lombok.Getter;
import me.dancedog.rewardclaim.Mod;
import net.minecraft.util.ResourceLocation;

/**
 * The specific housing block (skull) rewarded to a player
 * <p>
 * Created by DanceDog / Ben on 3/23/20 @ 4:50 PM
 */
@SuppressWarnings("unused")
public enum HousingSkull {
  // Fallback
  UNKNOWN(HousingSkullGroup.RED),

  // Red
  RED_TREASURE_CHEST(HousingSkullGroup.RED),
  GOLD_NUGGET(HousingSkullGroup.RED),
  POT_O_GOLD(HousingSkullGroup.RED),
  RUBIKS_CUBE(HousingSkullGroup.RED),
  PIGGY_BANK(HousingSkullGroup.RED),
  HEALTH_POTION(HousingSkullGroup.RED),

  // Green
  GREEN_TREASURE_CHEST(HousingSkullGroup.GREEN),
  COIN_BAG(HousingSkullGroup.GREEN),
  ORNAMENTAL_HELMET(HousingSkullGroup.GREEN),
  POCKET_GALAXY(HousingSkullGroup.GREEN),
  MYSTIC_PEARL(HousingSkullGroup.GREEN),
  AGILITY_POTION(HousingSkullGroup.GREEN),

  // Blue
  BLUE_TREASURE_CHEST(HousingSkullGroup.BLUE),
  GOLDEN_CHALICE(HousingSkullGroup.BLUE),
  JEWELERY_BOX(HousingSkullGroup.BLUE),
  CROWN(HousingSkullGroup.BLUE),
  MOLTEN_CORE(HousingSkullGroup.BLUE),
  MANA_POTION(HousingSkullGroup.BLUE);

  @Getter
  private final HousingSkullGroup group;
  @Getter
  private final ResourceLocation resource;

  HousingSkull(HousingSkullGroup group) {
    this.group = group;
    this.resource = Mod.getGuiTexture("reward_sub/housing_skull/" + name().toUpperCase() + ".png");
  }

  public static HousingSkull fromName(String name) {
    if (name == null || name.isEmpty()) {
      return UNKNOWN;
    }

    try {
      return valueOf(name.toUpperCase());
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      return UNKNOWN;
    }
  }
}
