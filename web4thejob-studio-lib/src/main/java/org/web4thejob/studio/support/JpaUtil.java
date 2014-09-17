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

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static org.zkoss.lang.Generics.cast;

public class JpaUtil {
    public static EntityComparator ENTITY_SORTER_INSTANCE = new EntityComparator();

    public static synchronized EntityManagerFactory getEntityManagerFactory(String name) {
        WebApp webApp = Executions.getCurrent().getDesktop().getWebApp();
        Map<String, EntityManagerFactory> emfs = cast(webApp.getAttribute("w4tjstudio-emfs"));
        if (emfs != null) {
            return emfs.get(name);
        }
        return null;
    }

    public static synchronized Map<String, EntityManagerFactory> getEntityManagerFactories() {
        WebApp webApp = Executions.getCurrent().getDesktop().getWebApp();
        return cast(webApp.getAttribute("w4tjstudio-emfs"));
    }

    public static synchronized void removeEntityManagerFactory(String name) {
        WebApp webApp = Executions.getCurrent().getDesktop().getWebApp();
        Map<String, EntityManagerFactory> emfs = cast(webApp.getAttribute("w4tjstudio-emfs"));
        if (emfs != null) {
            emfs.remove(name);
        }
    }

    public static synchronized void setEntityManagerFactory(String name, EntityManagerFactory emf) {
        WebApp webApp = Executions.getCurrent().getDesktop().getWebApp();
        Map<String, EntityManagerFactory> emfs = cast(webApp.getAttribute("w4tjstudio-emfs"));
        if (emfs == null) {
            emfs = new HashMap<>();
            webApp.setAttribute("w4tjstudio-emfs", emfs);
        }
        emfs.put(name, emf);
    }

    public static synchronized Map<String, String> getConnectionProperties(String name) {
        WebApp webApp = Executions.getCurrent().getDesktop().getWebApp();
        Map<String, Map<String, String>> properties = cast(webApp.getAttribute("w4tjstudio-properties"));
        if (properties != null) {
            return properties.get(name);
        }
        return null;
    }

    public static synchronized void setConnectionProperties(String name, Map<String, String> prop) {
        WebApp webApp = Executions.getCurrent().getDesktop().getWebApp();
        Map<String, Map<String, String>> properties = cast(webApp.getAttribute("w4tjstudio-properties"));
        if (properties == null) {
            properties = new HashMap<>();
            webApp.setAttribute("w4tjstudio-properties", properties);
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
