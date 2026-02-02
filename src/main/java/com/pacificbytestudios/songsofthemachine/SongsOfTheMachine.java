package com.pacificbytestudios.songsofthemachine;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.pacificbytestudios.songsofthemachine.components.AncientConstuctComponent;
import com.pacificbytestudios.songsofthemachine.components.MusicToolComponent;
import com.pacificbytestudios.songsofthemachine.interactions.MusicToolChangeActionInteraction;
import com.pacificbytestudios.songsofthemachine.interactions.MusicToolUseInteraction;
import com.pacificbytestudios.songsofthemachine.systems.AncientConstructLogicSystem;
import com.pacificbytestudios.songsofthemachine.systems.AncientConstructPlacementSystem;
import com.pacificbytestudios.songsofthemachine.watcher.PlayerHotbarWatcher;

public final class SongsOfTheMachine extends JavaPlugin {
  private static SongsOfTheMachine INSTANCE;
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private ComponentType<ChunkStore, AncientConstuctComponent> ancientConstructorComponentType;
  private ComponentType<ChunkStore, MusicToolComponent> musicToolComponentType;
  private PacketFilter packetFilter;

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
    this.musicToolComponentType = this.getChunkStoreRegistry()
        .registerComponent(MusicToolComponent.class, "MusicToolComponent", MusicToolComponent.CODEC);
    System.out.println("[SongsOfTheMachine] Registered MusicToolComponent");

    this.getCodecRegistry(Interaction.CODEC)
        .register("MusicToolUseInteraction", MusicToolUseInteraction.class, MusicToolUseInteraction.CODEC)
        .register("MusicToolChangeAction", MusicToolChangeActionInteraction.class,
            MusicToolChangeActionInteraction.CODEC);
    System.out.println("[SongsOfTheMachine] Registered MusicToolUseInteraction & MusicToolChangeActionInteraction");

    this.getChunkStoreRegistry().registerSystem(new AncientConstructPlacementSystem());
    System.out.println("[SongsOfTheMachine] Registered placement system");
    this.getChunkStoreRegistry().registerSystem(new AncientConstructLogicSystem());
    System.out.println("[SongsOfTheMachine] Registered construct logic");

    this.packetFilter = PacketAdapters.registerInbound(new PlayerHotbarWatcher());
    System.out.println("[SongsOfTheMachine] Registered player hotbar watcher");
  }

  @Override
  protected void shutdown() {
    if (this.packetFilter != null) {
      PacketAdapters.deregisterInbound(this.packetFilter);
    }

  }

  public ComponentType<ChunkStore, AncientConstuctComponent> getAncientConstructorComponentType() {
    return this.ancientConstructorComponentType;
  }

  public ComponentType<ChunkStore, MusicToolComponent> getMusicToolComponentType() {
    return this.musicToolComponentType;
  }

}
