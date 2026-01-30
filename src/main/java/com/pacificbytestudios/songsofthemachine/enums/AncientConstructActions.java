package com.pacificbytestudios.songsofthemachine.enums;

import java.util.HashMap;
import java.util.Map;

public enum AncientConstructActions {
  IDLE((byte) 0),
  MOVE_FORWARD((byte) 1),
  TURN_LEFT((byte) 2),
  TURN_RIGHT((byte) 3),
  BREAK_BLOCK((byte) 4);

  private static final Map<Byte, AncientConstructActions> BY_ID = new HashMap<>();

  static {
    for (AncientConstructActions action : values()) {
      BY_ID.put(action.id, action);
    }
  }

  private final byte id;

  AncientConstructActions(byte id) {
    this.id = id;
  }

  public byte getId() {
    return id;
  }

  public static AncientConstructActions fromByte(byte id) {
    return BY_ID.get(id);
  }
}
