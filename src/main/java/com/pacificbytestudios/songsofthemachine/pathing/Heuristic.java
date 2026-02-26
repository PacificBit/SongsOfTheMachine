package com.pacificbytestudios.songsofthemachine.pathing;

import com.hypixel.hytale.math.vector.Vector3i;

public interface Heuristic {

  public int calculateHeuristic(Vector3i current, Vector3i target);

}
