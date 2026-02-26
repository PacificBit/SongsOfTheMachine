package com.pacificbytestudios.songsofthemachine.components;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.pacificbytestudios.songsofthemachine.SongsOfTheMachine;

public class NeedsPathComponent implements Component<ChunkStore> {
  public static final BuilderCodec<NeedsPathComponent> CODEC = BuilderCodec
      .builder(NeedsPathComponent.class, NeedsPathComponent::new)

      .append(new KeyedCodec<>("Start", Vector3i.CODEC),
          (obj, val) -> obj.start = val,
          (obj) -> obj.start)
      .add()

      .append(new KeyedCodec<>("Target", Vector3i.CODEC),
          (obj, val) -> obj.target = val,
          (obj) -> obj.target)
      .add()

      .build();

  private Vector3i start;
  private Vector3i target;

  public Vector3i getStart() {
    return start;
  }

  public void setStart(Vector3i start) {
    this.start = start;
  }

  public Vector3i getTarget() {
    return target;
  }

  public void setTarget(Vector3i target) {
    this.target = target;
  }

  @Override
  public NeedsPathComponent clone() {
    NeedsPathComponent component = new NeedsPathComponent();
    return component;
  }

  public static ComponentType<ChunkStore, NeedsPathComponent> getComponentType() {
    return SongsOfTheMachine.get().getNeedsPathComponentType();
  }
}
