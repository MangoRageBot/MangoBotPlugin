package org.mangorage.mangobot.website;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import net.dv8tion.jda.api.JDA;
import org.mangorage.mangobot.modules.tricks.Trick;
import org.mangorage.mangobot.modules.tricks.TrickCommand;
import org.mangorage.mangobot.website.impl.AbstractServlet;
import org.mangorage.mangobot.website.impl.ObjectMap;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

public class TricksServlet extends AbstractServlet {


    public static String getUser(JDA jda, long id) {
        var user = jda.getUserById(id);
        if (user != null) {
            return user.getName();
        }
        return "";
    }

    public static String getGuild(JDA jda, long id) {
        var guild = jda.getGuildById(id);
        if (guild != null) {
            return guild.getName();
        }
        return "";
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        // This is where you handle the request and generate a response
        var map = (ObjectMap) getServletConfig().getServletContext().getAttribute("map");
        var command = map.get("trickCommand", TrickCommand.class);
        var jda = map.get("jda", JDA.class);

        response.setContentType("text/html");
        var guildId = request.getParameter("guildId");
        var trickId = request.getParameter("trickId");

        var str3 = "<html><body><h2>%s</h2></body></html>";
        var str4 = "<html><body><h4>%s</h4></body></html>";

        if (guildId != null && trickId != null) {
            try {
                Trick trick = command.getTrick(trickId, Long.parseLong(guildId));
                if (trick != null) {

                    response.getWriter().write(
                            str3.formatted(
                                    STR."Id: \{trick.getTrickID()}"
                            )
                    );

                    response.getWriter().write(
                            str3.formatted(
                                    STR."Type: \{trick.getType()}"
                            )
                    );

                    response.getWriter().write(
                            str3.formatted(
                                    STR."GuildId: \{trick.getGuildID()} \{getGuild(jda, trick.getGuildID())}"
                            )
                    );

                    switch (trick.getType()) {
                        case ALIAS -> {
                            response.getWriter().write(
                                    str3.formatted(
                                            "Alias Target:"
                                    )
                            );
                            response.getWriter().write(
                                    str3.formatted(
                                            trick.getAliasTarget()
                                    )
                            );
                            break;
                        }
                        case NORMAL -> {
                            response.getWriter().write(
                                    str3.formatted(
                                            "Content:"
                                    )
                            );
                            response.getWriter().write(
                                    str3.formatted(
                                            trick.getContent()
                                    )
                            );
                            break;
                        }
                        case SCRIPT -> {
                            response.getWriter().write(
                                    str3.formatted(
                                           "Script:"
                                    )
                            );
                            response.getWriter().write(
                                    str3.formatted(
                                            trick.getScript()
                                    )
                            );
                            break;
                        }
                    }

                    response.getWriter().write(
                            str4.formatted(
                                    STR."Trick Owner: \{trick.getOwnerID()} \{getUser(jda, trick.getOwnerID())}"
                            )
                    );

                    response.getWriter().write(
                            str4.formatted(
                                    STR."Last User to edit: \{trick.getLastUserEdited()} \{getUser(jda, trick.getLastUserEdited())}"
                            )
                    );

                    response.getWriter().write(
                            str4.formatted(
                                    STR."Trick Creation Date: \{Date.from(Instant.ofEpochMilli(trick.getCreated()))}"
                            )
                    );

                    response.getWriter().write(
                            str4.formatted(
                                    STR."Trick Last Edited: \{Date.from(Instant.ofEpochMilli(trick.getLastEdited()))}"
                            )
                    );

                    response.getWriter().write(
                            str4.formatted(
                                    STR."Times Used: \{trick.getTimesUsed()}"
                            )
                    );

                } else {
                    response.getWriter().write(
                            str3.formatted("Invalid Trick %s Supplied for Guild %s".formatted(trickId, guildId))
                    );
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        } else {
            response.getWriter().write(
                    str3.formatted(
                            "URL Format /trick?guildId=1234&trickId=example"
                    )
            );
        }
    }
}