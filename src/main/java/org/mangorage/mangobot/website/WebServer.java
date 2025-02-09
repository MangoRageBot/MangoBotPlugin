package org.mangorage.mangobot.website;



import jakarta.servlet.DispatcherType;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jetbrains.annotations.NotNull;
import org.mangorage.basicutils.LogHelper;
import org.mangorage.mangobot.website.impl.ObjectMap;
import org.mangorage.mangobot.website.servlet.FileServlet;
import org.mangorage.mangobot.website.servlet.FileUploadServlet;
import org.mangorage.mangobot.website.servlet.InfoServlet;
import org.mangorage.mangobot.website.servlet.TricksServlet;
import java.util.EnumSet;
import java.util.function.Consumer;

public final class WebServer {

    private static ServletHolder of(Class<? extends Servlet> tClass) {
        return new ServletHolder(tClass);
    }

    private static ServletHolder of(Class<? extends Servlet> tClass, Consumer<ServletHolder> holderConsumer) {
        var holder = of(tClass);
        holderConsumer.accept(holder);
        return holder;
    }

    public static void startWebServerSafely(ObjectMap objectMap) {
        new Thread(() -> {
            try {
                FolderPruner.init();
                startWebServer(objectMap);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void startWebServer(ObjectMap objectMap) throws Exception {
        Server server = new Server();


        // Use RequestLogHandler to handle logging
        RequestLogHandler logHandler = new RequestLogHandler();
        logHandler.setRequestLog((request, response) -> LogHelper.info("""
                Attempted URL: %s
                """.formatted(request.getRequestURL())));

        // Set up Servlet context
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("webpage-root/webpage"); // Serve files from "webpage" directory

        context.addServlet(DefaultServlet.class, "/*"); // Serve all webpage files
        context.addServlet(of(InfoServlet.class), "/info");
        context.addServlet(of(TricksServlet.class), "/trick");
        context.addServlet(of(FileUploadServlet.class, h -> {
            h.getRegistration().setMultipartConfig(new MultipartConfigElement("/tmp/uploads"));
        }), "/upload");
        context.addServlet(of(FileServlet.class), "/file");
        context.setAttribute("map", objectMap);

        context.addFilter(RequestInterceptorFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));


        ServerConnector connector = getServerConnector(server);
        server.addConnector(connector);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase("webpage-root/webpage");
        resourceHandler.setDirectoriesListed(true);


        HandlerList list = new HandlerList();
        list.addHandler(resourceHandler);
        list.addHandler(context);

        server.setHandler(list);

        // Start the server
        server.start();
        LogHelper.info("Webserver Started");
        server.join();
    }

    private static @NotNull ServerConnector getServerConnector(Server server) {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setTrustAll(true);

        sslContextFactory.setKeyStorePath("webpage-root/keystore.jks"); // Path to your keystore
        sslContextFactory.setKeyStorePassword("mango12"); // Keystore password
        sslContextFactory.setKeyManagerPassword("mango12"); // Key manager password

        // HTTPS Connector
        ServerConnector sslConnector = new ServerConnector(
                server,
                sslContextFactory
        );

        sslConnector.setPort(443); // HTTPS port
        return sslConnector;
    }
}
