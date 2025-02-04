package org.mangorage.mangobot.website;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;


public final class WebServer {
    public static void startWebServer() throws Exception {
        Server server = new Server(30076);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("webpage"); // Serve files from "webpage" directory
        context.addServlet(DefaultServlet.class, "/*"); // Serve all webpage files

        server.setHandler(context);

        server.start();
        System.out.println("Server started with port 30076");
        server.join();
    }
}