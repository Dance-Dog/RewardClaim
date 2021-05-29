package me.dancedog.rewardclaim.types;

import lombok.Getter;
import me.dancedog.rewardclaim.RewardClaim;
import net.minecraft.util.ResourceLocation;

/**
 * Hypixel GameTypes used with coin and token rewards
 * <p>
 * Created by DanceDog / Ben on 3/22/20 @ 9:23 PM
 */
@SuppressWarnings("unused")
public enum GameType {
  // Possible reward games
  QUAKECRAFT("Quakecraft"),
  WALLS("Walls"),
  PAINTBALL("Paintball"),
  SURVIVAL_GAMES("Blitz SG"),
  TNTGAMES("TNT Games"),
  VAMPIREZ("VampireZ"),
  WALLS3("Mega Walls"),
  ARCADE("Arcade"),
  ARENA("Arena"),
  UHC("UHC"),
  MCGO("Cops and Crims"),
  BATTLEGROUND("Warlords"),
  SUPER_SMASH("Smash Heroes"),
  GINGERBREAD("Turbo Kart Racers"),
  SKYWARS("SkyWars"),
  TRUE_COMBAT("Crazy Walls"),
  SPEEDUHC("Speed UHC"),
  LEGACY("Classic Games"),
  BEDWARS("Bed Wars"),
  MURDER_MYSTERY("Murder Mystery"),
  BUILD_BATTLE("Build Battle"),
  DUELS("Duels"),

  // Not currently appearing in rewards, but just in case
  HOUSING("Housing"),
  SKYCLASH("SkyClash"),
  PROTOTYPE("Prototype"),
  SKYBLOCK("SkyBlock");

  @Getter
  private final String properName;
  @Getter
  private final ResourceLocation resource;

  GameType(String properName) {
    this.properName = properName;
    this.resource = RewardClaim.getGuiTexture("reward_sub/game_type/" + name() + ".png");
  }

  public static GameType fromName(String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }
    try {
      return valueOf(name.toUpperCase());
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      return null;
    }
  }
}
