package org.mangorage.mangobot.website.impl;

import htmlflow.HtmlFlow;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.xmlet.htmlapifaster.EnumHttpEquivType;

import java.io.IOException;

public abstract class StandardHttpServlet extends HttpServlet {

    public StandardHttpServlet() {
        super();
    }

    @Override
    protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
        if (hasEmbed()) handleEmbed(resp);
    }

    @Override
    public final void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        super.service(req, res);
    }


    /**
     * Use Default (styles.css) or getServletInfo().css
     */
    public boolean useDefaultStyles() {
        return true;
    }

    public String getStyles() {
        return useDefaultStyles() ? "css/styles.css" : STR."css/\{getServletInfo()}.css";
    }

    @Override
    public String getServletInfo() {
        return getClass().getSimpleName();
    }

    public boolean hasEmbed() {
        return false;
    }

    public void handleEmbed(HttpServletResponse response) throws IOException {
        HtmlFlow
                .doc(response.getWriter())
                .html()
                .head()
                .meta().attrName("og:title").attrContent("MangoBot").__()
                .meta().attrName("og:description").attrContent("The Official MangoBot Discord Bot.").__()
                .meta().attrName("og:image").attrContent("https://mangobot.mangorage.org/pink-sheep.png").attrHttpEquiv(EnumHttpEquivType.CONTENT_TYPE).__()
                .meta().attrName("og:url").attrContent("https://mangobot.mangorage.org/file?id=568d44d8-b6bc-4394-a860-915fac5c085d&target=0").attrHttpEquiv(EnumHttpEquivType.CONTENT_TYPE).__()
                .meta().attrName("og:type").attrContent("website").__();
    }

    // ALL AVAILABLE METHODS....

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doHead(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }

    @Override
    protected long getLastModified(HttpServletRequest req) {
        return super.getLastModified(req);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doOptions(req, resp);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doTrace(req, resp);
    }
}
