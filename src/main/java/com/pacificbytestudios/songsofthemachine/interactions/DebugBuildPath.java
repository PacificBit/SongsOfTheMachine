package com.pacificbytestudios.songsofthemachine.interactions;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.pacificbytestudios.songsofthemachine.components.AncientConstuctComponent;
import com.pacificbytestudios.songsofthemachine.components.NeedsPathComponent;

public class DebugBuildPath extends SimpleInteraction {
  public static final BuilderCodec<DebugBuildPath> CODEC = BuilderCodec
      .builder(DebugBuildPath.class, DebugBuildPath::new)

      .append(new KeyedCodec<>("Start", Vector3i.CODEC),
          (obj, val) -> obj.start = val,
          (obj) -> obj.start)
      .add()

      .build();

  private Vector3i start;
  private Ref<ChunkStore> ancientConstruct;

  @Override
  protected void tick0(
      boolean firstRun,
      float time,
      InteractionType type,
      InteractionContext context,
      CooldownHandler cooldownHandler) {
    BlockPosition targetPos = context.getTargetBlock();

    Vector3i newPos = new Vector3i(targetPos.x, targetPos.y, targetPos.z);
    World world = context.getCommandBuffer().getExternalData().getWorld();

    if (this.start == null) {
      populateStart(newPos, world);
      return;
    }

    if (!this.ancientConstruct.isValid()) {
      System.out.println("[DebugBuildPath] Previous valid ref is now invalid");
      populateStart(newPos, world);
      return;
    }

    Store<ChunkStore> store = this.ancientConstruct.getStore();

    world.execute(() -> {
      store.addComponent(
          this.ancientConstruct,
          NeedsPathComponent.getComponentType());

      NeedsPathComponent needsPathComponent = store.getComponent(
          this.ancientConstruct,
          NeedsPathComponent.getComponentType());

      if (needsPathComponent == null) {
        System.err.println("[DebugBuildPath] Component nullo dopo add");
        return;
      }

      needsPathComponent.setStart(this.start);
      needsPathComponent.setTarget(newPos);

      System.out.println("[DebugBuildPath] Updated needsPathComponent");
    });
  }

  private void populateStart(Vector3i newPos, World world) {
    world.execute(() -> {
      WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(newPos.x, newPos.z));

      if (chunk == null) {
        System.out.println("[populateStart] Chunk not found");
      }

      Ref<ChunkStore> reference = chunk.getBlockComponentEntity(newPos.x, newPos.y, newPos.z);

      if (reference == null || !reference.isValid()) {
        System.out.println("[populateStart] Invalid ref");
        return;
      }

      AncientConstuctComponent ancientConstruct = reference.getStore()
          .getComponent(reference,
              AncientConstuctComponent.getComponentType());

      if (ancientConstruct == null) {
        System.out.println("[populateStart] No ancientConstruct was found");
        return;
      }

      System.out.println("[populateStart] Found Entity yay");
      this.start = newPos;
      this.ancientConstruct = reference;
    });
  }

}
