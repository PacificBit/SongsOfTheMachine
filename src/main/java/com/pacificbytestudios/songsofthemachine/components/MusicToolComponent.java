package com.pacificbytestudios.songsofthemachine.components;

import java.util.UUID;

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
  public static final byte MAX_CAPACITY = 4;

  public static final BuilderCodec<MusicToolComponent> CODEC = BuilderCodec
      .builder(MusicToolComponent.class, MusicToolComponent::new)
      .append(new KeyedCodec<>("Action", Codec.BYTE),
          (obj, value) -> obj.setAction(AncientConstructAction.fromByte(value)),
          (obj) -> obj.getAction() == null ? 0 : obj.getAction().getId())
      .add()

      .append(new KeyedCodec<>("InstrumentID", Codec.UUID_STRING),
          (obj, value) -> obj.uuid = value,
          (obj) -> obj.uuid)
      .add()

      .append(new KeyedCodec<>("Capacity", Codec.BYTE),
          (obj, value) -> obj.capacity = value,
          (obj) -> obj.capacity)
      .add()

      .build();

  public static ComponentType<ChunkStore, MusicToolComponent> getComponentType() {
    return SongsOfTheMachine.get().getMusicToolComponentType();
  }

  private AncientConstructAction action;
  private UUID uuid;
  private byte capacity = -1;

  public AncientConstructAction getAction() {
    return this.action;
  }

  public void setAction(AncientConstructAction action) {
    this.action = action;
  }

  public UUID getUUID() {
    return this.uuid;
  }

  public void setUUID(UUID uuid) {
    this.uuid = uuid;
  }

  public void setCapacity(byte capacity) {
    this.capacity = capacity;
  }

  public byte getCapacity() {
    return this.capacity;
  }

  @Override
  public MusicToolComponent clone() {
    MusicToolComponent component = new MusicToolComponent();
    component.action = this.action;
    component.uuid = this.uuid;
    component.capacity = this.capacity;
    return component;
  }

}
