package org.web4thejob.studio.dom;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;

import static org.zkoss.lang.Generics.cast;

/**
 * Created by e36132 on 4/6/2014.
 */
public class Element extends nu.xom.Element {
    private static final Method m;

    static {
        m = ReflectionUtils.findMethod(nu.xom.Element.class, "getNamespacePrefixesInScope");
        ReflectionUtils.makeAccessible(m);
    }

    public Element(String name, String uri) {
        super(name, uri.intern());
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


}
