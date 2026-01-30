package com.pacificbytestudios.songsofthemachine;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.pacificbytestudios.songsofthemachine.components.AncientConstuctComponent;
import com.pacificbytestudios.songsofthemachine.systems.AncientConstructLogicSystem;
import com.pacificbytestudios.songsofthemachine.systems.AncientConstructPlacementSystem;

public final class SongsOfTheMachine extends JavaPlugin {
  private static SongsOfTheMachine INSTANCE;
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private ComponentType<ChunkStore, AncientConstuctComponent> ancientConstructorComponentType;

  public SongsOfTheMachine(JavaPluginInit init) {
    super(init);
    LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    INSTANCE = this;
  }

  public static SongsOfTheMachine get() {
    return INSTANCE;
  }

  @Override
  protected void setup() {
    super.setup();

    this.ancientConstructorComponentType = this.getChunkStoreRegistry()
        .registerComponent(AncientConstuctComponent.class, "AncientConstuctComponent", AncientConstuctComponent.CODEC);
    System.out.println("[SongsOfTheMachine] Registered AncientConstuctComponent");

    this.getChunkStoreRegistry().registerSystem(new AncientConstructPlacementSystem());
    System.out.println("[SongsOfTheMachine] Registered placement system");
    this.getChunkStoreRegistry().registerSystem(new AncientConstructLogicSystem());
    System.out.println("[SongsOfTheMachine] Registered construct logic");
  }

  public ComponentType<ChunkStore, AncientConstuctComponent> getAncientConstructorComponentType() {
    return ancientConstructorComponentType;
  }

}
