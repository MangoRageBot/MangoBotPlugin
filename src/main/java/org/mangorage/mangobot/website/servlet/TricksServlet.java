package org.mangorage.mangobot.website.servlet;

import htmlflow.HtmlFlow;
import jakarta.servlet.ServletException;
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
        return user != null ? user.getName() : "";
    }

    public static String getGuild(JDA jda, long id) {
        var guild = jda.getGuildById(id);
        return guild != null ? guild.getName() : "";
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
        // Retrieve shared objects from the servlet context
        var map = (ObjectMap) getServletConfig().getServletContext().getAttribute(WebConstants.WEB_OBJECT_ID);
        var command = map.get("trickCommand", TrickCommand.class);
        var jda = map.get("jda", JDA.class);

        resp.setContentType("text/html");
        var guildId = req.getParameter("guildId");
        var trickId = req.getParameter("trickId");

        // Build HTML document with semantic structure and CSS classes for improved styling
        var html = HtmlFlow
                .doc(resp.getWriter())
                .html()
                .head()
                .meta().attrCharset("UTF-8").__()
                .title().text("Mangobot Tricks").__()
                .link().attrRel(EnumRelType.STYLESHEET).attrHref(getStyles()).__()
                .__() // end head
                .body().attrClass("page-body")
                .div().attrClass("container");

        if (guildId == null && trickId == null) {
            // Guild selection page
            var a =html.h2().attrClass("title").text("Select Guild").__()
                    .form().attrMethod(EnumMethodType.GET).attrAction("/trick").attrClass("form-group")
                    .label().attrFor("guildId").text("Choose a Guild:").__()
                    .select().attrName("guildId").attrId("guildId").attrClass("select-input");
            for (Long guild : command.getGuilds()) {
                a.option().attrValue(guild.toString()).text(getGuild(jda, guild)).__();
            }
            a.__() // close select
                    .button().attrType(EnumTypeButtonType.SUBMIT).attrClass("btn btn-primary").text("Enter!").__()
                    .__(); // close form
        } else if (guildId != null && trickId == null) {
            // Trick selection page
            var a = html.h2().attrClass("title").text("Select Trick").__()
                    .form().attrMethod(EnumMethodType.GET).attrAction("/trick").attrClass("form-group")
                    .input().attrType(EnumTypeInputType.HIDDEN).attrName("guildId").attrValue(guildId).__()
                    .label().attrFor("trickId").text("Choose a Trick:").__()
                    .select().attrName("trickId").attrId("trickId").attrClass("select-input");
            for (Trick trick : command.getTricksForGuild(getLong(guildId))) {
                a.option().attrValue(trick.getTrickID()).text(trick.getTrickID()).__();
            }
            a.__() // close select
                    .button().attrType(EnumTypeButtonType.SUBMIT).attrClass("btn btn-primary").text("Enter!").__()
                    .__(); // close form
        } else if (guildId != null && trickId != null) {
            // Trick details page
            try {
                Trick trick = command.getTrick(trickId, Long.parseLong(guildId));
                if (trick != null) {
                    html.h2().attrClass("trick-title").text("Trick Details").__();
                    html.div().attrClass("trick-info")
                            .p().attrClass("trick-id").text("ID: " + trick.getTrickID()).__()
                            .p().attrClass("trick-type").text("Type: " + trick.getType()).__()
                            .p().attrClass("trick-guild").text("Guild: " + trick.getGuildID() + " " + getGuild(jda, trick.getGuildID())).__()
                            .__(); // end trick-info

                    switch (trick.getType()) {
                        case ALIAS -> {
                            html.h3().attrClass("section-title").text("Alias Target").__();
                            html.div().attrClass("trick-alias").text(trick.getAliasTarget()).__();
                        }
                        case NORMAL -> {
                            html.h3().attrClass("section-title").text("Content").__();
                            html.div().attrClass("trick-content")
                                    .textarea().attrCols(50L).attrRows(20L).attrWrap(EnumWrapType.HARD)
                                    .attrReadonly(true).text(trick.getContent()).__()
                                    .__();
                        }
                        case SCRIPT -> {
                            html.h3().attrClass("section-title").text("Script").__();
                            html.div().attrClass("trick-script")
                                    .textarea().attrCols(50L).attrRows(20L).attrWrap(EnumWrapType.HARD)
                                    .attrReadonly(true).text(trick.getScript()).__()
                                    .__();
                        }
                    }

                    html.div().attrClass("trick-meta")
                            .p().attrClass("meta-item").text("Trick Owner: " + trick.getOwnerID() + " " + getUser(jda, trick.getOwnerID())).__()
                            .p().attrClass("meta-item").text("Last Edited By: " + trick.getLastUserEdited() + " " + getUser(jda, trick.getLastUserEdited())).__()
                            .p().attrClass("meta-item").text("Created: " + Date.from(Instant.ofEpochMilli(trick.getCreated()))).__()
                            .p().attrClass("meta-item").text("Last Edited: " + Date.from(Instant.ofEpochMilli(trick.getLastEdited()))).__()
                            .p().attrClass("meta-item").text("Times Used: " + trick.getTimesUsed()).__()
                            .p().attrClass("meta-item").text("Locked: " + trick.isLocked()).__()
                            .p().attrClass("meta-item").text("Embeds Suppressed: " + trick.isSuppressed()).__()
                            .__(); // end trick-meta
                } else {
                    html.h1().attrClass("error").text("Invalid Trick " + trickId + " supplied for Guild " + guildId).__();
                }
            } catch (Exception ignored) {
                html.h1().attrClass("error").text("An error occurred while processing your request.").__();
            }
        }

        // Close container, body, and html tags
        html.__() // close container
                .__() // close body
                .__(); // close html
    }

    @Override
    public boolean useDefaultStyles() {
        return false;
    }
}
