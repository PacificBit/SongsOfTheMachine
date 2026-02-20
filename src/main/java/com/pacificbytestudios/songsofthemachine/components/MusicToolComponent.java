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
  public static final byte MAX_CAPACITY = 8;

  public static final byte LOW_CAPACITY = 1;
  public static final byte AVG_CAPACITY = 4;
  public static final byte GREAT_CAPACITY = 8;

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

      .append(new KeyedCodec<>("Quality", Codec.BYTE),
          (obj, value) -> obj.quality = value,
          (obj) -> obj.quality)
      .add()

      .build();

  public static ComponentType<ChunkStore, MusicToolComponent> getComponentType() {
    return SongsOfTheMachine.get().getMusicToolComponentType();
  }

  private AncientConstructAction action;
  private UUID uuid;
  private byte capacity = -1;
  private byte quality = -1;

  public byte getQuality() {
    return quality;
  }

  public void setQuality(byte quality) {
    this.quality = quality;
  }

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
    component.quality = this.quality;
    return component;
  }

}
