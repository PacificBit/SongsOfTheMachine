package com.pacificbytestudios.songsofthemachine.customcodec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructAction;

public class ActionSelection {

  public static final BuilderCodec<ActionSelection> CODEC = BuilderCodec
      .builder(ActionSelection.class, ActionSelection::new)
      .append(new KeyedCodec<>("Move Forward", Codec.BOOLEAN),
          (obj, value) -> obj.action = AncientConstructAction.MOVE_FORWARD,
          (obj) -> true)
      .add()
      .append(new KeyedCodec<>("Turn Left", Codec.BOOLEAN),
          (obj, value) -> obj.action = AncientConstructAction.TURN_LEFT,
          (obj) -> true)
      .add()
      .append(new KeyedCodec<>("Turn Right", Codec.BOOLEAN),
          (obj, value) -> obj.action = AncientConstructAction.TURN_RIGHT,
          (obj) -> true)
      .add()
      .append(new KeyedCodec<>("Break Block", Codec.BOOLEAN),
          (obj, value) -> obj.action = AncientConstructAction.BREAK_BLOCK,
          (obj) -> true)
      .add()
      .build();

  private AncientConstructAction action;

  public AncientConstructAction getAction() {
    return action;
  }

}
