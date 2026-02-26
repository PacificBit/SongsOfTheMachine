package com.pacificbytestudios.songsofthemachine.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.pacificbytestudios.songsofthemachine.components.AncientConstuctComponent;
import com.pacificbytestudios.songsofthemachine.components.NeedsPathComponent;

public class OrthogonalAStar extends EntityTickingSystem<ChunkStore> {

  @Override
  public void tick(
      float deltaTime,
      int index,
      ArchetypeChunk<ChunkStore> archetypeChunk,
      Store<ChunkStore> chunkStore,
      CommandBuffer<ChunkStore> cb) {
    System.out.println("OrthogonalAStar !!!!!!!!!!!!!!!!!!!");
  }

  @Override
  public Query<ChunkStore> getQuery() {
    return Query.and(
        NeedsPathComponent.getComponentType(),
        AncientConstuctComponent.getComponentType());
  }
}
