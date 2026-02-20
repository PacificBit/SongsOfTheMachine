package com.pacificbytestudios.songsofthemachine.components;

import java.util.UUID;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.inventory.container.EmptyItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import com.pacificbytestudios.songsofthemachine.SongsOfTheMachine;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructStatus;

public class AncientConstuctComponent extends ItemContainerState {
  private static final short STORAGE_CAPACITY = 18;
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
      .append(new KeyedCodec<>("Storage", ItemContainer.CODEC),
          (obj, value) -> obj.storage = value,
          (obj) -> obj.getItemContainer())
      .add()
      .append(new KeyedCodec<>("CooldownMultiplier", Codec.FLOAT),
          (obj, value) -> obj.cooldownMultiplier = value,
          (obj) -> obj.cooldownMultiplier)
      .add()
      .build();

  private AncientConstructStatus status;
  private boolean loopActions;
  private int actionBuffer;
  private byte actionCount;
  private int bkActionBuffer;
  private byte bkActionCount;
  private float clock;
  private float timeout;
  private float cooldown;
  private float cooldownMultiplier = 1;
  private byte actionCapacity;
  private ItemContainer storage;
  private UUID listeningToPlayerInstrumentId;

  public AncientConstuctComponent() {
    this.clock = 0f;
    this.status = AncientConstructStatus.LISTENING;
  }

  public boolean addAction(AncientConstructAction action, UUID listeningTo) {
    if (this.listeningToPlayerInstrumentId == null ||
        !this.listeningToPlayerInstrumentId.equals(listeningTo)) {
      this.resetTime();
      this.listeningToPlayerInstrumentId = listeningTo;
    }

    if (this.actionCount > this.actionCapacity) {
      this.status = AncientConstructStatus.ERROR;
      return false;
    }

    int id = action.getId()
        & 0xFF;
    this.actionBuffer |= (id << (this.actionCount * BIT_ACTION_SIZE));
    this.bkActionBuffer = actionBuffer;
    this.actionCount += 1;
    this.bkActionCount = this.actionCount;

    if (this.actionCount == this.actionCapacity ||
        action.equals(AncientConstructAction.EARLY_EXIT)) {
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

  public boolean hasCooldownTerminated() {
    return this.clock >= this.cooldown;
  }

  public float getTime() {
    return this.clock;
  }

  public void resetTime() {
    this.clock = 0f;
  }

  public void clearActionBuffer() {
    this.status = AncientConstructStatus.LISTENING;
    this.actionBuffer = 0;
    this.bkActionBuffer = 0;
    this.actionCount = 0;
    this.bkActionCount = 0;
    this.loopActions = false;
    this.clock = 0f;
  }

  public int getActionBuffer() {
    return this.actionBuffer;
  }

  public byte getActionCapacity() {
    return actionCapacity;
  }

  public void setActionLoop(boolean loopStatus) {
    this.loopActions = loopStatus;
  }

  public boolean getIsLooping() {
    return this.loopActions;
  }

  public AncientConstructStatus getStatus() {
    return this.status;
  }

  public void setStatus(AncientConstructStatus status) {
    this.status = status;
  }

  public int getBufferLoad() {
    return this.actionCount;
  }

  public void setCooldown(float cooldown) {
    this.cooldown = cooldown * this.cooldownMultiplier;
  }

  public void setActionCapacity(byte actionCapacity) {
    this.status = AncientConstructStatus.LISTENING;
    this.actionCapacity = actionCapacity;
  }

  public boolean canBeInterrupted() {
    return this.status == AncientConstructStatus.LISTENING ||
        this.status == AncientConstructStatus.IDLE ||
        this.status == AncientConstructStatus.COOLDOWN ||
        this.loopActions;
  }

  public UUID getListeningInstrumentId() {
    return this.listeningToPlayerInstrumentId;
  }

  public AncientConstructAction[] getRemainingActions() {
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
      if (this.loopActions) {
        this.actionCount = this.bkActionCount;
        this.actionBuffer = this.bkActionBuffer;
        return;
      }
      System.out.println("[AncientConstuctComponent] clearNextAction - Completed all the scheduled actions");
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
    c.bkActionBuffer = this.bkActionBuffer;
    c.bkActionCount = this.bkActionCount;
    c.loopActions = this.loopActions;
    c.cooldown = this.cooldown;
    c.cooldownMultiplier = this.cooldownMultiplier;
    c.storage = EmptyItemContainer.getNewContainer(STORAGE_CAPACITY, SimpleItemContainer::new);
    return c;
  }

  public void copyFrom(AncientConstuctComponent other) {
    this.status = other.status;
    this.actionBuffer = other.actionBuffer;
    this.actionCount = other.actionCount;
    this.clock = other.clock;
    this.timeout = other.timeout;
    this.actionCapacity = other.actionCapacity;
    this.storage = other.storage;
    this.loopActions = other.loopActions;
    this.bkActionBuffer = other.bkActionBuffer;
    this.bkActionCount = other.bkActionCount;
    this.cooldown = other.cooldown;
    this.cooldownMultiplier = other.cooldownMultiplier;
  }

  @Override
  public ItemContainer getItemContainer() {
    return this.storage == null ? EmptyItemContainer.getNewContainer(STORAGE_CAPACITY, SimpleItemContainer::new)
        : this.storage;
  }

}
