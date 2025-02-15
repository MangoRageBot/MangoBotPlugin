package org.mangorage.mangobot.website.servlet;
import htmlflow.HtmlFlow;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mangorage.mangobot.website.impl.StandardHttpServlet;
import org.mangorage.mangobotapi.core.plugin.PluginContainer;
import org.mangorage.mangobotapi.core.plugin.PluginManager;
import org.xmlet.htmlapifaster.EnumRelType;

import java.io.IOException;

@WebServlet
public class InfoServlet extends StandardHttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // This is where you handle the request and generate a response
        resp.setContentType("text/html");
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
                ).__().__()
                .body()
                .h1()
                .text("Installed Plugins:")
                .__();

        for (PluginContainer container : PluginManager.getPluginContainers()) {
            var meta = container.getMetadata();
            html
                    .h2()
                    .text("Id: %s Name: %s Type: %s".formatted(meta.id(), meta.name(), meta.version(), container.getType()))
                    .__();
        }
    }

    @Override
    public boolean useDefaultStyles() {
        return false;
    }
}