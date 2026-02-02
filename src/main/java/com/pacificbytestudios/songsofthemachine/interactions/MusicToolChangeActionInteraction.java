package com.pacificbytestudios.songsofthemachine.interactions;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.IntStream;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.pacificbytestudios.songsofthemachine.components.MusicToolComponent;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;
import com.pacificbytestudios.songsofthemachine.storage.MusicToolHUIStorage;

public class MusicToolChangeActionInteraction extends SimpleInteraction {
  public static final BuilderCodec<MusicToolChangeActionInteraction> CODEC = BuilderCodec
      .builder(MusicToolChangeActionInteraction.class, MusicToolChangeActionInteraction::new, SimpleInteraction.CODEC)
      .append(new KeyedCodec<>("ChangesToPrevious", Codec.BOOLEAN),
          (obj, value) -> obj.changesToPrev = value,
          obj -> obj.changesToPrev)
      .add()
      .build();

  private static int actionCount;
  private boolean changesToPrev;
  private MusicToolHUIStorage store;

  private static final Map<AncientConstructAction, Integer> ACTION_INDEX = buildIndex(AncientConstructAction.values());

  private static Map<AncientConstructAction, Integer> buildIndex(AncientConstructAction[] cycle) {
    EnumMap<AncientConstructAction, Integer> map = new EnumMap<>(AncientConstructAction.class);

    IntStream.range(0, cycle.length)
        .forEach(i -> {
          map.put(cycle[i], i);
        });

    actionCount = cycle.length;
    return map;
  }

  private static AncientConstructAction step(AncientConstructAction current, int delta) {
    Integer actionIndex = ACTION_INDEX.get(current);
    if (actionIndex == null) {
      actionIndex = 0;
    }
    int nextIdx = (actionIndex + delta) % actionCount;
    if (nextIdx < 0)
      nextIdx += actionCount;
    return AncientConstructAction.values()[nextIdx];
  }

  public MusicToolChangeActionInteraction() {
    this.store = MusicToolHUIStorage.get();
  }

  @Override
  protected void tick0(
      boolean firstRun,
      float time,
      InteractionType type,
      InteractionContext context,
      CooldownHandler cooldownHandler) {

    Ref<EntityStore> playerRef = context.getEntity();
    World world = playerRef.getStore().getExternalData().getWorld();

    world.execute(() -> {
      ItemStack current = context.getHeldItem();

      MusicToolComponent comp = current.getFromMetadataOrDefault(
          MusicToolComponent.METADATA_KEY,
          MusicToolComponent.CODEC);

      AncientConstructAction curr = (comp.getAction() != null)
          ? comp.getAction()
          : AncientConstructAction.IDLE;

      int delta = this.changesToPrev ? -1 : 1;
      AncientConstructAction next = step(curr, delta);
      comp.setAction(next);

      Store<EntityStore> store = world.getEntityStore().getStore();
      Vector3d playerPos = store.getComponent(playerRef, TransformComponent.getComponentType()).getPosition();

      ItemStack updated = current.withMetadata(
          MusicToolComponent.METADATA_KEY,
          MusicToolComponent.CODEC,
          comp);

      ItemStackSlotTransaction transaction = context.getHeldItemContainer()
          .setItemStackForSlot(context.getHeldItemSlot(), updated);

      if (!transaction.succeeded()) {
        System.err.println("[MusicToolChangeAction] Could not complete slot transaction");
        return;
      }

      context.setHeldItem(updated);
      System.err.println("[MusicToolChangeAction] New action: " + next);

      this.store.getMusicToolHui(comp.getUUID()).updateCurrentAction(next);

      SoundUtil.playSoundEvent3dToPlayer(playerRef, SoundEvent.getAssetMap().getIndex(next.getSoundId()),
          SoundCategory.SFX, playerPos, store);
    });
  }

  @Override
  protected MusicToolChangeActionInteraction clone() {
    MusicToolChangeActionInteraction interaction = new MusicToolChangeActionInteraction();
    interaction.changesToPrev = this.changesToPrev;
    return interaction;
  }

}
