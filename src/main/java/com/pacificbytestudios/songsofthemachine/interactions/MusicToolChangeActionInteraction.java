package com.pacificbytestudios.songsofthemachine.interactions;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.pacificbytestudios.songsofthemachine.components.MusicToolComponent;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;

public class MusicToolChangeActionInteraction extends SimpleInteraction {

  public static final BuilderCodec<MusicToolChangeActionInteraction> CODEC = BuilderCodec
      .builder(MusicToolChangeActionInteraction.class, MusicToolChangeActionInteraction::new, SimpleInteraction.CODEC)
      .build();

  public static final KeyedCodec<MusicToolComponent> MUSIC_TOOL_KEY = new KeyedCodec<>("MusicToolComponent",
      MusicToolComponent.CODEC);

  public MusicToolChangeActionInteraction() {
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

      MusicToolComponent comp = current.getFromMetadataOrDefault(MusicToolComponent.METADATA_KEY,
          MusicToolComponent.CODEC);
      AncientConstructAction curr = comp.getAction() != null ? comp.getAction() : AncientConstructAction.IDLE;

      switch (curr) {
        case IDLE -> comp.setAction(AncientConstructAction.MOVE_FORWARD);
        case MOVE_FORWARD -> comp.setAction(AncientConstructAction.TURN_LEFT);
        case TURN_LEFT -> comp.setAction(AncientConstructAction.TURN_RIGHT);
        case TURN_RIGHT -> comp.setAction(AncientConstructAction.BASIC_BREAK_BLOCK);
        case BASIC_BREAK_BLOCK -> comp.setAction(AncientConstructAction.COMPLEX_BREAK_BLOCK);
        case COMPLEX_BREAK_BLOCK -> comp.setAction(AncientConstructAction.IDLE);
        default -> comp.setAction(AncientConstructAction.IDLE);
      }

      world.sendMessage(Message.raw("Tool changed to: " + comp.getAction()));

      ItemStack updated = current.withMetadata(MusicToolComponent.METADATA_KEY, MusicToolComponent.CODEC,
          comp);

      ItemStackSlotTransaction transaction = context.getHeldItemContainer()
          .setItemStackForSlot(context.getHeldItemSlot(), updated);

      if (!transaction.succeeded()) {
        System.out.println("[MusicToolChangeAction] Could not complete slot transaction");
        return;
      }

      context.setHeldItem(updated);

      System.out.println(
          "[MusicToolChangeAction] New action: " + comp.getAction());
    });
  }

  @Override
  protected MusicToolChangeActionInteraction clone() {
    MusicToolChangeActionInteraction interaction = new MusicToolChangeActionInteraction();
    return interaction;
  }

}
