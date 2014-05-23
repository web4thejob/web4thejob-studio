package org.web4thejob.studio.http;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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
    private static final String IMG_PATH_PREFFIX = "org/web4thejob/studio/support/img/";
    private static StringBuffer DESIGNER_SCRIPTS_CONTENT;
    private static StringBuffer DESIGNER_STYLES_CONTENT;
    private static StringBuffer CANVAS_SCRIPTS_CONTENT;

    static {
        String JS_PATH_PREFFIX = "org/web4thejob/studio/support/js/";
        Resource scripts = new ClassPathResource("org/web4thejob/studio/support/script.xml");
        Resource stylesheets = new ClassPathResource("org/web4thejob/studio/support/stylesheet.xml");
        Builder parser = new Builder(false);
        try {
            Document scriptsDoc = parser.build(scripts.getInputStream());
            Document stylesheetsDoc = parser.build(stylesheets.getInputStream());

            DESIGNER_STYLES_CONTENT = new StringBuffer();
            for (int i = 0; i < stylesheetsDoc.getRootElement().getChildCount(); i++) {
                Node child = stylesheetsDoc.getRootElement().getChild(i);
                if (child instanceof Element && ((Element) child).getLocalName().equals("stylesheet")) {
                    Resource script = new ClassPathResource(JS_PATH_PREFFIX + ((Element) child).getAttributeValue
                            ("src"));
                    DESIGNER_STYLES_CONTENT.append(IOUtils.toString(script.getInputStream()));
                }
            }

            DESIGNER_SCRIPTS_CONTENT = new StringBuffer();
            for (int i = 0; i < scriptsDoc.getRootElement().getChildCount(); i++) {
                Node child = scriptsDoc.getRootElement().getChild(i);
                if (child instanceof Element && ((Element) child).getLocalName().equals("script")) {
                    Resource script = new ClassPathResource(JS_PATH_PREFFIX + ((Element) child).getAttributeValue
                            ("src"));
                    DESIGNER_SCRIPTS_CONTENT.append(IOUtils.toString(script.getInputStream()));
                }
            }

            CANVAS_SCRIPTS_CONTENT = new StringBuffer();
            Resource script = new ClassPathResource(JS_PATH_PREFFIX + "canvas.js");
            CANVAS_SCRIPTS_CONTENT.append(IOUtils.toString(script.getInputStream()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (DESIGNER_SCRIPTS_CONTENT == null || DESIGNER_STYLES_CONTENT == null || CANVAS_SCRIPTS_CONTENT == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String path = request.getPathInfo();
        if (path.equals("/scripts") || path.equals("/styles") || path.equals("/canvas")) {
            try (PrintWriter out = response.getWriter()) {
                if (request.getPathInfo().equals("/scripts")) {
                    response.setContentType("text/javascript; charset=utf-8");
                    out.print(DESIGNER_SCRIPTS_CONTENT.toString());
                } else if (request.getPathInfo().equals("/styles")) {
                    response.setContentType("text/css; charset=utf-8");
                    out.print(DESIGNER_STYLES_CONTENT.toString());
                } else if (request.getPathInfo().equals("/canvas")) {
                    response.setContentType("text/javascript; charset=utf-8");
                    out.print(CANVAS_SCRIPTS_CONTENT.toString());
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        } else if (path.equals("/img")) {
            String f = request.getParameter("f");
            if (f == null || f.trim().length() == 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Resource img = new ClassPathResource(IMG_PATH_PREFFIX + f.trim());
            if (!img.exists()) {
                img = new ClassPathResource(IMG_PATH_PREFFIX + "zk.png");
                if (!img.exists()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }

            try (ServletOutputStream out = response.getOutputStream()) {
                byte[] raw = IOUtils.toByteArray(img.getInputStream());
                response.setContentType("image/" + FilenameUtils.getExtension(img.getFilename()));
                response.setContentLength(raw.length);
                out.write(raw);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

}
