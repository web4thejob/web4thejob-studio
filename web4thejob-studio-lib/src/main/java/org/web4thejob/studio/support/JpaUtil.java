package org.web4thejob.studio.support;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static org.zkoss.lang.Generics.cast;

/**
 * Created by e36132 on 3/7/2014.
 */
public class JpaUtil {
    public static EntityComparator ENTITY_SORTER_INSTANCE = new EntityComparator();

    public static synchronized EntityManagerFactory getEntityManagerFactory(String name) {
        Session session = Executions.getCurrent().getSession();
        Map<String, EntityManagerFactory> emfs = cast(session.getAttribute("w4tjstudio-emfs"));
        if (emfs != null) {
            return emfs.get(name);
        }
        return null;
    }

    public static synchronized Map<String, EntityManagerFactory> getEntityManagerFactories() {
        Session session = Executions.getCurrent().getSession();
        return cast(session.getAttribute("w4tjstudio-emfs"));
    }

    public static synchronized void removeEntityManagerFactory(String name) {
        Session session = Executions.getCurrent().getSession();
        Map<String, EntityManagerFactory> emfs = cast(session.getAttribute("w4tjstudio-emfs"));
        if (emfs != null) {
            emfs.remove(name);
        }
    }

    public static synchronized void setEntityManagerFactory(String name, EntityManagerFactory emf) {
        Session session = Executions.getCurrent().getSession();
        Map<String, EntityManagerFactory> emfs = cast(session.getAttribute("w4tjstudio-emfs"));
        if (emfs == null) {
            emfs = new HashMap<>();
            session.setAttribute("w4tjstudio-emfs", emfs);
        }
        emfs.put(name, emf);
    }

    public static synchronized Map<String, String> getConnectionProperties(String name) {
        Session session = Executions.getCurrent().getSession();
        Map<String, Map<String, String>> properties = cast(session.getAttribute("w4tjstudio-properties"));
        if (properties != null) {
            return properties.get(name);
        }
        return null;
    }

    public static synchronized void setConnectionProperties(String name, Map<String, String> prop) {
        Session session = Executions.getCurrent().getSession();
        Map<String, Map<String, String>> properties = cast(session.getAttribute("w4tjstudio-properties"));
        if (properties == null) {
            properties = new HashMap<>();
            session.setAttribute("w4tjstudio-properties", properties);
        }
        properties.put(name, prop);
    }

    public static class EntityComparator implements Comparator<EntityType> {

        @Override
        public int compare(EntityType o1, EntityType o2) {
            return o1.getJavaType().getCanonicalName().compareTo(o2.getJavaType().getCanonicalName());
        }
    }
}
