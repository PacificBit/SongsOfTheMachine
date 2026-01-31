package com.pacificbytestudios.songsofthemachine.customcodec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.pacificbytestudios.songsofthemachine.enums.AncientConstructActions;

public class ActionSelection {

  public static final BuilderCodec<ActionSelection> CODEC = BuilderCodec
      .builder(ActionSelection.class, ActionSelection::new)
      .append(new KeyedCodec<>("Move Forward", Codec.BOOLEAN),
          (obj, value) -> obj.action = AncientConstructActions.MOVE_FORWARD,
          (obj) -> true)
      .add()
      .append(new KeyedCodec<>("Turn Left", Codec.BOOLEAN),
          (obj, value) -> obj.action = AncientConstructActions.TURN_LEFT,
          (obj) -> true)
      .add()
      .append(new KeyedCodec<>("Turn Right", Codec.BOOLEAN),
          (obj, value) -> obj.action = AncientConstructActions.TURN_RIGHT,
          (obj) -> true)
      .add()
      .append(new KeyedCodec<>("Break Block", Codec.BOOLEAN),
          (obj, value) -> obj.action = AncientConstructActions.BREAK_BLOCK,
          (obj) -> true)
      .add()
      .build();

  private AncientConstructActions action;

  public AncientConstructActions getAction() {
    return action;
  }

}
