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
        // Set content type for HTML response
        resp.setContentType("text/html");

        // Start HTML generation with HtmlFlow
        var html = HtmlFlow
                .doc(resp.getWriter())
                .html()
                .head()
                .link()
                .attrRel(EnumRelType.STYLESHEET)  // Link to your CSS file
                .attrHref(getStyles())            // Get the path of your CSS
                .__()
                .__()// Close head
                .body()
                .div().attrClass("container")     // Use the container class for the main section
                .h1().attrClass("page-title")     // Title with special class for styling
                .text("Installed Plugins:")
                .__();                           // End of h1

        // Loop through each plugin container to display plugin details
        for (PluginContainer container : PluginManager.getPluginContainers()) {
            var meta = container.getMetadata();

            html
                    .div().attrClass("plugin-item")  // Each plugin gets a styled box
                    .h2()
                    .text("Plugin Details:")
                    .__()  // End of h2
                    .div().attrClass("plugin-details")  // Details section for plugin
                    .p().text("Id: ").span().attrClass("plugin-id").text(meta.id()).__()   // Plugin Id
                    .__()  // End of Id
                    .p().text("Name: ").span().attrClass("plugin-name").text(meta.name()).__()   // Plugin Name
                    .__()  // End of Name
                    .p().text("Type: ").span().attrClass("plugin-type").text(container.getType()).__()   // Plugin Type
                    .__()  // End of Type
                    .p().text("Version: ").span().attrClass("plugin-version").text(meta.version()).__()   // Plugin Version
                    .__()  // End of Version
                    .__()
                    .__();  // End of plugin-item div
        }

        // End body and HTML
        html.__().__();
    }

    @Override
    public boolean hasEmbed() {
        return true;
    }

    @Override
    public boolean useDefaultStyles() {
        return false;
    }
}
