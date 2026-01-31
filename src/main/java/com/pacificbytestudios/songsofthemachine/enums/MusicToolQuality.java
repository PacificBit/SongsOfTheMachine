package com.pacificbytestudios.songsofthemachine.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum MusicToolQuality {
  POOR((byte) 1, (byte) 16),
  MEDIUM((byte) 2, (byte) 32),
  GREAT((byte) 3, (byte) 64);

  private final byte id;
  private final byte range;

  private static final Map<Byte, MusicToolQuality> BY_ID;

  static {
    Map<Byte, MusicToolQuality> map = new HashMap<>();
    for (MusicToolQuality q : values()) {
      map.put(q.id, q);
    }
    BY_ID = Collections.unmodifiableMap(map);
  }

  MusicToolQuality(byte id, byte range) {
    this.id = id;
    this.range = range;
  }

  public byte getId() {
    return id;
  }

  public byte getRange() {
    return range;
  }

  public static MusicToolQuality fromId(byte id) {
    return BY_ID.get(id);
  }
}
