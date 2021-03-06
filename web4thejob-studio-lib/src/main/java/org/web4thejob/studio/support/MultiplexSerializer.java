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

import nu.xom.Element;
import nu.xom.Serializer;
import nu.xom.Text;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

import static org.apache.commons.lang3.Validate.notNull;
import static org.web4thejob.studio.support.StudioUtil.findMethod;
import static org.web4thejob.studio.support.StudioUtil.isCodeElement;

/**
 * Created by e36132 on 20/5/2014.
 */
public class MultiplexSerializer extends Serializer {


    public MultiplexSerializer(OutputStream out) {
        super(out);
        try {
            Object o = escaper.get(this);

            incrementIndent = findMethod(o.getClass(), "incrementIndent");
            notNull(incrementIndent);
            incrementIndent.setAccessible(true);

            decrementIndent = findMethod(o.getClass(), "decrementIndent");
            notNull(decrementIndent);
            decrementIndent.setAccessible(true);

            setPreserveSpace = findMethod(o.getClass(), "setPreserveSpace", boolean.class);
            notNull(setPreserveSpace);
            setPreserveSpace.setAccessible(true);

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    private static Field escaper;
    private Method incrementIndent;
    private Method decrementIndent;
    private Method setPreserveSpace;

    static {
        try {
            escaper = FieldUtils.getField(Serializer.class, "escaper", true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void write(Element element) throws IOException {
        if (isCodeElement(element)) {

            //native javascript block, do not process
            if (element.getAttribute("src") != null) {
                super.write(element);
                return;
            }

            boolean needsCDATA = !"style".equals(element.getLocalName());

            writeStartTag(element);

            if (needsCDATA) {
                breakLine();
                writeRaw("<![CDATA[");
                incrementIndent();
            }

            preserveWhiteSpace(true);

            for (int i = 0; i < element.getChildCount(); i++) {

                if (!(element.getChild(i) instanceof Text)) {
                    writeChild(element.getChild(i));
                    continue;
                }

                Text text = (Text) element.getChild(i);
                String source;
                try {
                    String tag = element.getLocalName();
                    if (tag.equals("attribute") || tag.equals("script") || tag.equals("zscript")) {
                        source = CodeFormatter.formatJS(text.getValue());
                    } else if (tag.equals("style")) {
                        source = CodeFormatter.formatCSS(text.getValue());
                    } else if (tag.equals("html")) {
                        source = CodeFormatter.formatHTML(text.getValue());
                    } else {
                        throw new IllegalArgumentException(tag + " is not a recognized code block");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                StringTokenizer tokenizer = new StringTokenizer(source, "\n", false);
                while (tokenizer.hasMoreTokens()) {
                    breakLine();
                    writeRaw(tokenizer.nextToken());
                }
            }

            preserveWhiteSpace(false);

            if (needsCDATA) {
                decrementIndent();
                breakLine();
                writeRaw("]]>");
            }

            //necessary trick to achieve proper indentation in closing tag
            //Serializer looks for non text children ???
            element = (Element) element.copy();
            element.appendChild(new Element("fake"));
            writeEndTag(element);
        } else super.write(element);


    }


    private void preserveWhiteSpace(boolean preserve) {
        try {
            setPreserveSpace.invoke(escaper.get(this), preserve);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void decrementIndent() {
        try {
            decrementIndent.invoke(escaper.get(this));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void incrementIndent() {
        try {
            incrementIndent.invoke(escaper.get(this));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
