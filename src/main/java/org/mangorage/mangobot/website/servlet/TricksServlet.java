package org.mangorage.mangobot.website.servlet;

import htmlflow.HtmlFlow;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import net.dv8tion.jda.api.JDA;
import org.mangorage.mangobot.modules.tricks.Trick;
import org.mangorage.mangobot.modules.tricks.TrickCommand;
import org.mangorage.mangobot.website.impl.AbstractServlet;
import org.mangorage.mangobot.website.impl.ObjectMap;
import org.xmlet.htmlapifaster.EnumRelType;

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

        var html = HtmlFlow
                .doc(response.getWriter())
                .html()
                .head()
                .link()
                .attrRel(
                        EnumRelType.STYLESHEET
                )
                .attrHref(
                        getStyles()
                )
                .__().__().body();

        if (guildId != null && trickId != null) {
            try {
                Trick trick = command.getTrick(trickId, Long.parseLong(guildId));
                if (trick != null) {
                    html.h2().text(
                            STR."Id: \{trick.getTrickID()}"
                    );

                    html.h2().text(
                            STR."Type: \{trick.getType()}"
                    );

                    html.h2().text(
                            STR."GuildId: \{trick.getGuildID()} \{getGuild(jda, trick.getGuildID())}"
                    );

                    switch (trick.getType()) {
                        case ALIAS -> {
                            html.h2().text(
                                    "Alias Target:"
                            );

                            html.h2().text(
                                    trick.getAliasTarget()
                            );
                            break;
                        }
                        case NORMAL -> {
                            html.h2().text(
                                    "Content:"
                            );

                            html
                                    .div()
                                    .textarea()
                                    .attrCols(50l)
                                    .attrRows(20L)
                                    .text(trick.getContent())
                                    .__();

                            break;
                        }
                        case SCRIPT -> {
                            html.h2().text(
                                    "Script:"
                            );

                            html.h2().text(
                                    trick.getScript()
                            );
                            break;
                        }
                    }


                    html.h4().text(
                            STR."Trick Owner: \{trick.getOwnerID()} \{getUser(jda, trick.getOwnerID())}"
                    );

                    html.h4().text(
                            STR."Last User to edit: \{trick.getLastUserEdited()} \{getUser(jda, trick.getLastUserEdited())}"
                    );

                    html.h4().text(
                            STR."Trick Creation Date: \{Date.from(Instant.ofEpochMilli(trick.getCreated()))}"
                    );

                    html.h4().text(
                            STR."Trick Last Edited: \{Date.from(Instant.ofEpochMilli(trick.getLastEdited()))}"
                    );

                    html.h4().text(
                            STR."Times Used: \{trick.getTimesUsed()}"
                    );

                } else {
                    html.h1().text(
                            "Invalid Trick %s Supplied for Guild %s".formatted(trickId, guildId)
                    );
                }
            } catch (Exception ignored) {}
        } else {
            html.h1().text(
                    "URL Format /trick?guildId=1234&trickId=example"
            );
        }
    }

    @Override
    public boolean useDefaultStyles() {
        return false;
    }
}