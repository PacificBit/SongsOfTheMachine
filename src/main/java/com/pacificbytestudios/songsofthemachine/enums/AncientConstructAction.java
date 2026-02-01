package com.pacificbytestudios.songsofthemachine.enums;

import java.util.HashMap;
import java.util.Map;

public enum AncientConstructAction {
  IDLE((byte) 0, 0.15f),
  MOVE_FORWARD((byte) 1, 0.25f),
  TURN_LEFT((byte) 2, 0.25f),
  TURN_RIGHT((byte) 3, 0.25f),
  BASIC_BREAK_BLOCK((byte) 4, 0.25f),
  COMPLEX_BREAK_BLOCK((byte) 5, 0.25f);

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
  private final float executionTime; // in secs

  AncientConstructAction(byte id, float executionTime) {
    this.id = id;
    this.executionTime = executionTime;
  }

  public byte getId() {
    return this.id;
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
