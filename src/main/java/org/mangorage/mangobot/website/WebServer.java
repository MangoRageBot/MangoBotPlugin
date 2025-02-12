package org.mangorage.mangobot.website;



import jakarta.servlet.DispatcherType;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jetbrains.annotations.NotNull;
import org.mangorage.basicutils.LogHelper;
import org.mangorage.mangobot.website.impl.ObjectMap;
import org.mangorage.mangobot.website.servlet.FileServlet;
import org.mangorage.mangobot.website.servlet.FileUploadServlet;
import org.mangorage.mangobot.website.servlet.InfoServlet;
import org.mangorage.mangobot.website.servlet.TestAuthServlet;
import org.mangorage.mangobot.website.servlet.TricksServlet;
import java.util.EnumSet;
import java.util.function.Consumer;

public final class WebServer {
    public static final ResolveString WEBPAGE_INTERNAL = new ResolveString("webpage-internal");
    public static final ResolveString WEBPAGE_ROOT = new ResolveString("webpage-root");
    public static final ResolveString WEBPAGE_PAGE = WEBPAGE_ROOT.resolve("webpage");


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

    public static void main(String[] args) {
        startWebServerSafely(new ObjectMap());
    }

    public static void startWebServer(ObjectMap objectMap) throws Exception {
        Server server = new Server();

        // Combine the handlers (file, jar resource handlers, and security)
        HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(configureInternalResourceHandler());
        handlers.addHandler(configureExternalResourceHandler());
        handlers.addHandler(configureServlets(objectMap));


        var securityHandler = configureAuth();
        securityHandler.setHandler(handlers);
        server.setHandler(securityHandler);


        ServerConnector connector = getServerConnector(server);
        server.addConnector(connector);


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

    private static @NotNull ServletContextHandler configureServlets(ObjectMap objectMap) {
        // Create the context and servlet handler
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase(WEBPAGE_PAGE.value());
        context.addServlet(DefaultServlet.class, "/*");

        // Add your servlets here as required
        context.addServlet(of(InfoServlet.class), "/info");
        context.addServlet(of(TricksServlet.class), "/trick");
        context.addServlet(of(FileUploadServlet.class, h -> {
            h.getRegistration().setMultipartConfig(new MultipartConfigElement("/tmp/uploads"));
        }), "/upload");
        context.addServlet(of(FileServlet.class), "/file");
        context.addServlet(of(TestAuthServlet.class), "/testAuth");
        context.setAttribute("map", objectMap);

        // Add filters if needed
        context.addFilter(RequestInterceptorFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        return context;
    }

    private static @NotNull ConstraintSecurityHandler configureAuth() {
        // Security Configuration
        Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[]{"admin", "user"}); // Define allowed roles

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/testAuth");

        // Create a login service (in-memory for demo purposes, replace with your own)
        HashLoginService loginService = new HashLoginService("MyRealm");
        loginService.setFullValidate(true);
        UserStore store = new UserStore();
        store.addUser("admin", Credential.getCredential("admin"), new String[]{"admin"});
        loginService.setUserStore(store);

        // Create the security handler
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticator(new BasicAuthenticator());
        securityHandler.addConstraintMapping(cm);
        securityHandler.setLoginService(loginService);

        return securityHandler;
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
