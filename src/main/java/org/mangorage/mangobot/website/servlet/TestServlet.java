package org.mangorage.mangobot.website.servlet;

import htmlflow.HtmlFlow;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import org.mangorage.mangobot.website.impl.AbstractServlet;

import java.io.IOException;

public class TestServlet extends AbstractServlet {
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        var flow = HtmlFlow
                .doc(res.getWriter())
                .html()
                .body()
                .h2()
                .text("Test Page!");
    }
}
