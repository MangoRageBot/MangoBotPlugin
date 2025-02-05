package org.mangorage.mangobot.website.servlet;
import htmlflow.HtmlFlow;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebServlet;
import org.mangorage.mangobot.website.impl.AbstractServlet;
import org.mangorage.mangobotapi.core.plugin.PluginContainer;
import org.mangorage.mangobotapi.core.plugin.PluginManager;
import org.xmlet.htmlapifaster.EnumRelType;

import java.io.IOException;

@WebServlet
public class InfoServlet extends AbstractServlet {
    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        // This is where you handle the request and generate a response
        response.setContentType("text/html");
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