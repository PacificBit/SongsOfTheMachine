package com.pacificbytestudios.songsofthemachine.hui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class EmptyHUI extends CustomUIHud {

  public EmptyHUI(PlayerRef playerRef) {
    super(playerRef);
  }

  @Override
  protected void build(UICommandBuilder uiCommandBuilder) {
  }

}
