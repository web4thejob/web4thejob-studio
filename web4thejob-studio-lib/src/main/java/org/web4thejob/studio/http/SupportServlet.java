/*
 * Copyright 2014 Veniamin Isaias
 *
 * This file is part of Web4thejob Studio.
 *
 * Web4thejob Studio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Web4thejob Studio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Web4thejob Studio.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.web4thejob.studio.http;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import org.apache.commons.codec.Charsets;
import org.zkoss.io.Files;
import org.zkoss.util.resource.Locators;
import org.zkoss.web.servlet.Servlets;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * Created by e36132 on 21/5/2014.
 */

@WebServlet(name = "w4tjstudio", urlPatterns = {"/w4tjstudio-support/img",
        "/w4tjstudio-support/designer/scripts", "/w4tjstudio-support/designer/styles",
        "/w4tjstudio-support/canvas/scripts", "/w4tjstudio-support/canvas/styles",
        "/w4tjstudio-support/fonts/*", "/w4tjstudio-support/designer/images/*"})
public class SupportServlet extends HttpServlet {
    private static final String IMG_PATH_PREFFIX = "org/web4thejob/studio/support/img/";
    private static StringBuffer DESIGNER_SCRIPTS_CONTENT;
    private static StringBuffer DESIGNER_STYLES_CONTENT;
    private static StringBuffer CANVAS_SCRIPTS_CONTENT;
    private static StringBuffer CANVAS_STYLES_CONTENT;

    static {
        String CSS_PATH_PREFFIX = "org/web4thejob/studio/support/css/";
        String JS_PATH_PREFFIX = "org/web4thejob/studio/support/js/";
        Builder parser = new Builder(false);
        try {
            InputStream is;

            is = Locators.getDefault().getResourceAsStream("org/web4thejob/studio/support/script.xml");
            Document scriptsDoc = parser.build(is);
            is.close();

            is = Locators.getDefault().getResourceAsStream("org/web4thejob/studio/support/stylesheet.xml");
            Document stylesheetsDoc = parser.build(is);
            is.close();

            DESIGNER_STYLES_CONTENT = new StringBuffer();
            for (int i = 0; i < stylesheetsDoc.getRootElement().getChildCount(); i++) {
                Node child = stylesheetsDoc.getRootElement().getChild(i);
                if (child instanceof Element && ((Element) child).getLocalName().equals("stylesheet")) {
                    is = Locators.getDefault().getResourceAsStream(JS_PATH_PREFFIX + ((Element) child).getAttributeValue("src"));
                    DESIGNER_STYLES_CONTENT.append(new String(Files.readAll(is), Charsets.UTF_8));
                    is.close();
                }
            }

            DESIGNER_SCRIPTS_CONTENT = new StringBuffer();
            for (int i = 0; i < scriptsDoc.getRootElement().getChildCount(); i++) {
                Node child = scriptsDoc.getRootElement().getChild(i);
                if (child instanceof Element && ((Element) child).getLocalName().equals("script")) {
                    is = Locators.getDefault().getResourceAsStream(JS_PATH_PREFFIX + ((Element) child).getAttributeValue("src"));
                    DESIGNER_SCRIPTS_CONTENT.append(new String(Files.readAll(is), Charsets.UTF_8));
                    is.close();
                }
            }

            CANVAS_SCRIPTS_CONTENT = new StringBuffer();
            is = Locators.getDefault().getResourceAsStream(JS_PATH_PREFFIX + "canvas.js");
            CANVAS_SCRIPTS_CONTENT.append(new String(Files.readAll(is), Charsets.UTF_8));
            is.close();

            CANVAS_STYLES_CONTENT = new StringBuffer();
            is = Locators.getDefault().getResourceAsStream(CSS_PATH_PREFFIX + "canvas.css");
            CANVAS_STYLES_CONTENT.append(new String(Files.readAll(is), Charsets.UTF_8));
            is.close();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (DESIGNER_SCRIPTS_CONTENT == null || DESIGNER_STYLES_CONTENT == null ||
                CANVAS_SCRIPTS_CONTENT == null || CANVAS_STYLES_CONTENT == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setHeader("Access-Control-Allow-Origin", "*");
        String path = request.getServletPath();
        if (path.equals("/w4tjstudio-support/img")) {
            String f = request.getParameter("f");
            if (f == null || f.trim().length() == 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String fileName = IMG_PATH_PREFFIX + f.trim();
            InputStream img = Locators.getDefault().getResourceAsStream(fileName);
            if (img == null) {
                fileName = IMG_PATH_PREFFIX + "zk.png";
                img = Locators.getDefault().getResourceAsStream(fileName);
                if (img == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }

            try (ServletOutputStream out = response.getOutputStream()) {
                byte[] raw = Files.readAll(img);
                img.close();
                String extension = Servlets.getExtension(fileName);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("image/" + extension);
                response.setContentLength(raw.length);
                out.write(raw);
            }
        } else if (path.equals("/w4tjstudio-support/fonts")) {
            int dot = request.getRequestURI().lastIndexOf(".");
            String ext = request.getRequestURI().substring(dot);
            String font = "org/web4thejob/studio/support/js/font-awesome/fonts/fontawesome-webfont" + ext;

            try (ServletOutputStream outraw = response.getOutputStream()) {
                InputStream is = Locators.getDefault().getResourceAsStream(font);
                byte[] raw = Files.readAll(is);
                is.close();
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/octet-stream");
                response.setContentLength(raw.length);
                outraw.write(raw);
            }
        } else if (path.equals("/w4tjstudio-support/designer/images")) {
            String dir = "org/web4thejob/studio/support/js/jquery-ui/images/";
            int slash = request.getRequestURI().lastIndexOf("/");
            String file = request.getRequestURI().substring(slash + 1);

            int dot = file.lastIndexOf(".");
            String ext = file.substring(dot + 1);

            try (ServletOutputStream outraw = response.getOutputStream()) {
                InputStream is = Locators.getDefault().getResourceAsStream(dir + file);
                byte[] raw = Files.readAll(is);
                is.close();
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("image/" + ext);
                response.setContentLength(raw.length);
                outraw.write(raw);
            }

        } else {
            try (PrintWriter out = response.getWriter()) {
                if (path.equals("/w4tjstudio-support/designer/scripts")) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("text/javascript; charset=utf-8");
                    out.print(DESIGNER_SCRIPTS_CONTENT.toString());
                } else if (path.equals("/w4tjstudio-support/designer/styles")) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("text/css; charset=utf-8");
                    out.print(DESIGNER_STYLES_CONTENT.toString());
                } else if (path.equals("/w4tjstudio-support/canvas/scripts")) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("text/javascript; charset=utf-8");
                    out.print(CANVAS_SCRIPTS_CONTENT.toString());
                } else if (path.equals("/w4tjstudio-support/canvas/styles")) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("text/css; charset=utf-8");
                    out.print(CANVAS_STYLES_CONTENT.toString());
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }

        }

        response.flushBuffer();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

}
