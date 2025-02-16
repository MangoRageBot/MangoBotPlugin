package org.mangorage.mangobot.website.filters;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.mangorage.basicutils.LogHelper;


import java.io.IOException;

import static org.mangorage.mangobot.website.util.WebUtil.getOrCreateUserToken;

@WebFilter("/*") // Intercept all incoming requests
public class RequestInterceptorFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (request instanceof Request main) {
            var ip = main.getHeader("X-Forwarded-For");
            LogHelper.info("Intercepted Request from %s for %s -> https://mangobot.mangorage.org%s".formatted(ip == null ? request.getRemoteAddr() : ip, main.getMethod(), main.getOriginalURI()));
        } else if (request instanceof HttpServletRequest http) {
            var ip = http.getHeader("X-Forwarded-For");
            LogHelper.info("Unknown Type (Class) From %s -> %s".formatted(ip == null ? http.getRemoteAddr() : ip, request.getClass()));
        } else {
            LogHelper.info("Unknown Type (Class) From %s -> %s".formatted(request.getRemoteAddr(), request.getClass()));
        }

        if (response instanceof HttpServletResponse resp && request instanceof HttpServletRequest req) {
            getOrCreateUserToken(req, resp); // Either Get it or create a new one!
            resp.setHeader("Content-Security-Policy", "img-src 'self' https://mangobot.mangorage.org/file?id=8e79263e-1579-4e88-8233-62ee91c52156&target=0; frame-src 'self' https://mangobot.mangorage.org/file?id=8e79263e-1579-4e88-8233-62ee91c52156&target=0;");
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
