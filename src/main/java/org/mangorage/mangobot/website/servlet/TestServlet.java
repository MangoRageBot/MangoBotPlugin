package org.mangorage.mangobot.website.servlet;

import htmlflow.HtmlFlow;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mangorage.mangobot.website.impl.StandardHttpServlet;
import org.xmlet.htmlapifaster.EnumEnctypeType;
import org.xmlet.htmlapifaster.EnumMethodType;
import org.xmlet.htmlapifaster.EnumTypeInputType;

import java.io.IOException;

public class TestServlet extends StandardHttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HtmlFlow
                .doc(resp.getWriter())
                .html()
                .head().title().text("File Upload Page").__().__()
                .body()
                .h1().text("Upload a File").__()
                .form().attrMethod(EnumMethodType.POST).attrAction("/upload").attrEnctype(EnumEnctypeType.MULTIPART_FORM_DATA)
                .input().attrType(EnumTypeInputType.FILE).attrName("file").__()
                .br().__()
                .input().attrType(EnumTypeInputType.SUBMIT).attrValue("Upload").__()
                .__()
                .__()
                .__();
    }
}
