package com.pacificbytestudios.songsofthemachine.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.pacificbytestudios.songsofthemachine.components.AncientConstuctComponent;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructActions;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructStatus;
import com.pacificbytestudios.songsofthemachine.storage.AncientConstructStore;
import com.pacificbytestudios.songsofthemachine.utils.Utils;

public class AncientConstructLogicSystem extends EntityTickingSystem<ChunkStore> {
  private AncientConstructStore store;

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

    if (construct == null) {
      return;
    }

    construct.addTime(deltaTime);

    if (construct.getStatus() == AncientConstructStatus.READY_TO_EXECUTE) {
      construct.setStatus(AncientConstructStatus.EXECUTING);
      construct.resetTime();
      return;
    } else if (construct.getStatus() == AncientConstructStatus.EXECUTING) {
      AncientConstructActions action = construct.getNextAction();

      if (construct.getTime() >= action.getExecutionTime()) {
        BlockModule.BlockStateInfo info = chunkStore.getComponent(entityRef,
            BlockModule.BlockStateInfo.getComponentType());
        if (info != null) {
          Utils.WorldContext context = Utils.getWorldContextFromInfo(cb, info);
          Vector3i blockPos = Utils.getBlockPosition(context.getChunk(), info.getIndex());
          execute(context, entityRef, construct, blockPos, action);
        }
        construct.clearNextAction();
        construct.resetTime();
        System.out.println("[AncientConstructLogicSystem] Done exucuting: " + action);
      }
      return;
    } else if (construct.getStatus() == AncientConstructStatus.COMEPLETED) {
      construct.clearActionBuffer();
      this.store.removeAncient(entityRef);
      return;
    }

    System.out
        .println("[AncientConstructLogicSystem] Entity " + entityRef.getIndex() + ", clock: " + construct.getTime());

    if (construct.resetClockIfTimeout()) {
      this.store.removeAncient(entityRef);
      construct.clearActionBuffer();
      System.out.println("[AncientConstructLogicSystem] Entity command timeout");
    }
  }

  private void execute(
      Utils.WorldContext context,
      Ref<ChunkStore> entityRef,
      AncientConstuctComponent component,
      Vector3i blockPos,
      AncientConstructActions action) {

    if (action == AncientConstructActions.IDLE)
      return;

    if (!context.isValid()) {
      System.out.println("[AncientConstructLogicSystem] execute() - Invalid world context");
      return;
    }

    if (action == AncientConstructActions.MOVE_FORWARD) {
      moveForward(context, entityRef, blockPos);
    }
  }

  private void moveForward(
      Utils.WorldContext context,
      Ref<ChunkStore> entityRef,
      Vector3i blockPos) {
    BlockType type = context.getChunk().getBlockType(blockPos);

    context.getWorld().execute(() -> {
      int rotation = context.getChunk().getRotation(blockPos.x, blockPos.y, blockPos.z).index();

      int xModifier = 0;
      int zModifier = 0;

      if ((rotation & 1) == 0) {
        zModifier = (rotation == 2) ? -1 : 1;
      } else {
        xModifier = (rotation == 1) ? 1 : -1;
      }

      context.getChunk()
          .breakBlock(
              blockPos.x,
              blockPos.y,
              blockPos.z);

      System.out.println("[AncientConstructLogicSystem] moveForward - Moving: " + blockPos);

      Vector3i newPos = new Vector3i(blockPos.x + xModifier, blockPos.y, blockPos.z + zModifier);
      WorldChunk chunk = context.getWorld().getChunk(ChunkUtil.indexChunkFromBlock(newPos.x, newPos.z));

      if (chunk == null) {
        System.out.println("[AncientConstructLogicSystem] moveForward - Invalid new chunk");
        return;
      }

      chunk.setBlock(
          newPos.x,
          newPos.y,
          newPos.z,
          BlockType.getAssetMap().getIndex(type.getId()),
          type,
          rotation,
          0,
          0);

      if (chunk.getIndex() != context.getChunk().getIndex()) {
        System.out.println("[AncientConstructLogicSystem] Moving into new chunk");
        this.store.removeAncientConstructFromChunkId(context.getChunk().getReference(), entityRef);
        Ref<ChunkStore> newEntityRef = chunk.getBlockComponentEntity(newPos.x, newPos.y, newPos.z);
        this.store.setAncientConstructChunkId(chunk.getReference(), newEntityRef);
      }
    });

  }

  @Override
  public Query<ChunkStore> getQuery() {
    return Query.and(
        BlockModule.BlockStateInfo.getComponentType(),
        AncientConstuctComponent.getComponentType());
  }

}
