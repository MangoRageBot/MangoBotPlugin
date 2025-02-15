package org.mangorage.mangobot.website.servlet;

import htmlflow.HtmlFlow;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.dv8tion.jda.api.JDA;
import org.mangorage.mangobot.modules.tricks.Trick;
import org.mangorage.mangobot.modules.tricks.TrickCommand;
import org.mangorage.mangobot.website.impl.ObjectMap;
import org.mangorage.mangobot.website.impl.StandardHttpServlet;
import org.mangorage.mangobot.website.util.WebConstants;
import org.xmlet.htmlapifaster.EnumMethodType;
import org.xmlet.htmlapifaster.EnumRelType;
import org.xmlet.htmlapifaster.EnumTypeButtonType;
import org.xmlet.htmlapifaster.EnumTypeInputType;
import org.xmlet.htmlapifaster.EnumWrapType;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

public class TricksServlet extends StandardHttpServlet {

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

    private static long getLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (Exception ignored) {
            return -1;
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // This is where you handle the request and generate a response
        var map = (ObjectMap) getServletConfig().getServletContext().getAttribute(WebConstants.WEB_OBJECT_ID);
        var command = map.get("trickCommand", TrickCommand.class);
        var jda = map.get("jda", JDA.class);

        resp.setContentType("text/html");
        var guildId = req.getParameter("guildId");
        var trickId = req.getParameter("trickId");

        var html = HtmlFlow
                .doc(resp.getWriter())
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

        if (guildId == null && trickId == null) {
            // When the Button Clicked makes the URL /trick?guildId=value
            var form = html.h2()
                    .text("SELECT GUILD")
                    .__()
                    .form()
                    .select()
                    .attrName("guildId");

            for (Long guild : command.getGuilds()) {
                form = form.option()
                        .attrValue(guild.toString())
                        .text(getGuild(jda, guild)).__();
            }

            form.__()
                    .button()
                    .attrType(EnumTypeButtonType.SUBMIT) // Ensure it's a submit button
                    .text("Enter!");

        } else if (guildId != null && trickId == null) {
            // When the Button Clicked makes the URL /trick?guildId=value&trickId=test
            var form = html.h2()
                    .text("SELECT TRICK")
                    .__()
                    .form()  // Single form starts here
                    .attrMethod(EnumMethodType.GET)  // Use GET to add parameters to the URL
                    .attrAction("/trick"); // Ensure submission to /trick

            // Preserve guildId as a hidden input
            form.input()
                    .attrType(EnumTypeInputType.HIDDEN)
                    .attrName("guildId")
                    .attrValue(guildId)
                    .__();

            // Create trick selection dropdown
            var select = form.select().attrName("trickId");

            for (Trick trick : command.getTricksForGuild(getLong(guildId))) {
                select.option()
                        .attrValue(trick.getTrickID())
                        .text(trick.getTrickID()).__();
            }

            // Close select and add submit button
            select.__()
                    .button()
                    .attrType(EnumTypeButtonType.SUBMIT) // Ensure it's a submit button
                    .text("Enter!");

        } else if (guildId != null && trickId != null) {
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
                            html.h2()
                                    .text("Alias Target:")
                                    .__()
                                    .h2()
                                    .text(trick.getAliasTarget());
                            break;
                        }
                        case NORMAL -> {
                            html.h2().text(
                                            "Content:"
                                    ).__()
                                    .div()
                                    .textarea()
                                    .attrCols(50L)
                                    .attrRows(20L)
                                    .attrWrap(EnumWrapType.HARD)
                                    .attrReadonly(true)
                                    .text(trick.getContent())
                                    .__().__();

                            break;
                        }
                        case SCRIPT -> {
                            html.h2().text(
                                            "Script:"
                                    ).__()
                                    .div()
                                    .textarea()
                                    .attrCols(50L)
                                    .attrRows(20L)
                                    .attrWrap(EnumWrapType.HARD)
                                    .attrReadonly(true)
                                    .text(trick.getScript())
                                    .__().__();
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

                    html.h4().text(
                            STR."Locked: \{trick.isLocked()}"
                    );

                    html.h4().text(
                            STR."Embeds Supressed: \{trick.isSuppressed()}"
                    );


                } else {
                    html.h1().text(
                            "Invalid Trick %s Supplied for Guild %s".formatted(trickId, guildId)
                    );
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean useDefaultStyles() {
        return false;
    }
}