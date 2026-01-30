package com.pacificbytestudios.songsofthemachine.storage;

import java.util.HashSet;
import java.util.Set;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class AncientConstructStore {
  private static AncientConstructStore INSTANCE;

  private Set<Ref<ChunkStore>> ancientConstuctComponents = new HashSet<>();

  private AncientConstructStore() {
  }

  public static AncientConstructStore get() {
    if (INSTANCE == null) {
      INSTANCE = new AncientConstructStore();
    }
    return INSTANCE;
  }

  public void addAncient(Ref<ChunkStore> ref) {
    ancientConstuctComponents.add(ref);
  }

  public void removeAncient(Ref<ChunkStore> ref) {
    if (ancientConstuctComponents.contains(ref)) {
      ancientConstuctComponents.remove(ref);
    }
  }

  public Set<Ref<ChunkStore>> getAncientConstuctComponents() {
    return ancientConstuctComponents;
  }

}
