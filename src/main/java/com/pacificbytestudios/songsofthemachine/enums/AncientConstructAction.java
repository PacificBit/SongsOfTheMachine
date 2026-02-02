package com.pacificbytestudios.songsofthemachine.enums;

import java.util.HashMap;
import java.util.Map;

public enum AncientConstructAction {
  IDLE((byte) 0, "Idle", 0.25f),
  MOVE_FORWARD((byte) 1, "Advance", 0.25f),
  MOVE_BACK((byte) 2, "Retreat", 0.25f),
  TURN_LEFT((byte) 3, "Turn Left", 0.25f),
  TURN_RIGHT((byte) 4, "Turn Right", 0.25f),
  BASIC_BREAK_BLOCK((byte) 5, "Chisel", 0.25f),
  COMPLEX_BREAK_BLOCK((byte) 6, "Excavate", 0.25f),
  DROP_IN_CONTAINER((byte) 7, "Deposit", 0.25f),
  TAKE_OUTPUT_BENCH((byte) 8, "Collect", 0.25f);

  private static final Map<Byte, AncientConstructAction> BY_ID = new HashMap<>();
  private static final Map<AncientConstructAction, int[]> ACTION_TO_EXCAVATION_SIZE_MAP = new HashMap<>();
  private static final Map<AncientConstructAction, String> ACTION_TO_UI_ID_MAP = new HashMap<>();

  static {
    for (AncientConstructAction action : values()) {
      BY_ID.put(action.id, action);
    }

    ACTION_TO_EXCAVATION_SIZE_MAP.put(
        AncientConstructAction.BASIC_BREAK_BLOCK,
        new int[] { 1, 2 });

    ACTION_TO_EXCAVATION_SIZE_MAP.put(
        AncientConstructAction.COMPLEX_BREAK_BLOCK,
        new int[] { 3, 3 });
  }

  private final byte id;
  private final String name;
  private final float executionTime; // in secs

  static {
    for (AncientConstructAction action : values()) {
      BY_ID.put(action.id, action);
    }

    ACTION_TO_EXCAVATION_SIZE_MAP.put(BASIC_BREAK_BLOCK, new int[] { 1, 2 });
    ACTION_TO_EXCAVATION_SIZE_MAP.put(COMPLEX_BREAK_BLOCK, new int[] { 3, 3 });

    ACTION_TO_UI_ID_MAP.put(IDLE, "Idle");
    ACTION_TO_UI_ID_MAP.put(MOVE_FORWARD, "MoveForward");
    ACTION_TO_UI_ID_MAP.put(MOVE_BACK, "MoveBack");
    ACTION_TO_UI_ID_MAP.put(TURN_LEFT, "TurnLeft");
    ACTION_TO_UI_ID_MAP.put(TURN_RIGHT, "TurnRight");
    ACTION_TO_UI_ID_MAP.put(BASIC_BREAK_BLOCK, "BasicBreakBlock");
    ACTION_TO_UI_ID_MAP.put(COMPLEX_BREAK_BLOCK, "ComplexBreakBlock");
    ACTION_TO_UI_ID_MAP.put(DROP_IN_CONTAINER, "DropInContainer");
    ACTION_TO_UI_ID_MAP.put(TAKE_OUTPUT_BENCH, "TakeOutputBench");
  }

  public static String getUiIdFor(AncientConstructAction action) {
    return ACTION_TO_UI_ID_MAP.get(action);
  }

  AncientConstructAction(byte id, String name, float executionTime) {
    this.id = id;
    this.executionTime = executionTime;
    this.name = name;
  }

  public byte getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public float getExecutionTime() {
    return this.executionTime;
  }

  public static AncientConstructAction fromByte(byte id) {
    return BY_ID.get(id);
  }

  public static int[] getExcavationSizeFor(AncientConstructAction action) {
    if (ACTION_TO_EXCAVATION_SIZE_MAP.containsKey(action)) {
      return ACTION_TO_EXCAVATION_SIZE_MAP.get(action);
    }
    return null;
  }

}
