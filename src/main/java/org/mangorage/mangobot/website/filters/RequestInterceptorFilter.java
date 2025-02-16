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

            response.getWriter().write(
                    """
                            <meta property="og:title" content="MangoBot" />
                            <meta property="og:description" content="The Offical MangoBot Discord Bot." />
                            <meta property="og:image" content="https://mangobot.mangorage.org/file?id=568d44d8-b6bc-4394-a860-915fac5c085d&target=0" />
                            <meta property="og:url" content="https://mangobot.mangorage.org/file?id=568d44d8-b6bc-4394-a860-915fac5c085d&target=0" />
                            <meta property="og:type" content="website" />
                    """
            );
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
