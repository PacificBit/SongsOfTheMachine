package com.pacificbytestudios.songsofthemachine.enums;

import java.util.HashMap;
import java.util.Map;

public enum AncientConstructActions {
  IDLE((byte) 0, 1),
  MOVE_FORWARD((byte) 1, 1),
  TURN_LEFT((byte) 2, 1),
  TURN_RIGHT((byte) 3, 1),
  BREAK_BLOCK((byte) 4, 1);

  private static final Map<Byte, AncientConstructActions> BY_ID = new HashMap<>();

  static {
    for (AncientConstructActions action : values()) {
      BY_ID.put(action.id, action);
    }
  }

  private final byte id;
  private final float executionTime; // in secs

  AncientConstructActions(byte id, float executionTime) {
    this.id = id;
    this.executionTime = executionTime;
  }

  public byte getId() {
    return this.id;
  }

  public float getExecutionTime() {
    return this.executionTime;
  }

  public static AncientConstructActions fromByte(byte id) {
    return BY_ID.get(id);
  }
}
