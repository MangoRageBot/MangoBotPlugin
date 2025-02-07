package org.mangorage.mangobot.website;



import jakarta.servlet.Servlet;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.mangorage.mangobot.website.impl.ObjectMap;
import org.mangorage.mangobot.website.servlet.InfoServlet;
import org.mangorage.mangobot.website.servlet.TestServlet;
import org.mangorage.mangobot.website.servlet.TricksServlet;

public final class WebServer {

    private static ServletHolder of(Class<? extends Servlet>  tClass) {
        return new ServletHolder(tClass);
    }

    public static void main(String[] args) throws Exception {
        startBasicWebServer();
    }

    public static void startBasicWebServer() throws Exception {
        Server server = new Server();
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("webpage"); // Serve files from "webpage" directory
        context.addServlet(DefaultServlet.class, "/*"); // Serve all webpage files
        server.setHandler(context);


        // HTTPS Connector
        ServerConnector httpsConnector = new ServerConnector(server);
        httpsConnector.setPort(30076);
        server.addConnector(httpsConnector);

        // Start the server
        server.start();
        server.join();
    }


    public static void startWebServer(ObjectMap objectMap) throws Exception {
        Server server = new Server();

        // Set up Servlet context
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("webpage"); // Serve files from "webpage" directory

        context.addServlet(DefaultServlet.class, "/*"); // Serve all webpage files
        context.addServlet(of(InfoServlet.class), "/info");
        context.addServlet(of(TricksServlet.class), "/trick");
        context.addServlet(of(TestServlet.class), "/test");

        context.setAttribute("map", objectMap);

        // SSL Context Factory for HTTPS
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setTrustAll(true);

        sslContextFactory.setKeyStorePath("keystore.jks"); // Path to your keystore
        sslContextFactory.setKeyStorePassword("mango12"); // Keystore password
        sslContextFactory.setKeyManagerPassword("mango12"); // Key manager password

        // HTTPS Connector
        ServerConnector sslConnector = new ServerConnector(
                server,
                sslContextFactory
        );

        sslConnector.setPort(443); // HTTPS port

        // Set the connector
        server.addConnector(sslConnector);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase("webpage");
        resourceHandler.setDirectoriesListed(true);


        HandlerList list = new HandlerList();
        list.addHandler(resourceHandler);
        list.addHandler(context);

        server.setHandler(list);

        // Start the server
        server.start();
        System.out.println("Webserver Started");
        server.join();
    }
}
