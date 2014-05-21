package org.web4thejob.studio.support;

import nu.xom.Element;
import nu.xom.Serializer;
import nu.xom.Text;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ReflectionUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

import static org.springframework.util.Assert.notNull;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.makeAccessible;
import static org.web4thejob.studio.support.StudioUtil.isCodeElement;

/**
 * Created by e36132 on 20/5/2014.
 */
public class MultiplexSerializer extends Serializer {

    private static Invocable invocable;
    private static Field escaper;
    private static Object options;

    static {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("nashorn");

        try {
            escaper = findField(Serializer.class, "escaper");
            notNull(escaper);
            makeAccessible(escaper);

            String basePath = "org/web4thejob/studio/support/js/beautify/";
            Resource js_beautify = new ClassPathResource(basePath + "beautify.js");
            Resource css_beautify = new ClassPathResource(basePath + "beautify-css.js");
            Resource html_beautify = new ClassPathResource(basePath + "beautify-html.js");
            engine.eval(new FileReader(js_beautify.getFile()));
            engine.eval(new FileReader(css_beautify.getFile()));
            engine.eval(new FileReader(html_beautify.getFile()));

            invocable = (Invocable) engine;
            Object json = engine.eval("JSON");
            options = invocable.invokeMethod(json, "parse", "{\"indent_size\": 2}");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MultiplexSerializer(OutputStream out) {
        super(out);
    }

    @Override
    protected void write(Element element) throws IOException {
        if (isCodeElement(element)) {
            writeStartTag(element);
            breakLine();
            writeRaw("<![CDATA[");
            breakLine();
            preserveWhiteSpace(true);

            for (int i = 0; i < element.getChildCount(); i++) {

                if (!(element.getChild(i) instanceof Text)) {
                    writeChild(element.getChild(i));
                    continue;
                }

                Text text = (Text) element.getChild(i);
                String js;
                try {
                    String tag = element.getLocalName();
                    if (tag.equals("attribute") || tag.equals("script") || tag.equals("zscript")) {
                        js = (String) invocable.invokeFunction("js_beautify", text.getValue(), options);
                    } else if (tag.equals("style")) {
                        js = (String) invocable.invokeFunction("css_beautify", text.getValue(), options);
                    } else if (tag.equals("html")) {
                        js = (String) invocable.invokeFunction("html_beautify", text.getValue(), options);
                    } else {
                        throw new IllegalArgumentException(tag + " is not a recognized code block");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                StringTokenizer tokenizer = new StringTokenizer(js, "\n", false);
                while (tokenizer.hasMoreTokens()) {
                    writeRaw(getSpaces(getIndent()) + tokenizer.nextToken());
                    breakLine();
                }
            }

            preserveWhiteSpace(false);
            writeRaw("]]>");
            breakLine();
            writeEndTag(element);
        } else super.write(element);


    }

    private void preserveWhiteSpace(boolean preserve) {
        try {
            Object o = escaper.get(this);
            Method m = ReflectionUtils.findMethod(o.getClass(), "setPreserveSpace", boolean.class);
            notNull(m);
            makeAccessible(m);
            m.invoke(o, preserve);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getSpaces(int number) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= number; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

}
