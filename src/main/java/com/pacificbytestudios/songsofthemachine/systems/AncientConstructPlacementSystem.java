package com.pacificbytestudios.songsofthemachine.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import com.pacificbytestudios.songsofthemachine.components.AncientConstuctComponent;
import com.pacificbytestudios.songsofthemachine.storage.AncientConstructStore;

public class AncientConstructPlacementSystem extends RefSystem<ChunkStore> {
  private AncientConstructStore store;

  public AncientConstructPlacementSystem() {
    store = AncientConstructStore.get();
  }

  @Override
  public void onEntityAdded(
      Ref<ChunkStore> ref,
      AddReason reason,
      Store<ChunkStore> store,
      CommandBuffer<ChunkStore> commandBuffer) {
    BlockModule.BlockStateInfo info = store.getComponent(ref,
        BlockModule.BlockStateInfo.getComponentType());
    Ref<ChunkStore> chunkRef = info.getChunkRef();
    if (!chunkRef.isValid()) {
      System.err.println("[AncientConstructPlacementSystem] Could not find chunk during entity creation");
      return;
    }

    this.store.setAncientConstructChunkId(chunkRef, ref);
    System.out.println("[AncientConstructPlacementSystem] Added new entity. Reason: " + reason);

    if (reason == AddReason.LOAD) {
      this.store.addAncient(ref);
      this.store.hasPendingCommands(ref);
    }
  }

  @Override
  public void onEntityRemove(
      Ref<ChunkStore> ref,
      RemoveReason reason,
      Store<ChunkStore> store,
      CommandBuffer<ChunkStore> commandBuffer) {
    BlockModule.BlockStateInfo info = store.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
    Ref<ChunkStore> chunkRef = info.getChunkRef();
    if (!chunkRef.isValid()) {
      System.err.println("[AncientConstructPlacementSystem] Could not find chunk during entity removal");
      return;
    }

    this.store.removeAncient(ref);
    this.store.removeAncientConstructFromChunkId(chunkRef, ref);

    System.out.println("[AncientConstructPlacementSystem] Removed entity. Reason: " + reason);
  }

  @Override
  public Query<ChunkStore> getQuery() {
    return Query.and(
        BlockModule.BlockStateInfo.getComponentType(),
        AncientConstuctComponent.getComponentType());
  }
}
