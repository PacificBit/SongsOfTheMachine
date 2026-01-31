package com.pacificbytestudios.songsofthemachine.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import com.pacificbytestudios.songsofthemachine.SongsOfTheMachine;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructStatus;

public class AncientConstuctComponent implements Component<ChunkStore> {
  private static final int BIT_ACTION_SIZE = 8;
  private static final int MASK_SLOT_0 = 0x000000FF;
  private static final int MASK_SLOT_1 = 0x0000FF00;
  private static final int MASK_SLOT_2 = 0x00FF0000;
  private static final int MASK_SLOT_3 = 0xFF000000;

  public static final BuilderCodec<AncientConstuctComponent> CODEC = BuilderCodec
      .builder(AncientConstuctComponent.class, AncientConstuctComponent::new)
      .append(new KeyedCodec<>("Command Timeout", Codec.FLOAT),
          (obj, value) -> obj.timeout = value,
          (obj) -> obj.timeout)
      .add()
      .build();

  private AncientConstructStatus status;
  private int actionBuffer;
  private byte actionCount;
  private float clock;
  private float timeout;
  private byte actionCapacity;

  public AncientConstuctComponent() {
    this.clock = 0f;
    this.status = AncientConstructStatus.LISTENING;
  }

  public boolean addAction(AncientConstructAction action) {
    if (this.actionCount > this.actionCapacity) {
      this.status = AncientConstructStatus.ERROR;
      return false;
    }
    int id = action.getId() & 0xFF;
    this.actionBuffer |= (id << (this.actionCount * BIT_ACTION_SIZE));
    this.actionCount += 1;

    if (this.actionCount == this.actionCapacity) {
      this.status = AncientConstructStatus.READY_TO_EXECUTE;
    }
    return true;
  }

  public void addTime(float deltaTime) {
    this.clock += deltaTime;
  }

  public boolean resetClockIfTimeout() {
    if (this.clock >= this.timeout) {
      this.clock = 0f;
      return true;
    }
    return false;
  }

  public float getTime() {
    return this.clock;
  }

  public void resetTime() {
    this.clock = 0f;
  }

  public void clearActionBuffer() {
    this.status = AncientConstructStatus.IDLE;
    this.actionBuffer = 0;
    this.actionCount = 0;
    this.clock = 0f;
  }

  public int getActionBuffer() {
    return this.actionBuffer;
  }

  public byte getActionCapacity() {
    return actionCapacity;
  }

  public AncientConstructStatus getStatus() {
    return this.status;
  }

  public void setStatus(AncientConstructStatus status) {
    this.status = status;
  }

  public void setActionCapacity(byte actionCapacity) {
    this.status = AncientConstructStatus.LISTENING;
    this.actionCapacity = actionCapacity;
  }

  public AncientConstructAction[] getActions() {
    byte a0 = (byte) ((actionBuffer & MASK_SLOT_0) >>> 0);
    byte a1 = (byte) ((actionBuffer & MASK_SLOT_1) >>> 8);
    byte a2 = (byte) ((actionBuffer & MASK_SLOT_2) >>> 16);
    byte a3 = (byte) ((actionBuffer & MASK_SLOT_3) >>> 24);

    return new AncientConstructAction[] {
        AncientConstructAction.fromByte(a0),
        AncientConstructAction.fromByte(a1),
        AncientConstructAction.fromByte(a2),
        AncientConstructAction.fromByte(a3)
    };
  }

  public AncientConstructAction getNextAction() {
    if (this.actionCount == 0) {
      return null;
    }
    return AncientConstructAction.fromByte((byte) (actionBuffer & MASK_SLOT_0));
  }

  public void clearNextAction() {
    if (this.actionCount <= 0)
      return;

    this.actionBuffer = this.actionBuffer >>> BIT_ACTION_SIZE;
    this.actionCount--;

    if (this.actionCount == 0) {
      this.status = AncientConstructStatus.COMPLETED;
    }
  }

  public static ComponentType<ChunkStore, AncientConstuctComponent> getComponentType() {
    return SongsOfTheMachine.get().getAncientConstructorComponentType();
  }

  @Override
  public AncientConstuctComponent clone() {
    AncientConstuctComponent c = new AncientConstuctComponent();
    c.status = AncientConstructStatus.LISTENING;
    c.actionBuffer = this.actionBuffer;
    c.actionCount = this.actionCount;
    c.timeout = this.timeout;
    c.actionCapacity = this.actionCapacity;
    return c;
  }

  public void copyFrom(AncientConstuctComponent other) {
    this.status = other.status;
    this.actionBuffer = other.actionBuffer;
    this.actionCount = other.actionCount;
    this.clock = other.clock;
    this.timeout = other.timeout;
    this.actionCapacity = other.actionCapacity;
  }

}
