package com.pacificbytestudios.songsofthemachine.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
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

    public String toString() {
      if (!isValid()) {
        return "World Context - Missing World or WorldChunk";
      }
      return "World Context - WorldName=" +
          this.world.getName() +
          ", ChunkCoords=(" +
          this.chunk.getX() + ", " +
          this.chunk.getZ() + ")";
    }
  }

  public static WorldContext getWorldContextFromInfo(
      CommandBuffer<ChunkStore> cb,
      BlockModule.BlockStateInfo info) {

    if (info == null)
      return new Utils.WorldContext(null, null);

    Ref<ChunkStore> chunkRef = info.getChunkRef();
    if (chunkRef == null)
      return new Utils.WorldContext(null, null);

    WorldChunk worldChunk = cb.getComponent(chunkRef, WorldChunk.getComponentType());
    if (worldChunk == null)
      return new Utils.WorldContext(null, null);

    World world = worldChunk.getWorld();
    return new Utils.WorldContext(worldChunk, world);
  }

  public static WorldContext getWorldContext(CommandBuffer<ChunkStore> cb, Ref<ChunkStore> ref) {
    WorldChunk worldChunk = cb.getComponent(ref, WorldChunk.getComponentType());
    if (worldChunk == null) {
      return new WorldContext(null, null);
    }
    World world = worldChunk.getWorld();
    return new WorldContext(worldChunk, world);
  }

  public static Vector3i getBlockPosition(WorldChunk chunk, int columnIndex) {
    if (chunk == null) {
      return null;
    }
    int x = ChunkUtil.xFromBlockInColumn(columnIndex);
    int y = ChunkUtil.yFromBlockInColumn(columnIndex);
    int z = ChunkUtil.zFromBlockInColumn(columnIndex);
    return new Vector3i(x + (chunk.getX() << 5), y, z + (chunk.getZ() << 5));
  }

}
