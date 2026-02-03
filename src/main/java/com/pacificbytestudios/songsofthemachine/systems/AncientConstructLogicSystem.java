package com.pacificbytestudios.songsofthemachine.systems;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockBreakingDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.pacificbytestudios.songsofthemachine.components.AncientConstuctComponent;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructStatus;
import com.pacificbytestudios.songsofthemachine.hui.ToolSelectionHUI;
import com.pacificbytestudios.songsofthemachine.storage.AncientConstructStore;
import com.pacificbytestudios.songsofthemachine.storage.MusicToolHUIStorage;
import com.pacificbytestudios.songsofthemachine.utils.Utils;

public class AncientConstructLogicSystem extends EntityTickingSystem<ChunkStore> {
  private static final String FURNACE_ID = "Furnace";
  private static final String TANNERY_ID = "Tannery";
  private static final String EMPTY_BLOCK_ID = "Empty";

  private static final String ERROR_SOUND_ID = "SFX_SOTM_Construct_Error";
  private static final String MOVE_SOUND_ID = "SFX_SOTM_Construct_Move";
  private static final String TURN_LEFT_SOUND_ID = "SFX_SOTM_Turn_Left";
  private static final String TURN_RIGHT_SOUND_ID = "SFX_SOTM_Turn_Right";
  private static final String BREAK_SOUND_ID = "SFX_Stone_Break";
  private static final String OPEN_CONTAINER_SOUND_ID = "SFX_Chest_Wooden_Open";

  private static final int DIRECTION_BACK = -1;
  private static final int DIRECTION_FORWARD = 1;

  private static Map<String, short[]> OUTPUT_SLOTS = new HashMap<>();
  private AncientConstructStore ancientConstructStore;
  private MusicToolHUIStorage huiStorage;

  static {
    OUTPUT_SLOTS.put(FURNACE_ID + "1", new short[] { 3, 4, 5, 6 });
    OUTPUT_SLOTS.put(FURNACE_ID + "2", new short[] { 4, 5, 6, 7 });
    OUTPUT_SLOTS.put(TANNERY_ID + "1", new short[] { 2, 3 });
    OUTPUT_SLOTS.put(TANNERY_ID + "2", new short[] { 3, 4, 5, 6 });
  }

  public AncientConstructLogicSystem() {
    ancientConstructStore = AncientConstructStore.get();
    huiStorage = MusicToolHUIStorage.get();
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

    if (!this.ancientConstructStore.hasPendingCommands(entityRef)) {
      return;
    }

    AncientConstuctComponent construct = archetypeChunk.getComponent(
        index, AncientConstuctComponent.getComponentType());

    if (construct == null) {
      System.err.println("[AncientConstructLogicSystem] Construct is null");
      return;
    }

    construct.addTime(deltaTime);

    if (construct.getStatus() == AncientConstructStatus.ERROR) {
      construct.clearActionBuffer();
      construct.setStatus(AncientConstructStatus.LISTENING);
      this.ancientConstructStore.removeAncient(entityRef);

      BlockModule.BlockStateInfo info = chunkStore.getComponent(entityRef,
          BlockModule.BlockStateInfo.getComponentType());
      Utils.WorldContext context = Utils.getWorldContextFromInfo(cb, info);

      context.getWorld().execute(() -> {
        Vector3i blockPos = Utils.getBlockPosition(context.getChunk(), info.getIndex());
        int soundIndex = SoundEvent.getAssetMap().getIndex(ERROR_SOUND_ID);
        SoundUtil.playSoundEvent3d(soundIndex,
            SoundCategory.SFX,
            blockPos.toVector3d(),
            context.getWorld().getEntityStore().getStore());
      });
    }

    if (construct.getStatus() == AncientConstructStatus.COOLDOWN) {
      if (!construct.hasCooldownTerminated()) {
        return;
      }
      construct.setStatus(AncientConstructStatus.EXECUTING);
    }

    if (construct.getStatus() == AncientConstructStatus.READY_TO_EXECUTE) {
      construct.setStatus(AncientConstructStatus.EXECUTING);
      construct.resetTime();
      clearInstrumentHUI(construct.getListeningInstrumentId(), construct.getActionCapacity());
      System.out.println("=======================================================");
      System.out.println(
          "[AncientConstructLogicSystem] Instruction list: " + Arrays.toString(construct.getRemainingActions()));
      return;
    } else if (construct.getStatus() == AncientConstructStatus.EXECUTING) {
      AncientConstructAction action = construct.getNextAction();

      if (action == null) {
        construct.setStatus(AncientConstructStatus.COMPLETED);
        return;
      }

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
        construct.setStatus(AncientConstructStatus.COOLDOWN);
        construct.setCooldown(action.getCooldownTime());
        System.out.println("[AncientConstructLogicSystem] Done exucuting: " + action);
        System.out
            .println(
                "[AncientConstructLogicSystem] Instruction list: " + Arrays.toString(construct.getRemainingActions()));
        System.out
            .println(
                "[AncientConstructLogicSystem] Exiting with State: " + construct.getStatus());
      }
      return;
    } else if (construct.getStatus() == AncientConstructStatus.COMPLETED) {
      System.out.println("[AncientConstructLogicSystem] Completed instruction set");
      construct.clearActionBuffer();
      this.ancientConstructStore.removeAncient(entityRef);
      return;
    }

    if (construct.resetClockIfTimeout()) {
      this.ancientConstructStore.removeAncient(entityRef);
      construct.clearActionBuffer();
      System.out.println("[AncientConstructLogicSystem] Entity command timeout");
      clearInstrumentHUI(construct.getListeningInstrumentId(), construct.getActionCapacity());
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
      case MOVE_FORWARD -> move(context, entityRef, blockPos, DIRECTION_FORWARD);
      case MOVE_BACK -> move(context, entityRef, blockPos, DIRECTION_BACK);
      case TURN_LEFT -> turn(context, entityRef, blockPos, action);
      case TURN_BACK -> turn(context, entityRef, blockPos, action);
      case TURN_RIGHT -> turn(context, entityRef, blockPos, action);
      case BASIC_BREAK_BLOCK -> breakBlock(context, entityRef, blockPos, action);
      case COMPLEX_BREAK_BLOCK -> breakBlock(context, entityRef, blockPos, action);
      case DROP_IN_CONTAINER -> dropInContainer(context, entityRef, blockPos);
      case TAKE_OUTPUT_BENCH -> takeBenchOutput(context, entityRef, blockPos);
      case IDLE -> {
      }
    }
  }

  private void move(
      Utils.WorldContext context,
      Ref<ChunkStore> entityRef,
      Vector3i blockPos,
      int direction) {
    BlockType type = context.getChunk().getBlockType(blockPos);

    context.getWorld().execute(() -> {
      int rotation = context.getChunk().getRotationIndex(blockPos.x, blockPos.y, blockPos.z);

      int xModifier = 0;
      int zModifier = 0;

      if ((rotation & 1) == 0) {
        zModifier = ((rotation == 2) ? -1 : 1) * direction;
      } else {
        xModifier = ((rotation == 1) ? 1 : -1) * direction;
      }

      Vector3i newPos = new Vector3i(blockPos.x + xModifier, blockPos.y, blockPos.z + zModifier);
      WorldChunk chunk = context.getWorld().getChunk(ChunkUtil.indexChunkFromBlock(newPos.x, newPos.z));
      AncientConstuctComponent oldComponent = entityRef.getStore().getComponent(entityRef,
          AncientConstuctComponent.getComponentType());

      if (chunk == null) {
        System.out.println("[AncientConstructLogicSystem] moveForward - Invalid new chunk");
        return;
      }

      if (!chunk.getBlockType(newPos.clone().add(Vector3i.UP)).getId().equals(EMPTY_BLOCK_ID)) {
        System.out.println("[AncientConstructLogicSystem] moveForward - Cannot move");
        oldComponent.setStatus(AncientConstructStatus.ERROR);
        return;
      }

      if (!chunk.getBlockType(newPos).getId().equals(EMPTY_BLOCK_ID)) {
        newPos.add(Vector3i.UP);
      }

      Vector3i frontBottomBlock = newPos.clone().add(Vector3i.DOWN);
      if (chunk.getBlockType(frontBottomBlock).getId().equals(EMPTY_BLOCK_ID)) {
        if (!chunk.getBlockType(frontBottomBlock.add(Vector3i.DOWN)).getId().equals(EMPTY_BLOCK_ID)) {
          newPos = frontBottomBlock.add(Vector3i.UP);
        } else {
          System.out.println("[AncientConstructLogicSystem] moveForward - Cannot move");
          oldComponent.setStatus(AncientConstructStatus.ERROR);
          return;
        }
      }

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

      int index = SoundEvent.getAssetMap().getIndex(MOVE_SOUND_ID);
      SoundUtil.playSoundEvent3d(index,
          SoundCategory.SFX,
          newPos.toVector3d(),
          context.getWorld().getEntityStore().getStore());

      this.ancientConstructStore.removeAncient(entityRef);
      this.ancientConstructStore.addAncient(newEntityRef);
      if (chunk.getIndex() != context.getChunk().getIndex()) {
        System.out.println("[AncientConstructLogicSystem] Moving into new chunk");
        this.ancientConstructStore.removeAncientConstructFromChunkId(context.getChunk().getReference(), entityRef);
        this.ancientConstructStore.setAncientConstructChunkId(chunk.getReference(), newEntityRef);
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

      int modifier = (action == AncientConstructAction.TURN_LEFT) ? 1 : -1;
      rotation += (action == AncientConstructAction.TURN_BACK) ? modifier * 2 : modifier;
      rotation = (rotation + 4) % 4;
      String soundId = (action == AncientConstructAction.TURN_LEFT) ? TURN_LEFT_SOUND_ID : TURN_RIGHT_SOUND_ID;

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

      int index = SoundEvent.getAssetMap().getIndex(soundId);
      SoundUtil.playSoundEvent3d(index,
          SoundCategory.SFX,
          blockPos.toVector3d(),
          context.getWorld().getEntityStore().getStore());

      this.ancientConstructStore.removeAncient(entityRef);
      this.ancientConstructStore.addAncient(newEntityRef);
    });
  }

  private void breakBlock(
      Utils.WorldContext context,
      Ref<ChunkStore> entityRef,
      Vector3i blockPos,
      AncientConstructAction action) {
    int[] size = AncientConstructAction.getExcavationSizeFor(action);
    int width = size[0];
    int height = size[1];

    context.getWorld().execute(() -> {
      int rotation = context.getChunk().getRotationIndex(blockPos.x, blockPos.y, blockPos.z);

      int forwardX = 0, forwardZ = 0;
      if ((rotation & 1) == 0) {
        forwardZ = (rotation == 2) ? -1 : 1;
      } else {
        forwardX = (rotation == 1) ? 1 : -1;
      }

      int rigthX = forwardZ;
      int rightZ = -forwardX;

      AncientConstuctComponent component = entityRef.getStore().getComponent(entityRef,
          AncientConstuctComponent.getComponentType());
      ItemContainer storage = component.getItemContainer();

      int lateralStart = -((width - 1) / 2);
      int lateralEnd = (width / 2);

      int lateralCount = (lateralEnd - lateralStart + 1);
      Vector3d[] brokenBlocks = new Vector3d[height * lateralCount];
      int brokenBlockIndex = 0;

      for (int dy = 0; dy < height; dy++) {
        for (int lateral = lateralStart; lateral <= lateralEnd; lateral++) {
          int x = blockPos.x + forwardX + (rigthX * lateral);
          int y = blockPos.y + dy;
          int z = blockPos.z + forwardZ + (rightZ * lateral);

          WorldChunk chunk = context.getWorld().getChunk(ChunkUtil.indexChunkFromBlock(x, z));
          if (chunk == null) {
            System.err.println("[AncientConstructLogicSystem] breakBlock - Could not find chunk");
            continue;
          }

          Vector3i newPos = new Vector3i(x, y, z);
          BlockType blockType = chunk.getBlockType(newPos);
          BlockGathering gathering = blockType.getGathering();
          if (gathering == null) {
            System.err.println(
                "[AncientConstructLogicSystem] breakBlock - Could not get gathering data for block at: " + newPos);
            continue;
          }

          BlockBreakingDropType breaking = gathering.getBreaking();
          if (breaking == null) {
            System.err.println(
                "[AncientConstructLogicSystem] breakBlock - Could not get breaking data for block at: " + newPos);
            continue;
          }

          int quantity = breaking.getQuantity();
          String itemId = breaking.getItemId();
          String dropListId = breaking.getDropListId();

          List<ItemStack> drops = BlockHarvestUtils.getDrops(blockType, quantity, itemId, dropListId);
          drops.forEach(storage::addItemStack);

          chunk.breakBlock(x, y, z);
          brokenBlocks[brokenBlockIndex++] = new Vector3d(x, y, z);
        }
      }

      // I wanted more Ummf~ when breaking more blocks. A greater sound
      int soundIndex = SoundEvent.getAssetMap().getIndex(BREAK_SOUND_ID);
      for (Vector3d brokenBlock : brokenBlocks) {
        if (brokenBlock == null) {
          continue;
        }
        SoundUtil.playSoundEvent3d(soundIndex,
            SoundCategory.SFX,
            brokenBlock,
            context.getWorld().getEntityStore().getStore());
      }
    });
  }

  public void dropInContainer(
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

      AncientConstuctComponent component = entityRef.getStore().getComponent(entityRef,
          AncientConstuctComponent.getComponentType());
      ItemContainer constructStorage = component.getItemContainer();

      if (chunk == null) {
        System.err.println("[AncientConstructLogicSystem] dropInContainer - Could not find chunk");
        return;
      }

      BlockType blockType = chunk.getBlockType(newPos);
      Bench benchBlock = blockType.getBench();

      if (benchBlock == null) {
        AssetExtraInfo.Data blockData = blockType.getData();

        ItemContainerState containerState = (ItemContainerState) context.getWorld().getState(
            newPos.x, newPos.y, newPos.z, true);

        if (blockData == null || blockData.getContainerData() == null || containerState == null) {
          System.out.println("[AncientConstructLogicSystem] dropInContainer - No valid container found");
          component.setStatus(AncientConstructStatus.ERROR);
          return;
        }
        constructStorage.moveAllItemStacksTo(containerState.getItemContainer());

      } else if (benchBlock.getType().equals(BenchType.Processing)) {
        ProcessingBenchState benchState = (ProcessingBenchState) context.getWorld().getState(
            newPos.x, newPos.y, newPos.z, true);
        constructStorage.moveAllItemStacksTo(benchState.getItemContainer());
        benchState.setActive(true);

      } else {
        System.out.println("[AncientConstructLogicSystem] dropInContainer - Bench type not supported");
        component.setStatus(AncientConstructStatus.ERROR);
        return;
      }

      int soundIndex = SoundEvent.getAssetMap().getIndex(OPEN_CONTAINER_SOUND_ID);
      SoundUtil.playSoundEvent3d(soundIndex,
          SoundCategory.SFX,
          newPos.toVector3d(),
          context.getWorld().getEntityStore().getStore());
    });

  }

  private void takeBenchOutput(
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

      AncientConstuctComponent component = entityRef.getStore().getComponent(entityRef,
          AncientConstuctComponent.getComponentType());
      ItemContainer constructStorage = component.getItemContainer();

      if (chunk == null) {
        System.err.println("[AncientConstructLogicSystem] takeBenchOutput - Could not find chunk");
        return;
      }

      Ref<ChunkStore> newPosBlockRef = chunk.getBlockComponentEntity(newPos.x, newPos.y, newPos.z);
      if (newPosBlockRef == null) {
        System.err
            .println(
                "[AncientConstructLogicSystem] takeBenchOutput - Either no block in front or it has an invalid ref");
        component.setStatus(AncientConstructStatus.ERROR);
        return;
      }

      BlockType blockType = chunk.getBlockType(newPos);
      Bench blockTypeBench = blockType.getBench();
      BlockState state = context.getWorld().getState(newPos.x, newPos.y, newPos.z, true);

      if ((state instanceof ProcessingBenchState benchState) &&
          blockTypeBench != null &&
          blockTypeBench.equals(benchState.getBench()) &&
          benchState.initialize(blockType)) {
        System.out.println("[AncientConstructLogicSystem] takeBenchOutput - Found working bench");

        if (blockTypeBench.getType() != BenchType.Processing) {
          System.err.println("[AncientConstructLogicSystem] takeBenchOutput - Is not a processing bench");
          component.setStatus(AncientConstructStatus.ERROR);
          return;
        }

        String benchId = blockTypeBench.getId() + benchState.getTierLevel();
        short[] outputSlots = OUTPUT_SLOTS.getOrDefault(benchId, null);
        if (outputSlots == null) {
          System.err.println(
              "[AncientConstructLogicSystem] takeBenchOutput - Could not find output slot from output slot map, for id "
                  + benchId);
          component.setStatus(AncientConstructStatus.ERROR);
          return;
        }

        for (short slot : outputSlots) {
          benchState.getItemContainer().moveItemStackFromSlot(slot, constructStorage);
        }
        int soundIndex = SoundEvent.getAssetMap().getIndex(OPEN_CONTAINER_SOUND_ID);
        SoundUtil.playSoundEvent3d(soundIndex,
            SoundCategory.SFX,
            newPos.toVector3d(),
            context.getWorld().getEntityStore().getStore());
      }
    });
  }

  private void clearInstrumentHUI(UUID musicToolId, int capacity) {
    ToolSelectionHUI hui = this.huiStorage.getMusicToolHui(musicToolId);
    if (hui == null) {
      System.err.println("No linked HUI to that instrument");
      return;
    }
    hui.updateActionCount(0, capacity);
  }

  @Override
  public Query<ChunkStore> getQuery() {
    return Query.and(
        BlockModule.BlockStateInfo.getComponentType(),
        AncientConstuctComponent.getComponentType());
  }

}
