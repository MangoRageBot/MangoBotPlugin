package org.mangorage.mangobot.website.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.mangorage.mangobot.website.impl.AbstractServlet;

import java.io.IOException;

public class TestAuthServlet extends AbstractServlet {
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        res.getWriter().write("Test");
    }
}
