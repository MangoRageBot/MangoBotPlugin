package org.mangorage.mangobot.website.servlet;

import htmlflow.HtmlFlow;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import org.mangorage.mangobot.website.impl.AbstractServlet;
import org.xmlet.htmlapifaster.EnumEnctypeType;
import org.xmlet.htmlapifaster.EnumMethodType;
import org.xmlet.htmlapifaster.EnumTypeInputType;

import java.io.IOException;

public class TestServlet extends AbstractServlet {
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        HtmlFlow.doc(res.getWriter())
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
