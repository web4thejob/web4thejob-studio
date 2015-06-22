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

package org.web4thejob.studio.controller.impl;

import nu.xom.*;
import org.web4thejob.studio.support.JpaUtil;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.net.URL;
import java.util.*;

import static org.web4thejob.studio.support.JpaUtil.ENTITY_SORTER_INSTANCE;


/**
 * Created by e36132 on 16/4/2014.
 */
public class JpaInfoController extends SelectorComposer<Component> {
    @Wire
    Div jpacontroller;
    @Wire
    Listbox punitList;
    @Wire
    Listbox managedList;


    private static SortedMap<String, Map<String, String>> getPersistenceUnitNames() {
        SortedMap<String, Map<String, String>> names = new TreeMap<>();


        try {
            XPathContext ctx = new XPathContext("p", "http://xmlns.jcp.org/xml/ns/persistence");

            Enumeration<URL> punits = Persistence.class.getClassLoader().getResources("META-INF/persistence.xml");
            while (punits.hasMoreElements()) {
                URL punit = punits.nextElement();
                Builder parser = new Builder(false);
                Document document = parser.build(punit.openStream());
                Nodes nodes = document.query("p:persistence/p:persistence-unit", ctx);
                if (nodes.size() == 1) {
                    String name = ((Element) nodes.get(0)).getAttributeValue("name");

                    Map<String, String> properties = JpaUtil.getConnectionProperties(name);
                    if (properties == null) {
                        properties = new HashMap<>();
                        JpaUtil.setConnectionProperties(name, properties);
                        properties.put("name", name);

                        nodes = document.query("p:persistence/p:persistence-unit/p:properties/p:property[@name='javax.persistence.jdbc.driver']", ctx);
                        if (nodes.size() == 1) {
                            properties.put("driver", ((Element) nodes.get(0)).getAttributeValue("value"));
                        }
                        nodes = document.query("p:persistence/p:persistence-unit/p:properties/p:property[@name='javax.persistence.jdbc.url']", ctx);
                        if (nodes.size() == 1) {
                            properties.put("url", ((Element) nodes.get(0)).getAttributeValue("value"));
                        }
                        nodes = document.query("p:persistence/p:persistence-unit/p:properties/p:property[@name='javax.persistence.jdbc.user']", ctx);
                        if (nodes.size() == 1) {
                            properties.put("user", ((Element) nodes.get(0)).getAttributeValue("value"));
                        }
                        nodes = document.query("p:persistence/p:persistence-unit/p:properties/p:property[@name='javax.persistence.jdbc.password']", ctx);
                        if (nodes.size() == 1) {
                            properties.put("password", ((Element) nodes.get(0)).getAttributeValue("value"));
                        }
                    }

                    names.put(name + "|" + punit.toString(), properties);

                }
            }

        } catch (Exception e) {
            StudioUtil.showError(e);
        }

        return names;
    }

    private static boolean isComplete(Map<String, String> properties) {
        return !(properties.get("name") == null || properties.get("driver") == null || properties.get("url") == null | properties.get("user") == null);
    }

    private static void renderConfigLink(A a, Map<String, String> properties) {
        if (isComplete(properties)) {
            a.setLabel("Complete");
            a.setStyle("color:#419641");
        } else {
            a.setLabel("Missing");
            a.setStyle("color:#b94a48");
        }
    }

    private static void renderManagedClasses(String name, Listbox punitList, Listbox managedList) {
        punitList.clearSelection();
        managedList.getItems().clear();
        Clients.evalJavaScript("jq('$jpacontroller .badge').remove()");

        EntityManagerFactory emf = JpaUtil.getEntityManagerFactory(name);
        if (emf == null) return;

        Metamodel metamodel = emf.getMetamodel();
        SortedSet<EntityType> entitiesSortedSet = new TreeSet<>(ENTITY_SORTER_INSTANCE);
        entitiesSortedSet.addAll(metamodel.getEntities());
        for (EntityType<?> entityType : entitiesSortedSet) {
            Listitem listitem = new Listitem();
            listitem.setParent(managedList);

            Listcell listcell = new Listcell(entityType.getJavaType().getCanonicalName());
            listcell.setParent(listitem);

//                A a = new A(entityType.getJavaType().getCanonicalName());
//                a.setParent(listcell);
//                a.setAttribute("entityType", entityType);
//                a.setSclass("jpa-managed-class");
//                    a.addEventListener(Events.ON_CLICK, managedClassClickHandler);
        }

        for (Listitem item : punitList.getItems()) {
            if (name.equals(item.getAttribute("name"))) {
                item.setSelected(true);
            }
        }

        Clients.evalJavaScript("jq(\"$jpacontroller .z-center-header\").append('<span class=\"badge\" style=\"margin-left:10px\">" + metamodel.getEntities().size() + "</span>')");
    }

    private void renderState(String name, Hlayout hlayout, A configLink) {
        hlayout.getChildren().clear();
        boolean started = JpaUtil.getEntityManagerFactory(name) != null;
        Label state = new Label(started ? "Started" : "Stopped");
        state.setSclass("label label-" + (started ? "success" : "default"));
        state.setParent(hlayout);
        if (!started) {
            A a = new A("Start?");
            a.setParent(hlayout);
            a.addEventListener(Events.ON_CLICK, new StartStopEMFHandler(name, hlayout, true, configLink));
            a.setWidgetListener(Events.ON_CLICK, "zAu.cmd0.showBusy();");
        } else {
            A a = new A("Stop?");
            a.setParent(hlayout);
            a.addEventListener(Events.ON_CLICK, new StartStopEMFHandler(name, hlayout, false, configLink));
            a.setWidgetListener(Events.ON_CLICK, "zAu.cmd0.showBusy();");
        }
        renderManagedClasses(name, punitList, managedList);
    }

    @Listen("onJpaScan=#jpacontroller")
    public void onJpaScan() {
        if (!punitList.getEventListeners(Events.ON_SELECT).iterator().hasNext()) {
            punitList.addEventListener(Events.ON_SELECT, new OnPunitSelectedHandler());
        }

        punitList.getItems().clear();
        managedList.getItems().clear();
        Clients.evalJavaScript("jq('$jpacontroller .badge').remove()");
        Clients.clearBusy();

        SortedMap<String, Map<String, String>> units = getPersistenceUnitNames();
        if (units.isEmpty()) {
            StudioUtil.showNotification("warning", "No JPA", "Sorry but your project does not contain any JPA persistence units.", true);
            return;
        }
        for (String unit : units.keySet()) {
            String name = unit.split("\\|")[0];
            String url = unit.split("\\|")[1];

            Listitem listitem = new Listitem();
            listitem.setAttribute("name", name);
            new Listcell(name).setParent(listitem);

            Listcell cell = new Listcell();
            cell.setParent(listitem);
            cell.setStyle("text-align:center");
            A a = new A();
            a.addEventListener(Events.ON_CLICK, new ConnInfoConfigClickHandler(name));
            a.setParent(cell);
            renderConfigLink(a, units.get(unit));

            cell = new Listcell();
            cell.setParent(listitem);
            cell.setStyle("text-align:center");
            Hlayout hlayout = new Hlayout();
            hlayout.setParent(cell);
            hlayout.setSpacing("5px");
            renderState(name, hlayout, a);
            new Listcell(url).setParent(listitem);

            listitem.setParent(punitList);

        }
    }

    private class StartStopEMFHandler implements EventListener<MouseEvent> {
        private String name;
        private Hlayout hlayout;
        private boolean start;
        private A configLink;

        public StartStopEMFHandler(String name, Hlayout hlayout, boolean start, A configLink) {
            this.name = name;
            this.hlayout = hlayout;
            this.start = start;
            this.configLink = configLink;
        }

        @Override
        public void onEvent(MouseEvent event) throws Exception {
            Clients.clearBusy();
            if (start) {
                Map<String, String> properties = JpaUtil.getConnectionProperties(name);
                if (!isComplete(properties)) {
                    StudioUtil.showPopover(configLink.getUuid(), "error", "<strong>Incomplete connection info.</strong> Fill in the connection info to continue.", false);
                    return;
                }

                Map<String, String> props = new HashMap<>();
                props.put("javax.persistence.jdbc.driver", properties.get("driver"));
                props.put("javax.persistence.jdbc.url", properties.get("url"));
                props.put("javax.persistence.jdbc.user", properties.get("user"));
                props.put("javax.persistence.jdbc.password", properties.get("password"));
                try {
                    EntityManagerFactory emf = Persistence.createEntityManagerFactory(name, props);
                    JpaUtil.setEntityManagerFactory(name, emf);
                    renderState(name, hlayout, configLink);
                } catch (Exception e) {
                    e.printStackTrace();
                    StudioUtil.showError(e);
                }
            } else {
                EntityManagerFactory emf = JpaUtil.getEntityManagerFactory(name);
                if (emf != null && emf.isOpen()) {
                    emf.close();
                }
                JpaUtil.removeEntityManagerFactory(name);
                renderState(name, hlayout, configLink);
            }
        }
    }

    private class ConnInfoConfigClickHandler implements EventListener<Event> {
        private String name;

        public ConnInfoConfigClickHandler(String name) {
            this.name = name;
        }

        @Override
        public void onEvent(Event event) throws Exception {
            A a = (A) event.getTarget();
            Map<String, String> properties = JpaUtil.getConnectionProperties(name);

            if ("onConfigChanged".equals(event.getName())) {
                renderConfigLink(a, properties);
            } else {
                Panel panel;
                try {
                    panel = (Panel) Executions.getCurrent().createComponents("~./include/jpaconninfo.zul", null, properties);
                } catch (Exception e) {
                    //this will happen if the user clicks on the link while another popover is open, ignore.
                    return;
                }
                panel.setAttribute("name", name);
                panel.setAttribute("target", a);
                panel.setAttribute("callback", this);
                Clients.evalJavaScript("showInPopover('" + a.getUuid() + "','" + panel.getUuid() + "')");

            }
        }
    }

    private class OnPunitSelectedHandler implements EventListener<SelectEvent<Listitem, ?>> {

        @Override
        public void onEvent(SelectEvent<Listitem, ?> event) throws Exception {
            Listitem selectedItem = event.getSelectedItems().iterator().next();
            String name = selectedItem.getAttribute("name").toString();
            renderManagedClasses(name, punitList, managedList);
        }
    }
}
