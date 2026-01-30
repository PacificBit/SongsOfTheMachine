package com.pacificbytestudios.songsofthemachine.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
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
      CommandBuffer<ChunkStore> commandBuffer) {

    for (Ref<ChunkStore> entityRef : this.store.getAncientConstuctComponents()) {
      System.out.println("[AncientConstructLogicSystem] Found entity");

      if (!entityRef.isValid()) {
        continue;
      }

      BlockModule.BlockStateInfo info = commandBuffer.getComponent(entityRef,
          BlockModule.BlockStateInfo.getComponentType());
      WorldContext context = getWorldContext(commandBuffer, info.getChunkRef());

      if (!context.isValid()) {
        continue;
      }

      Vector3i currBlockPos = getBlockPosition(context.chunk, info.getIndex());
      if (currBlockPos.x > 20) {
        continue;
      }

      BlockType type = context.chunk.getBlockType(currBlockPos);
      context.world.execute(() -> {

        // if (currBlockPos.x > 20) {
        // context.world.setBlock(currBlockPos.x, currBlockPos.y, currBlockPos.z,
        // "Empty");
        // return;
        // }

        int rotation = type.getRotationYawPlacementOffset().rotateY(1);
        context.chunk.breakBlock(currBlockPos.x, currBlockPos.y, currBlockPos.z);

        context.chunk.setBlock(
            currBlockPos.x + 1,
            currBlockPos.y,
            currBlockPos.z,
            BlockType.getAssetMap().getIndex(type.getId()),
            type,
            rotation, 0, 0);
      });

      AncientConstuctComponent component = chunkStore.getComponent(entityRef,
          AncientConstuctComponent.getComponentType());

      if (component == null) {
        return;
      }

      System.out.println(component.getActions());
    }
  }

  private WorldContext getWorldContext(CommandBuffer<ChunkStore> cb, Ref<ChunkStore> ref) {
    WorldChunk worldChunk = cb.getComponent(ref, WorldChunk.getComponentType());
    if (worldChunk == null) {
      return new WorldContext(null, null);
    }
    World world = worldChunk.getWorld();
    return new WorldContext(worldChunk, world);
  }

  private Vector3i getBlockPosition(WorldChunk chunk, int chunkIndex) {
    int x = ChunkUtil.xFromBlockInColumn(chunkIndex);
    int y = ChunkUtil.yFromBlockInColumn(chunkIndex);
    int z = ChunkUtil.zFromBlockInColumn(chunkIndex);
    return new Vector3i(x + (chunk.getX() << 5), y, z + (chunk.getZ() << 5));
  }

  @Override
  public Query<ChunkStore> getQuery() {
    return Query.and(
        BlockModule.BlockStateInfo.getComponentType(),
        AncientConstuctComponent.getComponentType());
  }
}
