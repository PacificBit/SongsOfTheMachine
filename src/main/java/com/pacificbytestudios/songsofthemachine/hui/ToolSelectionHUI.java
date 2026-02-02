package com.pacificbytestudios.songsofthemachine.hui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import com.pacificbytestudios.songsofthemachine.components.MusicToolComponent;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;

public class ToolSelectionHUI extends CustomUIHud {

  public ToolSelectionHUI(PlayerRef playerRef) {
    super(playerRef);
  }

  @Override
  protected void build(UICommandBuilder uiCommandBuilder) {
    uiCommandBuilder.append("HUI/ToolSelection.ui");
  }

  public void updateCurrentAction(AncientConstructAction action) {
    UICommandBuilder commandBuilder = new UICommandBuilder();

    for (AncientConstructAction ancientConstructAction : AncientConstructAction.values()) {
      String id = AncientConstructAction.getUiIdFor(ancientConstructAction);
      String style = "Empty";

      if (action.getId() == ancientConstructAction.getId()) {
        style = "Container";
      }
      commandBuilder.set("#" + id + ".Background", Value.ref("HUI/ToolSelection.ui", style));
    }

    update(false, commandBuilder);
  }

  public void updateActionCount(int count, int capacity) {
    UICommandBuilder commandBuilder = new UICommandBuilder();

    System.out.println(
        "[ToolSelectionHUI] updateActionCount - Updating used action. Count: " + count + ", Capacity: " + capacity);
    for (int i = 1; i < MusicToolComponent.MAX_CAPACITY + 1; i++) {
      if (i > capacity) {
        commandBuilder.set("#Action" + i + ".Visible", false);
        continue;
      }
      String style = "NotActive";
      if (i <= count) {
        style = "Active";
      }
      commandBuilder.set("#Action" + i + ".Visible", true);
      commandBuilder.set("#Action" + i + ".Background", Value.ref("HUI/ToolSelection.ui", style));
    }

    update(false, commandBuilder);
  }
}
