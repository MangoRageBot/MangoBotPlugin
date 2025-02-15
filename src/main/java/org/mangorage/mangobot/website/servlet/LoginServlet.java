package org.mangorage.mangobot.website.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.mangorage.mangobot.website.impl.ObjectMap;
import org.mangorage.mangobot.website.impl.StandardHttpServlet;
import org.mangorage.mangobot.website.util.WebConstants;

import java.io.IOException;
import java.util.Base64;

public class LoginServlet extends StandardHttpServlet {

    public UserIdentity getUserIdentity(HttpServletRequest request) {
        Request baseRequest = Request.getBaseRequest(request);
        return baseRequest != null ? baseRequest.getUserIdentity() : null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Retrieve LoginService and Authenticator from ObjectMap
        ObjectMap map = (ObjectMap) getServletConfig().getServletContext().getAttribute(WebConstants.WEB_OBJECT_ID);
        Authenticator authenticator = map.get("auth", Authenticator.class);

        // Use Jetty's existing Basic Auth
        Authentication authentication = null;
        try {
            authentication = authenticator.validateRequest(req, resp, true);
        } catch (ServerAuthException e) {
            throw new RuntimeException(e);
        }

        if (authentication instanceof Authentication.User) {
            UserIdentity identity = ((Authentication.User) authentication).getUserIdentity();
            if (identity != null) {
                // Store user identity and redirect
                req.setAttribute("org.eclipse.jetty.server.UserIdentity", identity);
                resp.sendRedirect("/account");
                return;
            }
        }

        // If not authenticated, force Basic Auth popup
        resp.setHeader("WWW-Authenticate", "Basic realm=\"My Secure Area\"");
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().println("HTTP 401 - Unauthorized");
    }
}