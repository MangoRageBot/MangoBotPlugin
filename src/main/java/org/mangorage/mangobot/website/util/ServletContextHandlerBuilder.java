package org.mangorage.mangobot.website.util;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.mangorage.mangobot.website.impl.StandardHttpServlet;

import java.util.EnumSet;
import java.util.function.Consumer;

public final class ServletContextHandlerBuilder {
    public static ServletContextHandlerBuilder create(ServletContextHandler handler) {
        return new ServletContextHandlerBuilder(handler);
    }


    private final ConstraintSecurityHandlerBuilder securityHandlerBuilder = ConstraintSecurityHandlerBuilder.create();
    private final ServletContextHandler handler;

    ServletContextHandlerBuilder(ServletContextHandler handler) {
        this.handler = handler;
    }

    public ServletContextHandlerBuilder setContextPath(String contextPath) {
        handler.setContextPath(contextPath);
        return this;
    }

    public ServletContextHandlerBuilder setResourceBase(String resourceBase) {
        handler.setResourceBase(resourceBase);
        return this;
    }

    public ServletContextHandlerBuilder setAttribute(String id, Object value) {
        handler.setAttribute(id, value);
        return this;
    }

    public ServletContextHandlerBuilder addFilter(Class<? extends Filter> filterClass, String pathSpec, EnumSet<DispatcherType> dispatches) {
        handler.addFilter(filterClass, pathSpec, dispatches);
        return this;
    }

    public ServletContextHandlerBuilder addServlet(Class<? extends Servlet> servletClass, String pathSpec) {
        handler.addServlet(new ServletHolder(servletClass), pathSpec);
        return this;
    }

    public ServletContextHandlerBuilder addServlet(Class<? extends Servlet> servletClass, String pathSpec, Consumer<ServletHolder> consumer) {
        var holder = new ServletHolder(servletClass);
        consumer.accept(holder);
        handler.addServlet(holder, pathSpec);
        return this;
    }

    public ServletContextHandlerBuilder addHttpServlet(Class<? extends StandardHttpServlet> servletClass, String pathSpec) {
        return addServlet(servletClass, pathSpec);
    }

    public ServletContextHandlerBuilder addHttpServlet(Class<? extends StandardHttpServlet> servletClass, String pathSpec, Consumer<ServletHolder> consumer) {
        return addServlet(servletClass, pathSpec, consumer);
    }

    // OPTIONAL
    public void configureLoginBuilder(Consumer<ConstraintSecurityHandlerBuilder> consumer) {
        consumer.accept(securityHandlerBuilder);
    }

    public ConstraintSecurityHandler getConstraintSecurityHandler() {
        return securityHandlerBuilder.getConstraintSecurityHandler();
    }

    public ServletContextHandler getServletContextHandler() {
        return handler;
    }
}
