package me.dancedog.rewardclaim.model;

import lombok.Data;
import me.dancedog.rewardclaim.types.CardRarity;
import net.minecraft.util.ResourceLocation;

/**
 * Created by DanceDog / Ben on 3/21/20 @ 10:10 PM
 */
@SuppressWarnings("unused")
@Data
public class Reward {

  // Card text
  private String title;
  private String subtitle; // Amount / Skull Name (ie "Rubik's Cube") / Armor Piece (ie "Leggings") / Vanity Item Name (ie "Moustache Emote")
  private String description;
  private CardRarity rarity;

  // Assets
  private ResourceLocation typeIcon; // Main image of the card
  private ResourceLocation gameIcon; // GameType icon of coin/token's game
  private ResourceLocation itemBg; // Background of the skull/armor's item preview
  private ResourceLocation itemIcon; // Preview icon of skull/armor's icon
}
