package com.pacificbytestudios.songsofthemachine.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.pacificbytestudios.songsofthemachine.SongsOfTheMachine;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;

public class MusicToolComponent implements Component<ChunkStore> {
  public static final String METADATA_KEY = "SongsOfTheMachine.MusicToolComponent";
  public static final BuilderCodec<MusicToolComponent> CODEC = BuilderCodec
      .builder(MusicToolComponent.class, MusicToolComponent::new)
      .append(new KeyedCodec<>("Action", Codec.BYTE),
          (obj, value) -> obj.setAction(AncientConstructAction.fromByte(value)),
          (obj) -> obj.getAction() == null ? 0 : obj.getAction().getId())
      .add()
      .build();

  private AncientConstructAction action;

  public AncientConstructAction getAction() {
    return this.action;
  }

  public void setAction(AncientConstructAction action) {
    this.action = action;
  }

  public static ComponentType<ChunkStore, MusicToolComponent> getComponentType() {
    return SongsOfTheMachine.get().getMusicToolComponentType();
  }

  @Override
  public MusicToolComponent clone() {
    MusicToolComponent component = new MusicToolComponent();
    component.action = this.action;
    return component;
  }
}
