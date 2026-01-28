package org.mangorage.mangobotplugin.commands.trick;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
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
    private final SessionFactory sessionFactory;
    private final boolean useDatabase;

    public TrickManager(MangoBot plugin) {
        this.plugin = plugin;

        String url = MangoBot.BOT_DATABASE_URL.get();
        String user = MangoBot.BOT_DATABASE_USERNAME.get();
        String pass = MangoBot.BOT_DATABASE_PASSWORD.get();

        this.useDatabase = MangoBot.BOT_USE_DATABASE.get();

        if (useDatabase) {
            this.sessionFactory = new Configuration()
                    .setProperty("hibernate.connection.url", url)
                    .setProperty("hibernate.connection.username", user)
                    .setProperty("hibernate.connection.password", pass)

                    .setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
                    .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")

                    .setProperty("hibernate.hbm2ddl.auto", "update")
                    .setProperty("hibernate.show_sql", "false")

                    .addAnnotatedClass(Trick.class)
                    .buildSessionFactory();

            migrateFilesToDatabase();

            loadFromDatabase();
        } else {
            this.sessionFactory = null;
            loadFromFiles();
        }
    }

    private void migrateFilesToDatabase() {
        List<Trick> fileTricks = TRICKS_DATA_HANDLER.load(plugin.getPluginDirectory());

        if (fileTricks.isEmpty()) return;

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for (Trick trick : fileTricks) {
                session.merge(trick);
                TRICKS_DATA_HANDLER.delete(plugin.getPluginDirectory(), trick);
            }

            tx.commit();
        }
    }

    private void loadFromFiles() {
        TRICKS_DATA_HANDLER.load(plugin.getPluginDirectory()).forEach(data -> {
            loadedTricks.put(new TrickKey(data.getTrickID(), data.getGuildID()), data);
        });
    }

    private void loadFromDatabase() {
        try (Session session = sessionFactory.openSession()) {
            List<Trick> tricks = session.createQuery("from Trick", Trick.class).list();
            for (Trick trick : tricks) {
                loadedTricks.put(new TrickKey(trick.getTrickID(), trick.getGuildID()), trick);
            }
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
            try (Session session = sessionFactory.openSession()) {
                Transaction tx = session.beginTransaction();
                session.merge(trick);
                tx.commit();
            }
        } else {
            TRICKS_DATA_HANDLER.save(plugin.getPluginDirectory(), trick);
        }
    }

    public boolean removeTrick(String trickID, long guildID) {
        final var removed = loadedTricks.remove(new TrickKey(trickID, guildID));

        if (removed == null) return false;

        if (useDatabase) {
            try (Session session = sessionFactory.openSession()) {
                final var transaction = session.beginTransaction();
                session
                        .createMutationQuery(
                                "delete from Trick t where t.trickID = :trickId and t.guildID = :guildId"
                        )
                        .setParameter("trickId", trickID)
                        .setParameter("guildId", guildID)
                        .executeUpdate();
                transaction.commit();
            }
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


