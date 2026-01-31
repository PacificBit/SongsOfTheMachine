package com.pacificbytestudios.songsofthemachine.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class Utils {

  public record WorldContext(
      WorldChunk chunk,
      World world) {
    public boolean isValid() {
      return chunk != null && world != null;
    }

    public WorldChunk getChunk() {
      return chunk;
    }

    public World getWorld() {
      return world;
    }
  }

  public static WorldContext getWorldContext(CommandBuffer<ChunkStore> cb, Ref<ChunkStore> ref) {
    WorldChunk worldChunk = cb.getComponent(ref, WorldChunk.getComponentType());
    if (worldChunk == null) {
      return new WorldContext(null, null);
    }
    World world = worldChunk.getWorld();
    return new WorldContext(worldChunk, world);
  }

  public static Vector3i getBlockPosition(WorldChunk chunk, int chunkIndex) {
    int x = ChunkUtil.xFromBlockInColumn(chunkIndex);
    int y = ChunkUtil.yFromBlockInColumn(chunkIndex);
    int z = ChunkUtil.zFromBlockInColumn(chunkIndex);
    return new Vector3i(x + (chunk.getX() << 5), y, z + (chunk.getZ() << 5));
  }

}
