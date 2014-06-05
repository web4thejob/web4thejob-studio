package org.web4thejob.studio.support;

import nu.xom.Element;
import nu.xom.Serializer;
import nu.xom.Text;
import org.springframework.util.ReflectionUtils;

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

    public MultiplexSerializer(OutputStream out) {
        super(out);
    }

    private static Field escaper;
    static {
        try {
            escaper = findField(Serializer.class, "escaper");
            escaper.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void write(Element element) throws IOException {
        if (isCodeElement(element)) {

//            if (element.getAttribute("src") != null) {
//                super.write(element);
//                return;
//            }

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
                        js = CodeFormatter.formatJS(text.getValue());
                    } else if (tag.equals("style")) {
                        js = CodeFormatter.formatCSS(text.getValue());
                    } else if (tag.equals("html")) {
                        js = CodeFormatter.formatHTML(text.getValue());
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
