package com.pacificbytestudios.songsofthemachine.pathing;

import com.hypixel.hytale.math.vector.Vector3i;

public class ManhattanHeuristic implements Heuristic {

  @Override
  public int calculateHeuristic(Vector3i current, Vector3i target) {
    return Math.abs(current.x - target.x) + Math.abs(current.y - target.y) + Math.abs(current.z - target.z);
  }

}
