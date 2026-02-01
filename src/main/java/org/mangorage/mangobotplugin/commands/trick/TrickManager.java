package org.mangorage.mangobotplugin.commands.trick;

import org.mangorage.mangobotcore.api.util.data.DataHandler;
import org.mangorage.mangobotcore.api.util.data.DatabaseHandler;
import org.mangorage.mangobotplugin.entrypoint.MangoBot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TrickManager {

    private static final DataHandler<Trick> TRICKS_DATA_HANDLER = DataHandler.create()
            .path("data/tricksV2")
            .maxDepth(3)
            .build(Trick.class);

    private static final DatabaseHandler<UUID, Trick> TRICKS_DATABASE_HANDLER = DatabaseHandler.create(
            MangoBot.BOT_DATABASE_URL.get(),
            MangoBot.BOT_DATABASE_USERNAME.get(),
            MangoBot.BOT_DATABASE_PASSWORD.get(),
            Trick.class
    );

    private final Map<TrickKey, Trick> loadedTricks = new HashMap<>();
    private final MangoBot plugin;
    private final boolean useDatabase;

    public TrickManager(MangoBot plugin) {
        this.plugin = plugin;
        this.useDatabase = MangoBot.BOT_USE_DATABASE.get();

        if (useDatabase) {
            migrateFilesToDatabase();
            loadFromDatabase();
        } else {
            loadFromFiles();
        }
    }

    private void migrateFilesToDatabase() {
        List<Trick> tricks = TRICKS_DATA_HANDLER.load(plugin.getPluginDirectory());
        TRICKS_DATABASE_HANDLER.migrateToDatabase(tricks);
    }

    private void loadFromFiles() {
        TRICKS_DATA_HANDLER.load(plugin.getPluginDirectory()).forEach(data -> {
            loadedTricks.put(new TrickKey(data.getTrickID(), data.getGuildID()), data);
        });
    }

    private void loadFromDatabase() {
        for (Trick trick : TRICKS_DATABASE_HANDLER.loadEntitiesFromDatabase()) {
            loadedTricks.put(new TrickKey(trick.getTrickID(), trick.getGuildID()), trick);
        }
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

    public void saveTrick(Trick trick) {
        if (useDatabase) {
            TRICKS_DATABASE_HANDLER.saveEntity(trick);
        } else {
            TRICKS_DATA_HANDLER.save(plugin.getPluginDirectory(), trick);
        }
    }

    public boolean removeTrick(String trickID, long guildID) {
        final var removed = loadedTricks.remove(new TrickKey(trickID, guildID));

        if (removed == null) return false;

        if (useDatabase) {
            TRICKS_DATABASE_HANDLER.removeEntity(removed);
        } else {
            TRICKS_DATA_HANDLER.delete(plugin.getPluginDirectory(), removed);
        }

        return true;
    }

    public void addTrick(Trick trick) {
        loadedTricks.put(new TrickKey(trick.getTrickID(), trick.getGuildID()), trick);
        saveTrick(trick);
    }
}


