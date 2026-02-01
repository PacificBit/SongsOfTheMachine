package com.pacificbytestudios.songsofthemachine.enums;

import java.util.HashMap;
import java.util.Map;

public enum AncientConstructAction {
  IDLE((byte) 0, "Idle", 0.15f),
  MOVE_FORWARD((byte) 1, "Advance", 0.25f),
  TURN_LEFT((byte) 2, "Turn Left", 0.25f),
  TURN_RIGHT((byte) 3, "Turn Right", 0.25f),
  BASIC_BREAK_BLOCK((byte) 4, "Chisel", 0.25f),
  COMPLEX_BREAK_BLOCK((byte) 5, "Excavate", 0.25f),
  DROP_IN_CONTAINER((byte) 6, "Deposit", 0.25f);

  private static final Map<Byte, AncientConstructAction> BY_ID = new HashMap<>();
  private static final Map<AncientConstructAction, int[]> ACTION_TO_EXCAVATION_SIZE_MAP = new HashMap<>();

  static {
    for (AncientConstructAction action : values()) {
      BY_ID.put(action.id, action);
    }

    ACTION_TO_EXCAVATION_SIZE_MAP.put(
        AncientConstructAction.BASIC_BREAK_BLOCK,
        new int[] { 1, 2 });

    ACTION_TO_EXCAVATION_SIZE_MAP.put(
        AncientConstructAction.COMPLEX_BREAK_BLOCK,
        new int[] { 3, 3 });
  }

  private final byte id;
  private final String name;
  private final float executionTime; // in secs

  AncientConstructAction(byte id, String name, float executionTime) {
    this.id = id;
    this.executionTime = executionTime;
    this.name = name;
  }

  public byte getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public float getExecutionTime() {
    return this.executionTime;
  }

  public static AncientConstructAction fromByte(byte id) {
    return BY_ID.get(id);
  }

  public static int[] getExcavationSizeFor(AncientConstructAction action) {
    if (ACTION_TO_EXCAVATION_SIZE_MAP.containsKey(action)) {
      return ACTION_TO_EXCAVATION_SIZE_MAP.get(action);
    }
    return null;
  }

}
