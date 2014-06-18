package org.web4thejob.studio.dom;

import nu.xom.Node;
import org.springframework.util.ReflectionUtils;

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
        m = ReflectionUtils.findMethod(nu.xom.Element.class, "getNamespacePrefixesInScope");
        ReflectionUtils.makeAccessible(m);
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
