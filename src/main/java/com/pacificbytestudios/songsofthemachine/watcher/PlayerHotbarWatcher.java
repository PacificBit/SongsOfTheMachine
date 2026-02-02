package com.pacificbytestudios.songsofthemachine.watcher;

import java.util.UUID;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.pacificbytestudios.songsofthemachine.components.MusicToolComponent;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;
import com.pacificbytestudios.songsofthemachine.hui.EmptyHUI;
import com.pacificbytestudios.songsofthemachine.hui.ToolSelectionHUI;
import com.pacificbytestudios.songsofthemachine.storage.MusicToolHUIStorage;

public class PlayerHotbarWatcher implements PlayerPacketWatcher {
  private MusicToolHUIStorage store;

  public PlayerHotbarWatcher() {
    this.store = MusicToolHUIStorage.get();
  }

  @Override
  public void accept(PlayerRef playerRef, Packet packet) {
    if (packet instanceof SyncInteractionChains syncChain) {
      for (SyncInteractionChain chain : syncChain.updates) {
        if (chain.interactionType == InteractionType.SwapFrom && chain.data != null) {
          this.onHotbarSlotSwitch(chain, playerRef);
        }
      }
    }
  }

  private void onHotbarSlotSwitch(SyncInteractionChain chain, PlayerRef playerRef) {
    short from = (short) chain.activeHotbarSlot;
    short to = (short) chain.data.targetSlot;

    Universe.get().getWorld(playerRef.getWorldUuid()).execute(() -> {
      Ref<EntityStore> entityRef = playerRef.getReference();
      if (!entityRef.isValid()) {
        System.err.println("[PlayerHotbarWatcher] onHotbarSlotSwitch - Invalid player ref");
        return;
      }

      Player player = entityRef.getStore().getComponent(entityRef, Player.getComponentType());
      if (player == null) {
        System.err.println("[PlayerHotbarWatcher] onHotbarSlotSwitch - Could not fetch the player");
        return;
      }

      ItemStack originItem = player.getInventory().getHotbar().getItemStack(from);
      // Math.abs because sometimes i received a correct but negative 'to' and it
      // raised IllegalArgumentException,
      ItemStack targetItem = player.getInventory().getHotbar().getItemStack((short) Math.abs(to));

      if (targetItem == null || !targetItem.getItemId().startsWith("Onyxium_Instrument_")) {
        if (originItem != null && originItem.getItemId().startsWith("Onyxium_Instrument_")) {
          player.getHudManager().setCustomHud(playerRef, new EmptyHUI(playerRef));
        }
        return;
      }

      MusicToolComponent musicTool = targetItem.getFromMetadataOrDefault(
          MusicToolComponent.METADATA_KEY,
          MusicToolComponent.CODEC);

      if (musicTool.getUUID() == null) {
        UUID newUUID = UUID.randomUUID();
        musicTool.setUUID(newUUID);

        ItemStack updated = targetItem.withMetadata(
            MusicToolComponent.METADATA_KEY,
            MusicToolComponent.CODEC,
            musicTool);

        ItemStackSlotTransaction transaction = player.getInventory().getCombinedHotbarFirst()
            .setItemStackForSlot(to, updated);

        if (!transaction.succeeded()) {
          System.err.println("[PlayerHotbarWatcher] Failed to assign UUID to slot " + to);
          return;
        } else {
          System.out.println("[PlayerHotbarWatcher] Assigned new UUID to slot " + to + ": " + newUUID);
        }

        targetItem = updated;
      }

      System.out.println(
          "[PlayerHotbarWatcher] onHotbarSlotSwitch - Slot '" + to + "', music tool uuid: " + musicTool.getUUID());

      ToolSelectionHUI hui = this.store.getMusicToolHui(musicTool.getUUID());
      if (hui == null) {
        System.out.println("[PlayerHotbarWatcher] onHotbarSlotSwitch - Created new Tool selection hui");
        hui = new ToolSelectionHUI(playerRef);
        this.store.addMusicToolHui(musicTool.getUUID(), hui);
      }

      player.getHudManager().setCustomHud(playerRef, hui);
      AncientConstructAction action = musicTool.getAction();
      hui.updateCurrentAction(action == null ? AncientConstructAction.IDLE : action);
    });
  }

}
