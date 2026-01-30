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
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructActions;
import com.pacificbytestudios.songsofthemachine.storage.AncientConstructStore;

public class AncientConstructPlacementSystem extends RefSystem<ChunkStore> {

  @Override
  public void onEntityAdded(
      Ref<ChunkStore> ref,
      AddReason reason,
      Store<ChunkStore> store,
      CommandBuffer<ChunkStore> commandBuffer) {
    AncientConstructStore.get().addAncient(ref);
    AncientConstuctComponent component = store.getComponent(ref, AncientConstuctComponent.getComponentType());
    component.addAction(AncientConstructActions.MOVE_FORWARD);
    component.addAction(AncientConstructActions.TURN_LEFT);
    component.addAction(AncientConstructActions.TURN_RIGHT);
    component.addAction(AncientConstructActions.BREAK_BLOCK);
    System.out.println("[AncientConstructPlacementSystem] Added new entity");
  }

  @Override
  public void onEntityRemove(
      Ref<ChunkStore> ref,
      RemoveReason reason,
      Store<ChunkStore> store,
      CommandBuffer<ChunkStore> commandBuffer) {
    AncientConstructStore.get().removeAncient(ref);
    System.out.println("[AncientConstructPlacementSystem] Removed entity");
  }

  @Override
  public Query<ChunkStore> getQuery() {
    return Query.and(
        BlockModule.BlockStateInfo.getComponentType(),
        AncientConstuctComponent.getComponentType());
  }
}
