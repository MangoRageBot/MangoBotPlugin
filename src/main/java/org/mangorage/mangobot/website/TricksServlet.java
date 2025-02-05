package org.mangorage.mangobot.website;

import dev.lavalink.youtube.clients.Web;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.mangorage.mangobot.modules.tricks.Trick;
import org.mangorage.mangobot.modules.tricks.TrickCommand;
import org.mangorage.mangobotapi.core.plugin.PluginContainer;
import org.mangorage.mangobotapi.core.plugin.PluginManager;

import java.io.IOException;

public class TricksServlet implements Servlet {


    @Override
    public void init(jakarta.servlet.ServletConfig servletConfig) throws ServletException {
        // Initialization logic, such as loading resources
        System.out.println("TricksServlet initialized!");
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        // This is where you handle the request and generate a response
        var command = WebServer.getTrickCommand();
        response.setContentType("text/html");
        var guildId = request.getParameter("guildId");
        var trickId = request.getParameter("trickId");


        var str1 = "<html><body><h1>Id: %s Type: %s</h1></body></html>";
        var str2 = "<html><body><h2>Content:</h2></body></html>";
        var str3 = "<html><body><h2>%s</h2></body></html>";

        if (guildId != null && trickId != null) {
            try {
                Trick trick = command.getTrick(trickId, Long.parseLong(guildId));
                if (trick != null) {

                    response.getWriter().write(
                            str1.formatted(
                                    trick.getTrickID(),
                                    trick.getType()
                            )
                    );
                    response.getWriter().write(str2);
                    response.getWriter().write(
                            str3.formatted(
                                    trick.getContent()
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

    @Override
    public void destroy() {
        // Cleanup logic, such as releasing resources
        System.out.println("MyServlet destroyed!");
    }

    @Override
    public jakarta.servlet.ServletConfig getServletConfig() {
        return null;  // Return any relevant config if needed
    }

    @Override
    public String getServletInfo() {
        return "TickServlet";  // Optional servlet info
    }
}