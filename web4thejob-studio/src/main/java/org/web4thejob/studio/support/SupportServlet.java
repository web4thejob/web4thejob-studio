package org.web4thejob.studio.support;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by e36132 on 21/5/2014.
 */

@WebServlet(name = "w4tjstudio", urlPatterns = {"/w4tjstudio-support/*"})
public class SupportServlet extends HttpServlet {
    private static StringBuffer SCRIPTS_CONTENT;
    private static StringBuffer STYLES_CONTENT;

    static {
        String PATH_PREFFIX = "org/web4thejob/studio/support/js/";
        Resource scripts = new ClassPathResource("org/web4thejob/studio/support/script.xml");
        Resource stylesheets = new ClassPathResource("org/web4thejob/studio/support/stylesheet.xml");
        Builder parser = new Builder(false);
        try {
            Document scriptsDoc = parser.build(scripts.getFile());
            Document stylesheetsDoc = parser.build(stylesheets.getFile());

            STYLES_CONTENT = new StringBuffer();
            for (int i = 0; i < stylesheetsDoc.getRootElement().getChildCount(); i++) {
                Node child = stylesheetsDoc.getRootElement().getChild(i);
                if (child instanceof Element && ((Element) child).getLocalName().equals("stylesheet")) {
                    Resource script = new ClassPathResource(PATH_PREFFIX + ((Element) child).getAttributeValue
                            ("src"));
                    STYLES_CONTENT.append(FileUtils.readFileToString(script.getFile()));
                }
            }

            SCRIPTS_CONTENT = new StringBuffer();
            for (int i = 0; i < scriptsDoc.getRootElement().getChildCount(); i++) {
                Node child = scriptsDoc.getRootElement().getChild(i);
                if (child instanceof Element && ((Element) child).getLocalName().equals("script")) {
                    Resource script = new ClassPathResource(PATH_PREFFIX + ((Element) child).getAttributeValue
                            ("src"));
                    SCRIPTS_CONTENT.append(FileUtils.readFileToString(script.getFile()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (SCRIPTS_CONTENT == null || STYLES_CONTENT == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        try (PrintWriter out = response.getWriter()) {
            if (request.getPathInfo().equals("/scripts")) {
                response.setContentType("text/javascript; charset=utf-8");
                out.print(SCRIPTS_CONTENT.toString());
            } else if (request.getPathInfo().equals("/styles")) {
                response.setContentType("text/css; charset=utf-8");
                out.print(STYLES_CONTENT.toString());
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

}
