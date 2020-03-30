package me.dancedog.rewardclaim.ui;

import java.io.IOException;
import lombok.Getter;
import me.dancedog.rewardclaim.model.RewardSession;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

/**
 * Created by DanceDog / Ben on 3/22/20 @ 10:43 AM
 */
public class GuiScreenRewardSession extends GuiScreen {

  private State guiState = State.INITIAL;
  private RewardSession session;

  private GuiRewardCard[] cards = new GuiRewardCard[3];
  private GuiButton closeButton;
  private GuiButton activationButton; // used to get the cursor out of center

  public GuiScreenRewardSession(RewardSession session) {
    this.session = session;
  }

  @Override
  public void initGui() {
    guiState = State.INITIAL;

    int middleCardX = width / 2 - (GuiRewardCard.CARD_WIDTH
        / 2); // used with spacing to offset first and last cards
    int cardY = height / 2 - (GuiRewardCard.CARD_HEIGHT / 2);
    int cardSpacing = 20;

    // Reward cards
    for (int i = 0; i < 3; i++) {
      int posX;
      switch (i) {
        case 0:
          posX = middleCardX - GuiRewardCard.CARD_WIDTH - cardSpacing;
          break;
        case 1:
          posX = middleCardX;
          break;
        case 2:
          posX = middleCardX + GuiRewardCard.CARD_WIDTH + cardSpacing;
          break;
        default:
          posX = 0;
      }
      this.cards[i] = new GuiRewardCard(
          session.getRewards().get(i),
          posX,
          cardY
      );
    }

    // Close button ("X")
    int squareButtonSize = 20;
    this.closeButton = new GuiButton(
        0,
        this.width - squareButtonSize - 5,
        5,
        squareButtonSize,
        squareButtonSize,
        "X");
    this.activationButton = new GuiButton(
        1,
        this.width / 2 - 100,
        this.height - 25,
        200,
        20,
        "Ready?"
    );

    this.buttonList.add(closeButton);
    this.buttonList.add(activationButton);
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    // Draw bg & buttons
    this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
    super.drawScreen(mouseX, mouseY, partialTicks);

    // Determine tooltip states
    if (guiState != State.INITIAL) {
      for (GuiRewardCard rewardCard : cards) {
        rewardCard.setShowTooltip(rewardCard.canShowTooltip(mouseX, mouseY));
      }
    }

    // Draw all 3 cards
    for (GuiRewardCard rewardCard : cards) {
      rewardCard.drawRewardCard(mouseX, mouseY);
    }

    drawCenteredString(fontRendererObj,
        EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + guiState.titleMessage,
        width / 2,
        (height / 2 - (GuiRewardCard.CARD_HEIGHT / 2)) / 2 - 5,
        0);

    drawString(fontRendererObj,
        EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + "Reward ID: " + session
            .getId(),
        3,
        height - 10,
        0);
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    // Only select card on left-click
    if (mouseButton != 0) {
      return;
    }

    // Open reward link if ID is clicked
    if (mouseX > 0
        && mouseY < height
        && mouseX < fontRendererObj.getStringWidth("Reward ID:" + session.getId())
        && mouseY > height - 10
    ) {
      String rewardUrlStr = "https://rewards.hypixel.net/claim-reward/" + session.getId();

      // Hacky way to open a URL with the player's consent
      ChatComponentText urlOpener = new ChatComponentText("");
      urlOpener.setChatStyle(
          new ChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_URL, rewardUrlStr)));
      this.handleComponentClick(urlOpener);
    }

    // Check if card was clicked (claimed)
    for (int i = 0; i < cards.length; i++) {
      if (guiState != State.CHOOSING) {
        break;
      }

      if (cards[i].isHovered(mouseX, mouseY)) {
        guiState = State.FINAL;

        for (GuiRewardCard rewardCard : cards) {
          rewardCard.setFlipped(true);
          rewardCard.setEnabled(false);
        }

        // Re-enable the selected card & claim its reward
        cards[i].setEnabled(true);
        this.session.claimReward(i);
      }
    }

    super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button == closeButton) {
      this.mc.setIngameFocus();

    } else if (button == activationButton) {
      this.activationButton.enabled = false;
      this.guiState = State.CHOOSING;
      for (GuiRewardCard rewardCard : cards) {
        rewardCard.setEnabled(true);
      }
    }
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  /**
   * An enum of states that the RewardClaimGui can be in
   */
  enum State {
    INITIAL, // Cards aren't enabled
    CHOOSING, // Cards are enabled
    FINAL; // A card was selected

    @Getter
    private final String titleMessage;

    State() {
      this.titleMessage = I18n.format("message.gui.title." + name().toLowerCase());
    }
  }
}
