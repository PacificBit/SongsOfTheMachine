package com.pacificbytestudios.songsofthemachine.storage;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class AncientConstructStore {
  private static AncientConstructStore INSTANCE;

  private Set<Ref<ChunkStore>> ancientConstuctComponents = new HashSet<>();
  private Map<Ref<ChunkStore>, Set<Ref<ChunkStore>>> ancientConstructInChunk = new ConcurrentHashMap<>();

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

  public void setAncientConstructChunkId(Ref<ChunkStore> chunkRef, Ref<ChunkStore> ancientConstructRef) {
    if (!ancientConstructInChunk.containsKey(chunkRef)) {
      ancientConstructInChunk.put(chunkRef, new HashSet<>());
    }
    ancientConstructInChunk.get(chunkRef).add(ancientConstructRef);
  }

  public void removeAncientConstructFromChunkId(Ref<ChunkStore> chunkRef, Ref<ChunkStore> ancientConstructRef) {
    if (!ancientConstructInChunk.containsKey(chunkRef)) {
      return;
    }
    ancientConstructInChunk.get(chunkRef).remove(ancientConstructRef);
  }

  public Set<Ref<ChunkStore>> getAncientConstructInChunk(Ref<ChunkStore> chunkRef) {
    if (!this.ancientConstructInChunk.containsKey(chunkRef)) {
      return null;
    }
    return this.ancientConstructInChunk.get(chunkRef);
  }

}
