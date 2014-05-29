package org.web4thejob.studio.support;

import java.util.Map;

/**
 * @author Veniamin Isaias
 * @since 1.0.0
 */
public interface ChildDelegate<T> {

    void onChild(T child, Map<String, Object> params);
}
