package com.pacificbytestudios.songsofthemachine.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.pacificbytestudios.songsofthemachine.hui.ToolSelectionHUI;

public class MusicToolHUIStorage {
  private static MusicToolHUIStorage INSTANCE;

  private Map<UUID, ToolSelectionHUI> musicToolHuiMap;

  private MusicToolHUIStorage() {
    musicToolHuiMap = new ConcurrentHashMap<>();
  }

  public static MusicToolHUIStorage get() {
    if (INSTANCE == null) {
      INSTANCE = new MusicToolHUIStorage();
    }
    return INSTANCE;
  }

  public ToolSelectionHUI getMusicToolHui(UUID musicToolId) {
    return musicToolHuiMap.get(musicToolId);
  }

  public void addMusicToolHui(UUID musicToolId, ToolSelectionHUI hui) {
    this.musicToolHuiMap.put(musicToolId, hui);
  }

}
