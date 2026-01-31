package com.pacificbytestudios.songsofthemachine.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.pacificbytestudios.songsofthemachine.components.AncientConstuctComponent;
import com.pacificbytestudios.songsofthemachine.storage.AncientConstructStore;

public class AncientConstructLogicSystem extends EntityTickingSystem<ChunkStore> {
  private AncientConstructStore store;

  record WorldContext(
      WorldChunk chunk,
      World world) {
    public boolean isValid() {
      return chunk != null && world != null;
    }
  }

  public AncientConstructLogicSystem() {
    store = AncientConstructStore.get();
  }

  @Override
  public void tick(
      float deltaTime,
      int index,
      ArchetypeChunk<ChunkStore> archetypeChunk,
      Store<ChunkStore> chunkStore,
      CommandBuffer<ChunkStore> cb) {
    Ref<ChunkStore> entityRef = archetypeChunk.getReferenceTo(index);

    if (!entityRef.isValid() || !this.store.hasPendingCommands(entityRef)) {
      return;
    }

    AncientConstuctComponent construct = archetypeChunk.getComponent(
        index, AncientConstuctComponent.getComponentType());

    BlockModule.BlockStateInfo info = archetypeChunk.getComponent(
        index, BlockModule.BlockStateInfo.getComponentType());

    if (construct == null || info == null) {
      return;
    }

    System.out
        .println("[AncientConstructLogicSystem] Entity " + entityRef.getIndex() + ", clock: " + construct.getClock());

    if (construct.addTimeAndCheckIfTimeout(deltaTime)) {
      this.store.removeAncient(entityRef);
      construct.clearActionBuffer();
      System.out.println("[AncientConstructLogicSystem] Entity command timeout");
    }
  }

  // private void move(WorldContext context, BlockModule.BlockStateInfo info) {
  // Vector3i currBlockPos = getBlockPosition(context.chunk, info.getIndex());
  //
  // if (currBlockPos.x > 20) {
  // return;
  // }
  //
  // BlockType type = context.chunk.getBlockType(currBlockPos);
  // context.world.execute(() -> {
  // int rotation = type.getRotationYawPlacementOffset().rotateY(1);
  // context.chunk.breakBlock(currBlockPos.x, currBlockPos.y, currBlockPos.z);
  //
  // context.chunk.setBlock(
  // currBlockPos.x + 1,
  // currBlockPos.y,
  // currBlockPos.z,
  // BlockType.getAssetMap().getIndex(type.getId()),
  // type,
  // rotation,
  // 0,
  // 0);
  // });
  // }

  // private Vector3i getBlockPosition(WorldChunk chunk, int chunkIndex) {
  // int x = ChunkUtil.xFromBlockInColumn(chunkIndex);
  // int y = ChunkUtil.yFromBlockInColumn(chunkIndex);
  // int z = ChunkUtil.zFromBlockInColumn(chunkIndex);
  // return new Vector3i(x + (chunk.getX() << 5), y, z + (chunk.getZ() << 5));
  // }

  @Override
  public Query<ChunkStore> getQuery() {
    return Query.and(
        BlockModule.BlockStateInfo.getComponentType(),
        AncientConstuctComponent.getComponentType());
  }
}
