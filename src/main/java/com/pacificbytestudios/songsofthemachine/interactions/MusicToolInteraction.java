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
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.pacificbytestudios.songsofthemachine.components.AncientConstuctComponent;
import com.pacificbytestudios.songsofthemachine.customcodec.ActionSelection;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructActions;
import com.pacificbytestudios.songsofthemachine.enums.MusicToolQuality;
import com.pacificbytestudios.songsofthemachine.storage.AncientConstructStore;
import com.pacificbytestudios.songsofthemachine.utils.Utils;

public class MusicToolInteraction extends SimpleInteraction {

  public static final BuilderCodec<MusicToolInteraction> CODEC = BuilderCodec
      .builder(MusicToolInteraction.class, MusicToolInteraction::new, SimpleInteraction.CODEC)
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
      .build();

  private AncientConstructActions action;
  private byte quality;
  private AncientConstructStore store;

  public MusicToolInteraction() {
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

    TransformComponent tc = playerRef.getStore()
        .getComponent(playerRef, TransformComponent.getComponentType());
    if (tc == null)
      return;

    Vector3i playerPos = tc.getPosition().toVector3i();
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

    Set<Long> chunkIndices = new HashSet<>(4);
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

        AncientConstuctComponent component = construct.getStore().getComponent(
            construct,
            AncientConstuctComponent.getComponentType());
        if (component == null)
          continue;

        if (component.addAction(action)) {
          System.out.println("[MusicToolInteraction] Action added successfully");
          this.store.addAncient(construct);
        } else {
          System.err.println("[MusicToolInteraction] Could not add action");
        }
      }
    }
  }

  @Override
  protected MusicToolInteraction clone() {
    MusicToolInteraction tool = new MusicToolInteraction();
    tool.action = this.action;
    tool.quality = this.quality;
    return tool;
  }

}
