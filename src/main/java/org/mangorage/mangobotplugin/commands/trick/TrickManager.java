package org.mangorage.mangobotplugin.commands.trick;

import org.mangorage.mangobotcore.api.util.data.DataHandler;
import org.mangorage.mangobotplugin.entrypoint.MangoBot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TrickManager {
    public static final DataHandler<Trick> TRICKS_DATA_HANDLER = DataHandler.create()
            .path("data/tricksV2")
            .maxDepth(3)
            .build(Trick.class);

    private final Map<TrickKey, Trick> loadedTricks = new HashMap<>();

    public TrickManager(MangoBot plugin) {
        // Load tricks from data handler
        TRICKS_DATA_HANDLER.load(plugin.getPluginDirectory()).forEach(data -> {
            loadedTricks.put(new TrickKey(data.getTrickID(), data.getGuildID()), data);
        });
    }

    public List<Trick> getTricksForGuild(long guildId) {
        return loadedTricks.values().stream()
                .filter(trick -> trick.getGuildID() == guildId)
                .toList();
    }

    public Trick getTrickForGuildByName(long guildId, String name) {
        return loadedTricks.get(new TrickKey(name, guildId));
    }

}
