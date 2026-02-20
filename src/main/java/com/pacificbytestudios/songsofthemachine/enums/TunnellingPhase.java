package com.pacificbytestudios.songsofthemachine.enums;

import java.util.HashMap;
import java.util.Map;

public enum TunnellingPhase {
  MOVE_FORWARD((short) 1),
  EXCAVATE((short) 2),
  RETURN_TO_ORIGIN((short) 3);

  public static final int MAX_RANGE = 32;
  private final short phase;
  private static Map<Short, TunnellingPhase> BY_ID = new HashMap<>();

  static {
    for (TunnellingPhase phase : TunnellingPhase.values()) {
      BY_ID.put(phase.getPhase(), phase);
    }
  }

  TunnellingPhase(short phase) {
    this.phase = phase;
  }

  public short getPhase() {
    return phase;
  }

  public static TunnellingPhase fromId(short id) {
    return BY_ID.get(id);
  }
}
