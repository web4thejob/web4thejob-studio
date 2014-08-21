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

package org.web4thejob.studio.support;

import nu.xom.Document;
import nu.xom.Serializer;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.io.Files;
import org.zkoss.util.resource.Locators;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

/**
 * Created by e36132 on 2/6/2014.
 */
public abstract class CodeFormatter {

    private static Invocable invocable;
    private static Object options;

    static {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine;

        if (System.getProperty("java.version").startsWith("1.8")) {
            engine = engineManager.getEngineByName("nashorn");
        } else {
            engine = engineManager.getEngineByName("JavaScript"); //rhino
        }

        try {
            String basePath = "org/web4thejob/studio/support/js/beautify/";
            InputStream is;

            is = Locators.getDefault().getResourceAsStream(basePath + "beautify.js");
            engine.eval(new String(Files.readAll(is), Charsets.UTF_8));
            is.close();

            is = Locators.getDefault().getResourceAsStream(basePath + "beautify-css.js");
            engine.eval(new String(Files.readAll(is), Charsets.UTF_8));
            is.close();

            is = Locators.getDefault().getResourceAsStream(basePath + "beautify-html.js");
            engine.eval(new String(Files.readAll(is), Charsets.UTF_8));
            is.close();

            invocable = (Invocable) engine;
            Object json = engine.eval("JSON");
            options = invocable.invokeMethod(json, "parse", "{\"indent_size\": 2}");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatXML(Document xml) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Serializer serializer = new MultiplexSerializer(out);
        try {
            serializer.setIndent(4);
            serializer.write(xml);
            return out.toString("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String formatCSS(String css) {

        if (StringUtils.isBlank(css)) {
            return "";
        }

        try {
            css = (String) invocable.invokeFunction("css_beautify", "<style>" + css + "</style>", options);
            StringBuffer sb = new StringBuffer();
            StringTokenizer tokenizer = new StringTokenizer(css, "\n", true);
            while (tokenizer.hasMoreElements()) {
                String line = tokenizer.nextToken();
                if (line.contains("<style>")) {
                    line = line.replaceFirst("<style>", "");
                    line = line.trim();
                } else if (line.contains("</style>")) {
                    line = line.replaceFirst("</style>", "");
                }
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String formatJS(String js) {
        try {
            return (String) invocable.invokeFunction("js_beautify", js, options);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String formatHTML(String html) {
        try {
            return (String) invocable.invokeFunction("html_beautify", html, options);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


/*
    public static String formatXML(String xml) {
        return formatHTML(xml);
    }

    public static String formatHTML(String html) {
        try {
//            return (String) invocable.invokeFunction("html_beautify", html, options);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
*/

}
