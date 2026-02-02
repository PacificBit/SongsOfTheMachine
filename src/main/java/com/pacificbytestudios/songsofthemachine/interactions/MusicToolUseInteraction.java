package com.pacificbytestudios.songsofthemachine.interactions;

import java.util.HashSet;
import java.util.Set;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.pacificbytestudios.songsofthemachine.components.AncientConstuctComponent;
import com.pacificbytestudios.songsofthemachine.components.MusicToolComponent;
import com.pacificbytestudios.songsofthemachine.customcodec.ActionSelection;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructStatus;
import com.pacificbytestudios.songsofthemachine.enums.MusicToolQuality;
import com.pacificbytestudios.songsofthemachine.storage.AncientConstructStore;
import com.pacificbytestudios.songsofthemachine.storage.MusicToolHUIStorage;
import com.pacificbytestudios.songsofthemachine.utils.Utils;

public class MusicToolUseInteraction extends SimpleInteraction {
  public static final BuilderCodec<MusicToolUseInteraction> CODEC = BuilderCodec
      .builder(MusicToolUseInteraction.class, MusicToolUseInteraction::new, SimpleInteraction.CODEC)
      .append(
          new KeyedCodec<>("Action", ActionSelection.CODEC),
          (obj, value) -> obj.action = value.getAction(),
          obj -> null)
      .add()
      .append(
          new KeyedCodec<>("Quality", Codec.BYTE),
          (obj, value) -> obj.quality = value,
          obj -> obj.quality)
      .add()
      .append(
          new KeyedCodec<>("ActionsCapacity", Codec.BYTE),
          (obj, value) -> obj.actionCapacity = (byte) Math.min(value, MusicToolComponent.MAX_CAPACITY),
          obj -> obj.actionCapacity == null ? 1 : obj.actionCapacity)
      .add()
      .append(new KeyedCodec<>("IsLoopingInstrument", Codec.BOOLEAN),
          (obj, value) -> obj.isLoopingInstrument = value,
          obj -> obj.isLoopingInstrument)
      .add()
      .build();

  private AncientConstructAction action;
  private byte quality;
  private Byte actionCapacity;
  private boolean isLoopingInstrument;
  private AncientConstructStore constructStore;
  private MusicToolHUIStorage huiStorage;

  public MusicToolUseInteraction() {
    this.constructStore = AncientConstructStore.get();
    this.huiStorage = MusicToolHUIStorage.get();
  }

  @Override
  protected void tick0(
      boolean firstRun,
      float time,
      InteractionType type,
      InteractionContext context,
      CooldownHandler cooldownHandler) {
    System.out.println(
        "[MusicToolInteraction] Music tool action: " + action.getId());
    Ref<EntityStore> playerRef = context.getEntity();
    World world = playerRef.getStore().getExternalData().getWorld();

    world.execute(() -> {
      ItemStack held = context.getHeldItem();
      MusicToolComponent musicTool = held.getFromMetadataOrDefault(
          MusicToolComponent.METADATA_KEY,
          MusicToolComponent.CODEC);

      if (musicTool != null && musicTool.getAction() != null) {
        this.action = musicTool.getAction();
      } else {
        System.err.println("[MusicToolUseInteraction] Could not fetch action");
        this.action = AncientConstructAction.IDLE;
      }

      System.out.println("[MusicToolUseInteraction] Using current tool to apply the action: " + this.action);

      TransformComponent transformComponent = playerRef.getStore()
          .getComponent(playerRef, TransformComponent.getComponentType());
      if (transformComponent == null)
        return;

      Vector3i playerPos = transformComponent.getPosition().toVector3i();
      MusicToolQuality tollQuality = MusicToolQuality.fromId(quality);
      if (tollQuality == null) {
        return;
      }
      byte range = tollQuality.getRange();

      final int px = playerPos.getX();
      final int pz = playerPos.getZ();

      final int half = range / 2;
      final int minX = px - half;
      final int maxX = px + half;
      final int minZ = pz - half;
      final int maxZ = pz + half;

      Set<Long> chunkIndices = new HashSet<>(
          4);
      chunkIndices.add(ChunkUtil.indexChunkFromBlock(minX, minZ));
      chunkIndices.add(ChunkUtil.indexChunkFromBlock(maxX, minZ));
      chunkIndices.add(ChunkUtil.indexChunkFromBlock(minX, maxZ));
      chunkIndices.add(ChunkUtil.indexChunkFromBlock(maxX, maxZ));

      for (long chunkIndex : chunkIndices) {
        WorldChunk chunk = world.getChunk(chunkIndex);
        if (chunk == null) {
          System.err.println("[MusicToolInteraction] Could not fetch chunk");
          continue;
        }

        Set<Ref<ChunkStore>> ancientConstructs = this.constructStore.getAncientConstructInChunk(chunk.getReference());
        if (ancientConstructs == null || ancientConstructs.isEmpty()) {
          continue;
        }

        for (Ref<ChunkStore> construct : ancientConstructs) {
          BlockModule.BlockStateInfo info = construct.getStore().getComponent(
              construct,
              BlockModule.BlockStateInfo.getComponentType());
          if (info == null) {
            System.err.println("[MusicToolInteraction] No block info found for construct: (world=" + world.getName()
                + ", chunk=(" + chunk.getX() + ", " + chunk.getZ() + "))");
            continue;
          }

          Vector3i constructPos = Utils.getBlockPosition(chunk, info.getIndex());

          if (constructPos.x < minX || constructPos.x > maxX
              || constructPos.z < minZ || constructPos.z > maxZ) {
            continue;
          }

          System.out.println("[MusicToolInteraction] Found AncientConstruct at: " + constructPos);

          AncientConstuctComponent ancientConstruct = construct.getStore().getComponent(construct,
              AncientConstuctComponent.getComponentType());

          if (ancientConstruct == null || !ancientConstruct.canBeInterrupted()) {
            System.out.println("[MusicToolInteraction] Cannot interact with component");
            System.out
                .println("Status " + ancientConstruct.getStatus() + " is looping " + ancientConstruct.getIsLooping());
            continue;
          }

          if (ancientConstruct.getStatus() != AncientConstructStatus.LISTENING) {
            ancientConstruct.clearActionBuffer();
          }

          if (ancientConstruct.getActionCapacity() != this.actionCapacity) {
            ancientConstruct.clearActionBuffer();
            ancientConstruct.setActionCapacity(this.actionCapacity);
          }

          ancientConstruct.setStatus(AncientConstructStatus.LISTENING);
          ancientConstruct.setActionLoop(this.isLoopingInstrument);

          if (ancientConstruct.addAction(action, musicTool.getUUID())) {
            System.out.println("[MusicToolInteraction] Action added successfully");
            this.constructStore.addAncient(construct);
            this.huiStorage.getMusicToolHui(musicTool.getUUID()).updateActionCount(
                ancientConstruct.getBufferLoad(),
                ancientConstruct.getActionCapacity());
          } else {
            System.err.println("[MusicToolInteraction] Could not add action");
          }
        }
      }
    });
  }

  @Override
  protected MusicToolUseInteraction clone() {
    MusicToolUseInteraction tool = new MusicToolUseInteraction();
    tool.action = this.action;
    tool.quality = this.quality;
    tool.actionCapacity = this.actionCapacity;
    tool.isLoopingInstrument = this.isLoopingInstrument;
    return tool;
  }

}
