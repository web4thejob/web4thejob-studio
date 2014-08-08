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

package org.web4thejob.studio.dom;

import nu.xom.Node;
import org.web4thejob.studio.support.StudioUtil;

import java.lang.reflect.Method;
import java.util.Map;

import static org.zkoss.lang.Generics.cast;

/**
 * Created by e36132 on 4/6/2014.
 */
public class Element extends nu.xom.Element {
    public Element(String name, String uri) {
        super(name, uri.intern());
    }

    public Element(nu.xom.Element element) {
        super(element);
    }

    private static final Method m;

    static {
        m = StudioUtil.findMethod(nu.xom.Element.class, "getNamespacePrefixesInScope");
        m.setAccessible(true);
    }

    @Override
    public void addNamespaceDeclaration(String prefix, String uri) {
        super.addNamespaceDeclaration(prefix, uri.intern());
    }

    public Map<String, String> getNamespacePrefixesInScope() {
        try {
            return cast((Map) m.invoke(this));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Node copy() {
        return new Element(this);
    }
}
