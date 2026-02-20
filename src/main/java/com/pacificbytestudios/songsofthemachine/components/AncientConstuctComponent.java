package com.pacificbytestudios.songsofthemachine.components;

import java.util.ArrayDeque;
import java.util.UUID;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.container.EmptyItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import com.pacificbytestudios.songsofthemachine.SongsOfTheMachine;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructStatus;
import com.pacificbytestudios.songsofthemachine.utils.Vector3iArrayDequeCodec;

public class AncientConstuctComponent extends ItemContainerState {
  private static final short STORAGE_CAPACITY = 18;
  private static final int BIT_ACTION_SIZE = 4;
  private static final long ACTION_MASK = 0xFL;
  public static final short EMPTY_SUB_INSTR = -1;

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

      // used to wake up entity when the chunk loads back
      .append(new KeyedCodec<>("ActionBuffer", Codec.LONG),
          (obj, value) -> obj.actionBuffer = value,
          (obj) -> obj.actionBuffer)
      .add()

      .append(new KeyedCodec<>("BKActionBuffer", Codec.LONG),
          (obj, value) -> obj.bkActionBuffer = value,
          (obj) -> obj.bkActionBuffer)
      .add()

      .append(new KeyedCodec<>("ActionCount", Codec.BYTE),
          (obj, value) -> obj.actionCount = value,
          (obj) -> obj.actionCount)
      .add()

      .append(new KeyedCodec<>("BKActionCount", Codec.BYTE),
          (obj, value) -> obj.bkActionCount = value,
          (obj) -> obj.bkActionCount)
      .add()

      .append(new KeyedCodec<>("Clock", Codec.FLOAT),
          (obj, value) -> obj.clock = value,
          (obj) -> obj.clock)
      .add()

      .append(new KeyedCodec<>("Timeout", Codec.FLOAT),
          (obj, value) -> obj.timeout = value,
          (obj) -> obj.timeout)
      .add()

      .append(new KeyedCodec<>("Cooldown", Codec.FLOAT),
          (obj, value) -> obj.cooldown = value,
          (obj) -> obj.cooldown)
      .add()

      .append(new KeyedCodec<>("ActionCapacity", Codec.BYTE),
          (obj, value) -> obj.actionCapacity = value,
          (obj) -> obj.actionCapacity)
      .add()

      .append(new KeyedCodec<>("ListeningToPlayerInstrumentId", Codec.STRING),
          (obj, value) -> obj.listeningToPlayerInstrumentId = value == null ? null : UUID.fromString(value),
          (obj) -> obj.listeningToPlayerInstrumentId == null ? null : obj.listeningToPlayerInstrumentId.toString())
      .add()

      .append(new KeyedCodec<>("LoopActions", Codec.BOOLEAN),
          (obj, value) -> obj.loopActions = value,
          (obj) -> obj.loopActions)
      .add()

      .append(new KeyedCodec<>("Status", Codec.BYTE),
          (obj, value) -> obj.status = AncientConstructStatus.fromByte(value),
          (obj) -> obj.status.getId())
      .add()

      .append(new KeyedCodec<>("InstructionSubPhase", Codec.SHORT),
          (obj, value) -> obj.instructionSubPhase = value,
          (obj) -> obj.instructionSubPhase)
      .add()

      .append(new KeyedCodec<>("Waypoints", new Vector3iArrayDequeCodec()),
          (obj, value) -> obj.waypoints = ((value == null) ? new ArrayDeque<>() : value),
          (obj) -> obj.waypoints)
      .add()

      .append(new KeyedCodec<>("TargetPos", Vector3i.CODEC),
          (obj, value) -> obj.targetPos = value,
          (obj) -> obj.targetPos)
      .add()

      .build();

  private AncientConstructStatus status;
  private boolean loopActions;
  private long actionBuffer = 0L;
  private long bkActionBuffer = 0L;
  private byte actionCount;
  private byte bkActionCount;
  private float clock;
  private float timeout;
  private float cooldown;
  private float cooldownMultiplier = 1;
  private byte actionCapacity;
  private ItemContainer storage;
  private short instructionSubPhase = EMPTY_SUB_INSTR;
  private UUID listeningToPlayerInstrumentId;
  private ArrayDeque<Vector3i> waypoints = new ArrayDeque<>();
  private Vector3i targetPos;

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

    if (this.actionCount >= this.actionCapacity) {
      this.status = AncientConstructStatus.ERROR;
      return false;
    }

    long id = (long) (action.getId() & 0x0F);
    int shift = this.actionCount * BIT_ACTION_SIZE;
    this.actionBuffer |= (id << shift);
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
    this.actionBuffer = 0L;
    this.bkActionBuffer = 0L;
    this.actionCount = 0;
    this.bkActionCount = 0;
    this.loopActions = false;
    this.clock = 0f;
    this.instructionSubPhase = EMPTY_SUB_INSTR;
    this.waypoints.clear();
  }

  public long getActionBuffer() {
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

  public ArrayDeque<Vector3i> getWaypoints() {
    return this.waypoints;
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

  public short getInstructionSubPhase() {
    return this.instructionSubPhase;
  }

  public void setInstructionSubPhase(short subPhase) {
    this.instructionSubPhase = subPhase;
  }

  public AncientConstructAction[] getRemainingActions() {
    long buffer = this.actionBuffer;
    AncientConstructAction[] remainingActions = new AncientConstructAction[this.actionCount];

    for (int i = 0; i < this.actionCount; i++) {
      byte id = (byte) (buffer & ACTION_MASK);
      remainingActions[i] = AncientConstructAction.fromByte(id);
      buffer >>>= BIT_ACTION_SIZE;
    }

    return remainingActions;
  }

  public AncientConstructAction getNextAction() {
    if (this.actionCount == 0) {
      return null;
    }
    return AncientConstructAction.fromByte((byte) (this.actionBuffer & ACTION_MASK));
  }

  public AncientConstructAction getFollowingAction() {
    if (this.actionCount < 2) {
      return null;
    }
    long following = (this.actionBuffer >>> BIT_ACTION_SIZE) & ACTION_MASK;
    return AncientConstructAction.fromByte((byte) following);
  }

  public Vector3i getTargetPos() {
    return this.targetPos;
  }

  public void setTargetPos(Vector3i target) {
    this.targetPos = target;
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
      // System.out.println("[AncientConstuctComponent] clearNextAction - Completed
      // all the scheduled actions");
      this.status = AncientConstructStatus.COMPLETED;
    }
  }

  public static ComponentType<ChunkStore, AncientConstuctComponent> getComponentType() {
    return SongsOfTheMachine.get().getAncientConstructorComponentType();
  }

  @Override
  public AncientConstuctComponent clone() {
    AncientConstuctComponent c = new AncientConstuctComponent();
    c.status = this.status;
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
    c.waypoints = new ArrayDeque<>();
    c.instructionSubPhase = this.instructionSubPhase;
    c.targetPos = this.targetPos;
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
    this.waypoints = other.waypoints;
    this.instructionSubPhase = other.instructionSubPhase;
    this.targetPos = other.targetPos;
  }

  @Override
  public ItemContainer getItemContainer() {
    return this.storage == null ? EmptyItemContainer.getNewContainer(STORAGE_CAPACITY, SimpleItemContainer::new)
        : this.storage;
  }
}
