package org.mangorage.mangobot.website;



import jakarta.servlet.DispatcherType;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jetbrains.annotations.NotNull;
import org.mangorage.basicutils.LogHelper;
import org.mangorage.mangobot.website.impl.ObjectMap;
import org.mangorage.mangobot.website.servlet.FileServlet;
import org.mangorage.mangobot.website.servlet.FileUploadServlet;
import org.mangorage.mangobot.website.servlet.InfoServlet;
import org.mangorage.mangobot.website.servlet.TricksServlet;
import java.net.URL;
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

        // Create the ResourceHandler for file serving
        ResourceHandler fileResourceHandler = new ResourceHandler();
        fileResourceHandler.setResourceBase("webpage-root/webpage");

        // Create the ResourceHandler for resources in the JAR
        // Figure out what path to serve content from
        ClassLoader cl = WebServer.class.getClassLoader();
        // We look for a file, as ClassLoader.getResource() is not
        // designed to look for directories (we resolve the directory later)
        URL f = cl.getResource("webpage-data/");
        if (f == null)
        {
            throw new RuntimeException("Unable to find resource directory");
        }
        var webRootUri = f.toURI();

        ResourceHandler jarResourceHandler = new ResourceHandler();
        jarResourceHandler.setBaseResource(Resource.newResource(webRootUri));

        // Create the context and servlet handler
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("webpage-root/webpage");
        context.addServlet(DefaultServlet.class, "/*");

        // Add your servlets here as required
        context.addServlet(of(InfoServlet.class), "/info");
        context.addServlet(of(TricksServlet.class), "/trick");
        context.addServlet(of(FileUploadServlet.class, h -> {
            h.getRegistration().setMultipartConfig(new MultipartConfigElement("/tmp/uploads"));
        }), "/upload");
        context.addServlet(of(FileServlet.class), "/file");
        context.setAttribute("map", objectMap);
        // Add filters if needed
        context.addFilter(RequestInterceptorFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        // Create the connector
        ServerConnector connector = getServerConnector(server);
        server.addConnector(connector);

        // Combine the handlers (file and jar resource handlers)
        HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(jarResourceHandler);
        handlers.addHandler(fileResourceHandler);
        handlers.addHandler(context);

        server.setHandler(handlers);

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
