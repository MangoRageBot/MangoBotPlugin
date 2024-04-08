package org.mangorage.mangobot.modules.developer;

import org.mangorage.mangobotapi.core.plugin.api.CorePlugin;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServerAuthorizer {
    private final CorePlugin plugin;

    // Servers banned from using my bot!
    private final List<Long> serverIDs = List.of(
            1179527567926108160L
    );


    public ServerAuthorizer(CorePlugin plugin) {
        this.plugin = plugin;
        var timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Checking Guilds bot is in....");
                plugin.getJDA().getGuilds().forEach(g -> {

                    System.out.println(g.getIdLong());
                    if (serverIDs.contains(g.getIdLong())) {
                        System.out.println("Left Guild -> " + g.getIdLong());
                        g.leave().queue();
                    }
                });
            }
        }, 10000, 60_000 * 5);
    }
}
