package org.mangorage.mangobot.website.handlers;

import htmlflow.HtmlFlow;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.handler.ErrorHandler;

import javax.swing.text.html.HTML;
import java.io.IOException;
import java.io.Writer;

public class DefaultErrorHandler extends ErrorHandler {
    @Override
    protected void handleErrorPage(HttpServletRequest request, Writer writer, int code, String message) throws IOException {
        HtmlFlow
                .doc(writer)
                .html()
                .body()
                .h1()
                .text("" + code)
                .__()
                .h4()
                .text(message);
    }
}
