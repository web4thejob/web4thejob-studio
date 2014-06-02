package org.web4thejob.studio.support;

import org.apache.commons.io.IOUtils;
import org.zkoss.util.resource.Locators;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

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
            engine.eval(IOUtils.toString(Locators.getDefault().getResourceAsStream(basePath + "beautify.js")));
            engine.eval(IOUtils.toString(Locators.getDefault().getResourceAsStream(basePath + "beautify-css.js")));
            engine.eval(IOUtils.toString(Locators.getDefault().getResourceAsStream(basePath + "beautify-html.js")));

            invocable = (Invocable) engine;
            Object json = engine.eval("JSON");
            options = invocable.invokeMethod(json, "parse", "{\"indent_size\": 2}");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

}
