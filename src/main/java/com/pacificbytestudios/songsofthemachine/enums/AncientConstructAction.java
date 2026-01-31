package com.pacificbytestudios.songsofthemachine.enums;

import java.util.HashMap;
import java.util.Map;

public enum AncientConstructAction {
  IDLE((byte) 0, 1),
  MOVE_FORWARD((byte) 1, 0.15f),
  TURN_LEFT((byte) 2, 0.15f),
  TURN_RIGHT((byte) 3, 0.15f),
  BREAK_BLOCK((byte) 4, 1);

  private static final Map<Byte, AncientConstructAction> BY_ID = new HashMap<>();

  static {
    for (AncientConstructAction action : values()) {
      BY_ID.put(action.id, action);
    }
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
}
