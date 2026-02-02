package com.pacificbytestudios.songsofthemachine.events;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.pacificbytestudios.songsofthemachine.components.MusicToolComponent;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;
import com.pacificbytestudios.songsofthemachine.hui.ToolSelectionHUI;
import com.pacificbytestudios.songsofthemachine.storage.MusicToolHUIStorage;

public class PlayerReadyHandler {
  private static MusicToolHUIStorage store;

  static {
    store = MusicToolHUIStorage.get();
  }

  public static void handle(final PlayerReadyEvent event) {
    final Player player = event.getPlayer();

    if (player == null) {
      System.err.println("[PlayerReadyHandler] handle - Invalid player");
      return;
    }

    player.getWorld().execute(() -> {
      final ItemStack itemInHand = player.getInventory().getItemInHand();
      if (itemInHand == null || !itemInHand.getItemId().startsWith("Onyxium_Instrument_")) {
        System.out.println("[PlayerReadyHandler] handle - No item found in hand, leaving");
        return;
      }

      final MusicToolComponent musicTool = itemInHand.getFromMetadataOrDefault(
          MusicToolComponent.METADATA_KEY,
          MusicToolComponent.CODEC);

      ToolSelectionHUI hui = store.getMusicToolHui(musicTool.getUUID());

      if (hui == null) {
        hui = new ToolSelectionHUI(player.getPlayerRef());
        store.addMusicToolHui(musicTool.getUUID(), hui);
      }

      player.getHudManager().setCustomHud(player.getPlayerRef(), hui);
      final AncientConstructAction action = musicTool.getAction();
      hui.updateCurrentAction(action == null ? AncientConstructAction.IDLE : action);
    });
  }

}
