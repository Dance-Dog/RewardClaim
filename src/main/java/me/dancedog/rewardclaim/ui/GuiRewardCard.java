package me.dancedog.rewardclaim.ui;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import me.dancedog.rewardclaim.Mod;
import me.dancedog.rewardclaim.model.RewardCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Created by DanceDog / Ben on 3/22/20 @ 5:22 PM
 */
class GuiRewardCard extends Gui {

  static final int CARD_WIDTH = 110;
  static final int CARD_HEIGHT = (int) Math.ceil(CARD_WIDTH * 1.43);
  private static final Minecraft mc = Minecraft.getMinecraft();
  private static final FontRenderer fontRendererObj = mc.fontRendererObj;
  private static final ResourceLocation tooltipTexture = Mod
      .getGuiTexture("cards/card_tooltip.png");

  private final RewardCard cardInfo;
  private int posX = 0;
  private int posY = 0;

  @Setter
  private boolean isFlipped = false;
  @Setter
  private boolean isEnabled = false;
  @Getter
  private List<String> tooltipLines;
  @Setter
  private boolean showTooltip = false;

  GuiRewardCard(RewardCard cardInfo) {
    this.cardInfo = cardInfo;

    tooltipLines = new ArrayList<>();
    tooltipLines
        .add(EnumChatFormatting.WHITE + "" + EnumChatFormatting.ITALIC + cardInfo.getDescription());
  }

  void initGui(int posX, int posY) {
    this.posX = posX;
    this.posY = posY;
  }

  void drawRewardCard(int mouseX, int mouseY) {
    if (isEnabled && isHovered(mouseX, mouseY)) {
      isFlipped = true;
    }
    drawCardTextures();

    // Draw the text onto the card's front
    if (isFlipped) {
      // Card title
      GL11.glPushMatrix();
      double titleScale =
          64d / fontRendererObj.getStringWidth(cardInfo.getTitle()); // Max title length is 65px
      GL11.glScaled(titleScale, titleScale, titleScale);
      drawCenteredString(
          fontRendererObj,
          cardInfo.getTitle(),
          (int) ((posX + CARD_WIDTH / 2d) / titleScale),
          (int) ((posY + 111) / titleScale),
          0xffffff);
      GL11.glPopMatrix();

      // Card subtitle
      GL11.glPushMatrix();
      int subtitleWidth = fontRendererObj.getStringWidth(cardInfo.getSubtitle());
      double subtitleScale;
      if (subtitleWidth > 74) {
        subtitleScale = 74d / subtitleWidth;
      } else if (subtitleWidth < 67) {
        subtitleScale = 1.1;
      } else {
        subtitleScale = 1;
      }
      GL11.glScaled(subtitleScale, subtitleScale, subtitleScale);
      drawCenteredString(
          fontRendererObj,
          cardInfo.getSubtitle(),
          (int) ((posX + CARD_WIDTH / 2d) / subtitleScale),
          (int) ((posY + 135) / subtitleScale),
          cardInfo.getRarity().getSubtitleColor());
      GL11.glPopMatrix();
    }

    drawTooltip();

    // Makes the card darker if the ready button is not yet pressed (same appearance as being behind drawDefaultBackground)
    if (!this.isEnabled) {
      drawGradientRect(posX, posY, posX + CARD_WIDTH, posY + CARD_HEIGHT, -1072689136, -804253680);
      GlStateManager
          .enableBlend(); // drawGradientRect disables this, causing the screen to be tinted white
    }
  }

  private void drawCardTextures() {
    GL11.glPushMatrix();
    GL11.glColor3f(1f, 1f, 1f);
    GL11.glEnable(GL11.GL_BLEND); // allow transparency in textures

    // Draw the card itself
    mc.getTextureManager().bindTexture(
        this.isFlipped ?
            cardInfo.getRarity().getFrontResource()
            : cardInfo.getRarity().getBackResource());
    drawModalRectWithCustomSizedTexture(posX, posY, 0, 0, CARD_WIDTH, CARD_HEIGHT, CARD_WIDTH,
        CARD_HEIGHT);

    // Draw the card's front side
    if (this.isFlipped) {
      /*
      Draw Images
       */

      // Main card image
      mc.getTextureManager().bindTexture(cardInfo.getTypeIcon());
      drawModalRectWithCustomSizedTexture(posX, posY, 0, 0, CARD_WIDTH, CARD_HEIGHT, CARD_WIDTH,
          CARD_HEIGHT);

      // Game icon for coins
      if (cardInfo.getGameType() != null) {
        mc.getTextureManager().bindTexture(cardInfo.getGameType().getResource());
        drawModalRectWithCustomSizedTexture(posX + 65, posY + 63, 0, 0, 21, 21, 21, 21);
      }

      // Item icon for armor & housing blocks
      if (cardInfo.getItemIcon() != null) {
        // Background
        mc.getTextureManager().bindTexture(cardInfo.getItemIconBg());
        drawModalRectWithCustomSizedTexture(posX + 64, posY + 63, 0, 0, 28, 28, 28, 28);

        // Draw the item inside the background
        mc.getTextureManager().bindTexture(cardInfo.getItemIcon());
        drawModalRectWithCustomSizedTexture(posX + 67, posY + 66, 0, 0, 22, 22, 22, 22);
      }
    }
    GL11.glPopMatrix();
  }

  private void drawTooltip() {
    if (!showTooltip) {
      return;
    }

    GL11.glColor3f(1f, 1f, 1f);
    GL11.glEnable(GL11.GL_BLEND);

    int tooltipX = posX - 16;
    int tooltipY = Math.max(posY - 48, 0);
    float scaleFactor = 0.8f;
    int textIndent = (int) (tooltipX / scaleFactor) + 8;

    mc.getTextureManager().bindTexture(tooltipTexture);
    drawModalRectWithCustomSizedTexture(tooltipX, tooltipY, 0, 0, 143, 51, 143, 51);

    GL11.glPushMatrix();
    GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);
    // Rarity text
    fontRendererObj.drawStringWithShadow(
        EnumChatFormatting.GOLD + "Rarity: " +
            cardInfo.getRarity().getRarityColor() + "" + EnumChatFormatting.BOLD +
            cardInfo.getRarity().name(),
        textIndent,
        (tooltipY / scaleFactor) + 9,
        0);

    // Description text
    fontRendererObj.drawSplitString(
        cardInfo.getDescription(),
        textIndent,
        (int) (tooltipY / scaleFactor) + 31,
        168,
        0xffffff);
    GL11.glPopMatrix();
  }

  boolean canShowTooltip(int mouseX, int mouseY) {
    return isEnabled
        && isFlipped
        && isHovered(mouseX, mouseY);
  }

  boolean isHovered(int mouseX, int mouseY) {
    return mouseX >= posX
        && mouseX <= posX + CARD_WIDTH
        && mouseY >= posY
        && mouseY <= posY + CARD_HEIGHT;
  }
}
