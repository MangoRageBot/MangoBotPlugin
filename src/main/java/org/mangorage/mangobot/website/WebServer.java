package org.mangorage.mangobot.website;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;


public final class WebServer {
    public static void startWebServer() throws Exception {
        Server server = new Server(30076);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("webpage"); // Serve files from "webpage" directory
        context.addServlet(DefaultServlet.class, "/*"); // Serve all webpage files
        server.setHandler(context);


        // Configure SSL
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath("keystore.jks"); // Path to your keystore
        sslContextFactory.setKeyStorePassword("Mango12"); // Keystore password
        sslContextFactory.setKeyManagerPassword("Mango12"); // Key password

        // HTTPS Configuration
        HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        // HTTPS Connector
        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(httpsConfig));


        sslConnector.setPort(8443); // HTTPS port

        server.setConnectors(new Connector[]{sslConnector});


        server.start();
        System.out.println("Server started with port 30076");
        server.join();
    }

    public static void main(String[] args) throws Exception {
        startWebServer();
    }
}