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
    private final MangoBot plugin;

    public TrickManager(MangoBot plugin) {
        this.plugin = plugin;

        // Load tricks from data handler
        TRICKS_DATA_HANDLER.load(plugin.getPluginDirectory()).forEach(data -> {
            loadedTricks.put(new TrickKey(data.getTrickID(), data.getGuildID()), data);
        });
    }

    public List<Long> getAllGuilds() {
        return loadedTricks.values().stream()
                .map(Trick::getGuildID)
                .distinct()
                .toList();
    }

    public List<Trick> getTricksForGuild(long guildId) {
        return loadedTricks.values().stream()
                .filter(trick -> trick.getGuildID() == guildId)
                .toList();
    }

    public Trick getTrickForGuildByName(long guildId, String name) {
        return loadedTricks.get(new TrickKey(name, guildId));
    }

    public boolean removeTrick(String trickID, long guildID) {
        TrickKey key = new TrickKey(trickID, guildID);
        Trick removed = loadedTricks.remove(key);
        if (removed != null) {
            TRICKS_DATA_HANDLER.delete(plugin.getPluginDirectory(), removed);
            return true;
        }
        return false;
    }

}
