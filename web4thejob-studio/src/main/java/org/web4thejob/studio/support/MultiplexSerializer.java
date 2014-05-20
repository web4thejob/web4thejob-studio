package org.web4thejob.studio.support;

import nu.xom.Element;
import nu.xom.Serializer;
import nu.xom.Text;
import org.springframework.core.io.Resource;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.support.ServletContextResource;
import org.zkoss.zk.ui.Executions;

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

/**
 * Created by e36132 on 20/5/2014.
 */
public class MultiplexSerializer extends Serializer {

    private static ScriptEngine engine;
    private static Invocable invocable;

    {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByName("nashorn");

        try {
            Resource resource = new ServletContextResource(Executions.getCurrent().getDesktop().getWebApp()
                    .getServletContext(), "js/beautify.js");
            engine.eval(new FileReader(resource.getFile()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        invocable = (Invocable) engine;
    }
    private Field escaper;

    public MultiplexSerializer(OutputStream out) {
        super(out);
        escaper = findField(Serializer.class, "escaper");
        notNull(escaper);
        makeAccessible(escaper);
    }

    @Override
    protected void write(Element element) throws IOException {
        if ("attribute".equals(element.getLocalName()) || "script".equals(element.getLocalName())) {
            writeStartTag(element);
            breakLine();
            writeRaw("<![CDATA[");
            breakLine();
            preserveWhiteSpace(true);

            for (int i = 0; i < element.getChildCount(); i++) {
                Text text = (Text) element.getChild(i);

                String js;
                try {
                    Object json = engine.eval("JSON");
                    Object options = invocable.invokeMethod(json, "parse", "{\"indent_size\": 2}");
                    js = (String) invocable.invokeFunction("js_beautify", text.getValue(), options);
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
//            decrementIndent();
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

    private void decrementIndent() {
        try {
            Object o = escaper.get(this);
            Method m = ReflectionUtils.findMethod(o.getClass(), "decrementIndent");
            notNull(m);
            makeAccessible(m);
            m.invoke(o);
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
