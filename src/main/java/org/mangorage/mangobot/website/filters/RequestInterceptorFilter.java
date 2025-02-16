package org.mangorage.mangobot.website.filters;
import htmlflow.HtmlFlow;
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
import org.xmlet.htmlapifaster.EnumHttpEquivType;


import javax.swing.text.html.HTML;
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

            HtmlFlow
                    .doc(resp.getWriter())
                    .html()
                    .head()
                    .meta().attrName("og:title").attrContent("MangoBot").__()
                    .meta().attrName("og:description").attrContent("The Official MangoBot Discord Bot.").__()
                    .meta().attrName("og:image").attrContent("https://mangobot.mangorage.org/pink-sheep.png").attrHttpEquiv(EnumHttpEquivType.CONTENT_TYPE).__()
                    .meta().attrName("og:url").attrContent("https://mangobot.mangorage.org/file?id=568d44d8-b6bc-4394-a860-915fac5c085d&target=0").attrHttpEquiv(EnumHttpEquivType.CONTENT_TYPE).__()
                    .meta().attrName("og:type").attrContent("website").__();
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
