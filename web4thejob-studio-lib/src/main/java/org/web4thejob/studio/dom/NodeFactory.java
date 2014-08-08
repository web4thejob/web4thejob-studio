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


import org.apache.commons.lang3.reflect.FieldUtils;
import org.web4thejob.studio.support.StudioUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by e36132 on 4/6/2014.
 */
public class NodeFactory extends nu.xom.NodeFactory {
    public NodeFactory() {
        try {
            m.invoke(cache, "zk");
            m.invoke(cache, "zul");
            m.invoke(cache, "native");
            m.invoke(cache, "client");
            m.invoke(cache, "client/attribute");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final Object cache;
    private static final Method m;

    static {
        try {
            Class v = Class.forName("nu.xom.Verifier");
            Field f = FieldUtils.getField(v, "cache", true);
            cache = f.get(null);
            m = StudioUtil.findMethod(cache.getClass(), "put", String.class);
            m.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Element startMakingElement(String name, String namespace) {
        return new Element(name, namespace.intern());
    }
}
