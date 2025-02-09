package org.mangorage.mangobot.website.servlet;

import htmlflow.HtmlFlow;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.mangorage.mangobot.website.impl.AbstractServlet;

import java.io.IOException;
import java.nio.file.Path;

public class FileDisplayServlet extends AbstractServlet {
    private static final Path UPLOADS = Path.of("webserver/uploads");

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        var file = req.getParameter("id");
        if (file != null) {


            HtmlFlow
                    .doc(res.getWriter())
                    .html()
                    .body()
                    .h1()
                    .text("File QUERIED, " + file);
        } else {
            HtmlFlow
                    .doc(res.getWriter())
                    .html()
                    .body()
                    .h1()
                    .text("NO FILE QUERIED, /file?id=example.txt");
        }
    }
}
