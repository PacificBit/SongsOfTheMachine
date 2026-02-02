package com.pacificbytestudios.songsofthemachine.hui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
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
}
