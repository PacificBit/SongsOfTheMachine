package com.pacificbytestudios.songsofthemachine.interactions;

import java.util.HashSet;
import java.util.Set;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
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
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructStatus;
import com.pacificbytestudios.songsofthemachine.enums.MusicToolQuality;
import com.pacificbytestudios.songsofthemachine.storage.AncientConstructStore;
import com.pacificbytestudios.songsofthemachine.utils.Utils;

public class CallToActionInteraction extends SimpleInteraction {
  public static BuilderCodec<CallToActionInteraction> CODEC = BuilderCodec
      .builder(CallToActionInteraction.class, CallToActionInteraction::new, SimpleInteraction.CODEC)
      .build();
  private AncientConstructStore constructStore;

  public CallToActionInteraction() {
    this.constructStore = AncientConstructStore.get();
  }

  @Override
  protected void tick0(
      boolean firstRun,
      float time,
      InteractionType type,
      InteractionContext context,
      CooldownHandler cooldownHandler) {
    Ref<EntityStore> playerRef = context.getEntity();
    if (playerRef == null || !playerRef.isValid()) {
      System.err.println("[CallToActionInteraction] Invalid player ref");
      return;
    }

    Store<EntityStore> store = playerRef.getStore();
    MovementStatesComponent movStateComp = store.getComponent(playerRef, MovementStatesComponent.getComponentType());

    if (!movStateComp.getMovementStates().crouching) {
      return;
    }

    context.getInteractionManager().cancelChains(context.getChain());

    World world = playerRef.getStore().getExternalData().getWorld();

    world.execute(() -> {
      ItemStack held = context.getHeldItem();
      MusicToolComponent musicTool = held.getFromMetadataOrDefault(
          MusicToolComponent.METADATA_KEY,
          MusicToolComponent.CODEC);

      TransformComponent transformComponent = playerRef.getStore()
          .getComponent(playerRef, TransformComponent.getComponentType());
      if (transformComponent == null)
        return;

      Vector3i playerPos = transformComponent.getPosition().toVector3i();
      byte range = MusicToolQuality.AVG.getRange();

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
          System.err.println("[CallToActionInteraction] Could not fetch chunk");
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
            System.err.println("[CallToActionInteraction] No block info found for construct: (world=" + world.getName()
                + ", chunk=(" + chunk.getX() + ", " + chunk.getZ() + "))");
            continue;
          }

          Vector3i constructPos = Utils.getBlockPosition(chunk, info.getIndex());

          if (constructPos.x < minX || constructPos.x > maxX
              || constructPos.z < minZ || constructPos.z > maxZ) {
            continue;
          }

          System.out.println("[CallToActionInteraction] Found AncientConstruct at: " + constructPos);

          AncientConstuctComponent ancientConstruct = construct.getStore().getComponent(construct,
              AncientConstuctComponent.getComponentType());

          if (ancientConstruct == null || !ancientConstruct.canBeInterrupted()) {
            System.out.println("[CallToActionInteraction] Cannot interact with component");
            System.out
                .println("Status " + ancientConstruct.getStatus() + " is looping " + ancientConstruct.getIsLooping());
            continue;
          }

          if (ancientConstruct.getStatus() != AncientConstructStatus.LISTENING) {
            ancientConstruct.clearActionBuffer();
          }

          ancientConstruct.addAction(
              AncientConstructAction.EARLY_EXIT,
              musicTool.getUUID());
        }
      }
    });
  }

}
