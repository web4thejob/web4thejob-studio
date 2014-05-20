package org.web4thejob.studio.support;

import nu.xom.Element;
import nu.xom.Serializer;
import nu.xom.Text;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

import static org.springframework.util.Assert.notNull;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.makeAccessible;

/**
 * Created by e36132 on 20/5/2014.
 */
public class MultiplexSerializer extends Serializer {
    private Field escaper;

    public MultiplexSerializer(OutputStream out) {
        super(out);
        escaper = findField(Serializer.class, "escaper");
        notNull(escaper);
        makeAccessible(escaper);
    }

    @Override
    protected void write(Element element) throws IOException {
        if ("attribute".equals(element.getLocalName())) {
            writeStartTag(element);
            breakLine();
            writeRaw("<![CDATA[");
            breakLine();
            preserveWhiteSpace(true);

            for (int i = 0; i < element.getChildCount(); i++) {
                Text text = (Text) element.getChild(i);
                StringTokenizer tokenizer = new StringTokenizer(text.getValue(), "\n", false);
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
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
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
