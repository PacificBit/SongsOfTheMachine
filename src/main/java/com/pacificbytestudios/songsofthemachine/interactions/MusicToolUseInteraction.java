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
import com.pacificbytestudios.songsofthemachine.utils.Utils;

public class MusicToolUseInteraction extends SimpleInteraction {
  private static final byte MAX_CAPACITY = 4;
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
          (obj, value) -> obj.actionCapacity = (byte) Math.min(value, MAX_CAPACITY),
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
  private AncientConstructStore store;

  public MusicToolUseInteraction() {
    this.store = AncientConstructStore.get();
  }

  @Override
  protected void tick0(
      boolean firstRun,
      float time,
      InteractionType type,
      InteractionContext context,
      CooldownHandler cooldownHandler) {
    System.out.println("[MusicToolInteraction] Music tool action: " + action.getId());
    Ref<EntityStore> playerRef = context.getEntity();
    World world = playerRef.getStore().getExternalData().getWorld();

    world.execute(() -> {
      ItemStack held = context.getHeldItem();
      MusicToolComponent comp = held.getFromMetadataOrDefault(MusicToolComponent.METADATA_KEY,
          MusicToolComponent.CODEC);

      if (comp != null && comp.getAction() != null) {
        this.action = comp.getAction();
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
        if (chunk == null)
          continue;

        Set<Ref<ChunkStore>> ancientConstructs = this.store.getAncientConstructInChunk(chunk.getReference());
        if (ancientConstructs == null || ancientConstructs.isEmpty()) {
          continue;
        }

        for (Ref<ChunkStore> construct : ancientConstructs) {
          BlockModule.BlockStateInfo info = construct.getStore().getComponent(
              construct,
              BlockModule.BlockStateInfo.getComponentType());
          if (info == null)
            continue;

          Vector3i constructPos = Utils.getBlockPosition(chunk, info.getIndex());

          if (constructPos.x < minX || constructPos.x > maxX
              || constructPos.z < minZ || constructPos.z > maxZ) {
            continue;
          }

          System.out.println("[MusicToolInteraction] Found AncientConstruct at: " + constructPos);

          AncientConstuctComponent component = construct.getStore().getComponent(construct,
              AncientConstuctComponent.getComponentType());

          if (component == null || !component.canBeInterrupted()) {
            System.out.println("[MusicToolInteraction] Cannot interact with component");
            System.out.println("Status " + component.getStatus() + " is looping " + component.getIsLooping());
            continue;
          }

          component.setStatus(AncientConstructStatus.LISTENING);
          component.setActionLoop(this.isLoopingInstrument);
          if (component.getActionCapacity() != this.actionCapacity) {
            component.clearActionBuffer();
            component.setActionCapacity(this.actionCapacity);
          }

          if (component.addAction(action)) {
            System.out.println("[MusicToolInteraction] Action added successfully");
            this.store.addAncient(construct);
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
