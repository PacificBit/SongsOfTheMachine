package com.pacificbytestudios.songsofthemachine.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import com.pacificbytestudios.songsofthemachine.SongsOfTheMachine;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructActions;

public class AncientConstuctComponent implements Component<ChunkStore> {
  public static final BuilderCodec<AncientConstuctComponent> CODEC = BuilderCodec
      .builder(AncientConstuctComponent.class, AncientConstuctComponent::new)

      .append(new KeyedCodec<>("State", Codec.BYTE),
          (obj, value) -> obj.state = AncientConstructActions.fromByte(value),
          (obj) -> obj.state.getId())
      .add()

      .build();
  private static final int BIT_ACTION_SIZE = 8;
  private static final int ACTION_BUFFER_SIZE = 4;
  private static final int MASK_SLOT_0 = 0x000000FF;
  private static final int MASK_SLOT_1 = 0x0000FF00;
  private static final int MASK_SLOT_2 = 0x00FF0000;
  private static final int MASK_SLOT_3 = 0xFF000000;

  private AncientConstructActions state;
  private int actionBuffer;
  private byte actionCount;

  public AncientConstuctComponent() {
  }

  public AncientConstructActions getState() {
    return state;
  }

  public void setState(AncientConstructActions action) {
    this.state = action;
  }

  public boolean addAction(AncientConstructActions action) {
    if (actionCount >= ACTION_BUFFER_SIZE) {
      return false;
    }
    this.actionBuffer |= (action.getId() << (actionCount * BIT_ACTION_SIZE));
    actionCount += 1;
    return true;
  }

  public void clearActionBuffer() {
    this.actionBuffer = 0;
    this.actionCount = 0;
  }

  public int getActionBuffer() {
    return this.actionBuffer;
  }

  public AncientConstructActions[] getActions() {
    byte a0 = (byte) ((actionBuffer & MASK_SLOT_0) >>> 0);
    byte a1 = (byte) ((actionBuffer & MASK_SLOT_1) >>> 8);
    byte a2 = (byte) ((actionBuffer & MASK_SLOT_2) >>> 16);
    byte a3 = (byte) ((actionBuffer & MASK_SLOT_3) >>> 24);

    return new AncientConstructActions[] {
        AncientConstructActions.fromByte(a0),
        AncientConstructActions.fromByte(a1),
        AncientConstructActions.fromByte(a2),
        AncientConstructActions.fromByte(a3)
    };
  }

  public static ComponentType<ChunkStore, AncientConstuctComponent> getComponentType() {
    return SongsOfTheMachine.get().getAncientConstructorComponentType();
  }

  @Override
  public AncientConstuctComponent clone() {
    AncientConstuctComponent c = new AncientConstuctComponent();
    c.state = this.state;
    c.actionBuffer = this.actionBuffer;
    c.actionCount = this.actionCount;
    return c;
  }
}
