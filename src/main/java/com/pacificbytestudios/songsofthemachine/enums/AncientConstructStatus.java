package com.pacificbytestudios.songsofthemachine.enums;

import java.util.HashMap;
import java.util.Map;

public enum AncientConstructStatus {
  IDLE((byte) 0),
  LISTENING((byte) 1),
  READY_TO_EXECUTE((byte) 2),
  EXECUTING((byte) 3),
  COMEPLETED((byte) 4),
  COOLDOWN((byte) 5),
  ERROR((byte) 6);

  private static final Map<Byte, AncientConstructStatus> BY_ID = new HashMap<>();

  static {
    for (AncientConstructStatus action : values()) {
      BY_ID.put(action.id, action);
    }
  }

  private final byte id;

  AncientConstructStatus(byte id) {
    this.id = id;
  }

  public byte getId() {
    return id;
  }

  public static AncientConstructStatus fromByte(byte id) {
    return BY_ID.get(id);
  }
}
