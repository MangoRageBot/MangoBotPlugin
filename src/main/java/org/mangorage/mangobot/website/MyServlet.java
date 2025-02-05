package org.mangorage.mangobot.website;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebServlet;
import org.mangorage.mangobotapi.core.plugin.PluginContainer;
import org.mangorage.mangobotapi.core.plugin.PluginManager;

import java.io.IOException;


public class MyServlet implements Servlet {

    @Override
    public void init(jakarta.servlet.ServletConfig servletConfig) throws ServletException {
        // Initialization logic, such as loading resources
        System.out.println("MyServlet initialized!");
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        // This is where you handle the request and generate a response
        response.setContentType("text/html");
        response.getWriter().write("<html><body><h1>Installed Plugins:</h1></body></html>");

        var str = "<html><body><h2>Id: %s Name: %s Version: %s Type: %s</h2></body></html>";
        for (PluginContainer container : PluginManager.getPluginContainers()) {
            var meta = container.getMetadata();
            response.getWriter().write(
                    str.formatted(
                            meta.id(),
                            meta.name(),
                            meta.version(),
                            container.getType()
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
        return "MyServlet";  // Optional servlet info
    }
}