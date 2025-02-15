package org.mangorage.mangobot.website;



import jakarta.servlet.DispatcherType;
import jakarta.servlet.MultipartConfigElement;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jetbrains.annotations.NotNull;
import org.mangorage.basicutils.LogHelper;
import org.mangorage.mangobot.website.filters.RequestInterceptorFilter;
import org.mangorage.mangobot.website.handlers.DefaultErrorHandler;
import org.mangorage.mangobot.website.impl.ObjectMap;
import org.mangorage.mangobot.website.impl.StandardHttpServlet;
import org.mangorage.mangobot.website.servlet.AccountServlet;
import org.mangorage.mangobot.website.servlet.FileServlet;
import org.mangorage.mangobot.website.servlet.FileUploadServlet;
import org.mangorage.mangobot.website.servlet.InfoServlet;
import org.mangorage.mangobot.website.servlet.LoginServlet;
import org.mangorage.mangobot.website.servlet.TestAuthServlet;
import org.mangorage.mangobot.website.servlet.TricksServlet;
import org.mangorage.mangobot.website.util.ResolveString;
import org.mangorage.mangobot.website.util.ServletContextHandlerBuilder;
import org.mangorage.mangobot.website.util.WebConstants;

import java.util.EnumSet;
import java.util.Set;

public final class WebServer {
    public static final ResolveString WEBPAGE_INTERNAL = new ResolveString("webpage-internal");
    public static final ResolveString WEBPAGE_ROOT = new ResolveString("webpage-root");
    public static final ResolveString WEBPAGE_PAGE = WEBPAGE_ROOT.resolve("webpage");


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

    public static void main(String[] args) {
        startWebServerSafely(new ObjectMap());
    }

    public static void startWebServer(ObjectMap objectMap) throws Exception {
        Server server = new Server();

        var builder = configureBuilders(objectMap);
        var contextHandler = builder.getServletContextHandler();
        var securityHandler = builder.getConstraintSecurityHandler();

        // Combine the handlers (file, jar resource handlers, and security)
        HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(configureInternalResourceHandler());
        handlers.addHandler(configureExternalResourceHandler());
        handlers.addHandler(contextHandler);

        securityHandler.setHandler(handlers);
        server.setHandler(securityHandler);


        ServerConnector connector = getServerConnector(server);
        server.addConnector(connector);

        objectMap.put(WebConstants.LOGIN_SERVICE, securityHandler);
        objectMap.put("auth", securityHandler.getAuthenticator());

        server.start();
        LogHelper.info("Webserver Started");
        server.join();
    }

    private static @NotNull ResourceHandler configureInternalResourceHandler() {
        // Create the ResourceHandler for resources in the JAR
        var file = WebServer.class.getClassLoader().getResource(WEBPAGE_INTERNAL.value());
        if (file == null) throw new RuntimeException("Unable to find resource directory");

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setBaseResource(Resource.newResource(file));

        return resourceHandler;
    }

    private static @NotNull ResourceHandler configureExternalResourceHandler() {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(WEBPAGE_PAGE.value());

        return resourceHandler;
    }


    private static @NotNull ServletContextHandlerBuilder configureBuilders(ObjectMap objectMap) {
        var builder = ServletContextHandlerBuilder.create(new ServletContextHandler(ServletContextHandler.SESSIONS));

        builder
                .setContextPath("/")
                .setResourceBase(WEBPAGE_PAGE.value())
                .dynamic(h -> {
                    h.setErrorHandler(new DefaultErrorHandler());
                })
                .addServlet(DefaultServlet.class, "/*")
                .addHttpServlet(InfoServlet.class, "/info")
                .addHttpServlet(TricksServlet.class, "/trick")
                .addHttpServlet(FileServlet.class, "/file")
                .addHttpServlet(TestAuthServlet.class, "/testAuth")
                .addHttpServlet(LoginServlet.class, "/login")
                .addHttpServlet(AccountServlet.class, "/account")
                .addHttpServlet(FileUploadServlet.class, "/upload", h -> {
                    h.getRegistration().setMultipartConfig(
                            new MultipartConfigElement("/tmp/uploads")
                    );
                })
                .setAttribute(WebConstants.WEB_OBJECT_ID, objectMap)
                .addFilter(RequestInterceptorFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST))
                .configureLoginBuilder(security -> {
                    security
                            .setFullValidate(true)
                            .setAuthenticator(new BasicAuthenticator())
                            .addUser("admin", "pass", Set.of("admin"))
                            .lock(
                                Set.of("admin"),
                                "/testAuth"
                            );
                });

        return builder;
    }

    private static @NotNull ServerConnector getServerConnector(Server server) {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setTrustAll(true);

        sslContextFactory.setKeyStorePath(WEBPAGE_ROOT.resolveFully("keystore.jks")); // Path to your keystore
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
