package org.mangorage.mangobot.website.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.mangorage.mangobot.website.impl.ObjectMap;
import org.mangorage.mangobot.website.impl.StandardHttpServlet;
import org.mangorage.mangobot.website.util.WebConstants;

import java.io.IOException;

public class AccountServlet extends StandardHttpServlet {
    public UserIdentity getUserIdentity(HttpServletRequest request) {
        Request baseRequest = Request.getBaseRequest(request);
        return baseRequest != null ? baseRequest.getUserIdentity() : null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMap map = (ObjectMap) getServletConfig().getServletContext().getAttribute(WebConstants.WEB_OBJECT_ID);
        var service = map.get(WebConstants.LOGIN_SERVICE, SecurityHandler.class);

        var identity = getUserIdentity(req);

        if (identity != null && service.getLoginService().validate(identity)) {
            resp.getWriter().write("Welcome %s!".formatted(identity.getUserPrincipal().getName()));
        } else {
            resp.getWriter().write("No Account Signed In");
        }
    }
}
