package org.web4thejob.studio.dom;


import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by e36132 on 4/6/2014.
 */
public class NodeFactory extends nu.xom.NodeFactory {
    private static final Object cache;
    private static final Method m;

    static {
        try {
            Class v = Class.forName("nu.xom.Verifier");
            Field f = ReflectionUtils.findField(v, "cache");
            ReflectionUtils.makeAccessible(f);
            cache = f.get(null);
            m = ReflectionUtils.findMethod(cache.getClass(), "put", String.class);
            ReflectionUtils.makeAccessible(m);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    @Override
    public Element startMakingElement(String name, String namespace) {
        return new Element(name, namespace.intern());
    }
}
