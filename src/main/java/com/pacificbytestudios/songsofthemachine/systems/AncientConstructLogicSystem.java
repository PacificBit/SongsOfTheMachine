package com.pacificbytestudios.songsofthemachine.systems;

import java.util.Arrays;
import java.util.List;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.pacificbytestudios.songsofthemachine.components.AncientConstuctComponent;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;
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

    if (!entityRef.isValid()) {
      System.err.println("[AncientConstructLogicSystem] Invalid Ref");
      return;
    }

    if (!this.store.hasPendingCommands(entityRef)) {
      return;
    }

    AncientConstuctComponent construct = archetypeChunk.getComponent(
        index, AncientConstuctComponent.getComponentType());

    if (construct == null) {
      System.err.println("[AncientConstructLogicSystem] Construct is null");
      return;
    }

    construct.addTime(deltaTime);

    if (construct.getStatus() == AncientConstructStatus.READY_TO_EXECUTE) {
      construct.setStatus(AncientConstructStatus.EXECUTING);
      construct.resetTime();
      System.out.println("=======================================================");
      System.out.println("[AncientConstructLogicSystem] Instruction list: " + Arrays.toString(construct.getActions()));
      return;
    } else if (construct.getStatus() == AncientConstructStatus.EXECUTING) {
      AncientConstructAction action = construct.getNextAction();

      if (construct.getTime() >= action.getExecutionTime()) {
        System.out.println("[AncientConstructLogicSystem] Executing next instruction: " + action);
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
        System.out
            .println("[AncientConstructLogicSystem] Instruction list: " + Arrays.toString(construct.getActions()));
        System.out.println(construct.getStatus());
      }
      return;
    } else if (construct.getStatus() == AncientConstructStatus.COMPLETED) {
      System.out.println("[AncientConstructLogicSystem] Completed instruction set");
      construct.clearActionBuffer();
      this.store.removeAncient(entityRef);
      construct.setStatus(AncientConstructStatus.LISTENING);
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
      AncientConstructAction action) {
    if (!context.isValid()) {
      System.out.println("[AncientConstructLogicSystem] execute() - Invalid world context");
      return;
    }

    switch (action) {
      case MOVE_FORWARD -> moveForward(context, entityRef, blockPos);
      case TURN_LEFT -> turn(context, entityRef, blockPos, action);
      case TURN_RIGHT -> turn(context, entityRef, blockPos, action);
      case BREAK_BLOCK -> breakBlock(context, entityRef, blockPos);
      case IDLE -> {
      }
    }
  }

  private void moveForward(
      Utils.WorldContext context,
      Ref<ChunkStore> entityRef,
      Vector3i blockPos) {
    BlockType type = context.getChunk().getBlockType(blockPos);

    context.getWorld().execute(() -> {
      int rotation = context.getChunk().getRotationIndex(blockPos.x, blockPos.y, blockPos.z);

      int xModifier = 0;
      int zModifier = 0;

      if ((rotation & 1) == 0) {
        zModifier = (rotation == 2) ? -1 : 1;
      } else {
        xModifier = (rotation == 1) ? 1 : -1;
      }

      System.out.println("[AncientConstructLogicSystem] moveForward - Moving: " + blockPos);

      Vector3i newPos = new Vector3i(blockPos.x + xModifier, blockPos.y, blockPos.z + zModifier);
      WorldChunk chunk = context.getWorld().getChunk(ChunkUtil.indexChunkFromBlock(newPos.x, newPos.z));

      if (chunk == null) {
        System.out.println("[AncientConstructLogicSystem] moveForward - Invalid new chunk");
        return;
      }

      if (!chunk.getBlockType(newPos).getId().equals("Empty")
          || !chunk.getBlockType(newPos.clone().add(Vector3i.UP)).getId().equals("Empty")) {
        System.out.println("[AncientConstructLogicSystem] moveForward - Cannot move");
        return;
      }

      AncientConstuctComponent oldComponent = entityRef.getStore().getComponent(entityRef,
          AncientConstuctComponent.getComponentType());

      context.getChunk()
          .breakBlock(
              blockPos.x,
              blockPos.y,
              blockPos.z);

      chunk.setBlock(
          newPos.x,
          newPos.y,
          newPos.z,
          BlockType.getAssetMap().getIndex(type.getId()),
          type,
          rotation,
          0,
          0);
      Ref<ChunkStore> newEntityRef = chunk.getBlockComponentEntity(newPos.x, newPos.y, newPos.z);

      AncientConstuctComponent newComponent = newEntityRef.getStore().getComponent(newEntityRef,
          AncientConstuctComponent.getComponentType());
      newComponent.copyFrom(oldComponent);

      this.store.removeAncient(entityRef);
      this.store.addAncient(newEntityRef);
      if (chunk.getIndex() != context.getChunk().getIndex()) {
        System.out.println("[AncientConstructLogicSystem] Moving into new chunk");
        this.store.removeAncientConstructFromChunkId(context.getChunk().getReference(), entityRef);
        this.store.setAncientConstructChunkId(chunk.getReference(), newEntityRef);
      }
    });
  }

  private void turn(
      Utils.WorldContext context,
      Ref<ChunkStore> entityRef,
      Vector3i blockPos,
      AncientConstructAction action) {
    BlockType type = context.getChunk().getBlockType(blockPos);

    context.getWorld().execute(() -> {
      int rotation = context.getChunk().getRotationIndex(blockPos.x, blockPos.y, blockPos.z);

      rotation += (action == AncientConstructAction.TURN_LEFT) ? 1 : -1;
      rotation = (rotation + 4) % 4;

      AncientConstuctComponent oldComponent = entityRef.getStore().getComponent(entityRef,
          AncientConstuctComponent.getComponentType());

      context.getChunk().setBlock(
          blockPos.x,
          blockPos.y,
          blockPos.z,
          BlockType.getAssetMap().getIndex(type.getId()),
          type,
          rotation,
          0,
          0);

      Ref<ChunkStore> newEntityRef = context.getChunk().getBlockComponentEntity(
          blockPos.x,
          blockPos.y,
          blockPos.z);

      AncientConstuctComponent newComponent = newEntityRef.getStore().getComponent(newEntityRef,
          AncientConstuctComponent.getComponentType());
      newComponent.copyFrom(oldComponent);

      this.store.removeAncient(entityRef);
      this.store.addAncient(newEntityRef);
    });
  }

  private void breakBlock(
      Utils.WorldContext context,
      Ref<ChunkStore> entityRef,
      Vector3i blockPos) {

    context.getWorld().execute(() -> {
      int rotation = context.getChunk().getRotationIndex(blockPos.x, blockPos.y, blockPos.z);

      int xModifier = 0;
      int zModifier = 0;

      if ((rotation & 1) == 0) {
        zModifier = (rotation == 2) ? -1 : 1;
      } else {
        xModifier = (rotation == 1) ? 1 : -1;
      }
      Vector3i newPos = new Vector3i(blockPos.x + xModifier, blockPos.y, blockPos.z + zModifier);
      WorldChunk chunk = context.getWorld().getChunk(ChunkUtil.indexChunkFromBlock(newPos.x, newPos.z));

      BlockType blockType = chunk.getBlockType(newPos);

      String dropListId = blockType.getGathering().getBreaking().getDropListId();
      List<ItemStack> itemStacks = ItemModule.get().getRandomItemDrops(dropListId);

      AncientConstuctComponent component = entityRef.getStore().getComponent(entityRef,
          AncientConstuctComponent.getComponentType());

      ItemContainer storage = component.getItemContainer();
      itemStacks.forEach(storage::addItemStack);

      chunk.breakBlock(newPos.x, newPos.y, newPos.z);
    });
  }

  @Override
  public Query<ChunkStore> getQuery() {
    return Query.and(
        BlockModule.BlockStateInfo.getComponentType(),
        AncientConstuctComponent.getComponentType());
  }

}
