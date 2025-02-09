package org.mangorage.mangobot.website;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import org.eclipse.jetty.server.Request;
import org.mangorage.basicutils.LogHelper;


import java.io.IOException;

@WebFilter("/*") // Intercept all incoming requests
public class RequestInterceptorFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof Request main) {
            LogHelper.info("Intercepted Request %s -> https://mangobot.mangorage.org%s".formatted(main.getMethod(), main.getOriginalURI()));
        } else {
            LogHelper.info("Unknown Type -> " + request.getClass());
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
