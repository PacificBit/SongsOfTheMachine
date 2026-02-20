package com.pacificbytestudios.songsofthemachine.utils;

import java.util.ArrayDeque;

import org.bson.BsonArray;
import org.bson.BsonValue;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.math.vector.Vector3i;

public class Vector3iArrayDequeCodec implements Codec<ArrayDeque<Vector3i>> {

  @Override
  public Schema toSchema(SchemaContext context) {
    ArraySchema outerSchema = new ArraySchema();
    outerSchema.setTitle("Vector3iDeque");
    outerSchema.setItems(Vector3i.CODEC.toSchema(context));
    return outerSchema;
  }

  @Override
  public ArrayDeque<Vector3i> decode(BsonValue bsonValue, ExtraInfo info) {
    ArrayDeque<Vector3i> output = new ArrayDeque<>();
    if (bsonValue == null) {
      return output;
    }

    bsonValue.asArray().forEach(item -> {
      output.push(Vector3i.CODEC.decode(item, info));
    });

    return output;
  }

  @Override
  public BsonValue encode(ArrayDeque<Vector3i> deque, ExtraInfo info) {
    BsonArray dequeArray = new BsonArray();
    if (deque != null) {
      deque.forEach(item -> {
        dequeArray.add(Vector3i.CODEC.encode(item, info));
      });
    }
    return dequeArray;
  }
}
