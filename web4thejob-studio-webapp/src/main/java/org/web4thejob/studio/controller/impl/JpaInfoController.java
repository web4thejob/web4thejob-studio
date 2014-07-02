package org.web4thejob.studio.controller.impl;

import nu.xom.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
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
import javax.persistence.metamodel.SingularAttribute;
import java.util.*;

import static org.zkoss.lang.Generics.cast;


/**
 * Created by e36132 on 16/4/2014.
 */
public class JpaInfoController extends SelectorComposer<Component> {
    private static AttributeComparator attributeComparator = new AttributeComparator();
    private static EntityComparator entityComparator = new EntityComparator();
    @Wire
    Div jpacontroller;
    @Wire
    Listbox punitList;
    @Wire
    Listbox managedList;


    private static SortedMap<String, Map<String, String>> getPersistenceUnitNames() {
        SortedMap<String, Map<String, String>> names = new TreeMap<>();


        try {
            XPathContext ctx = new XPathContext("p", "http://java.sun.com/xml/ns/persistence");
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            for (Resource resource : resolver.getResources("classpath*:META-INF/persistence.xml")) {
                Builder parser = new Builder(false);
                Document document = parser.build(resource.getInputStream());
                Nodes nodes = document.query("p:persistence/p:persistence-unit", ctx);
                if (nodes.size() == 1) {
                    String name = ((Element) nodes.get(0)).getAttributeValue("name");

                    Map<String, String> properties = getConnectionProperties(name);
                    if (properties == null) {
                        properties = new HashMap<>();
                        setConnectionProperties(name, properties);
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

                    names.put(name + "|" + resource.getURL(), properties);

                }
            }

        } catch (Exception e) {
            StudioUtil.showError(e);
        }

        return names;
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory(String name) {
        Session session = Executions.getCurrent().getSession();
        Map<String, EntityManagerFactory> emfs = cast(session.getAttribute("w4tjstudio-emfs"));
        if (emfs != null) {
            return emfs.get(name);
        }
        return null;
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

    private static boolean isComplete(Map<String, String> properties) {
        return !(properties.get("name") == null || properties.get("driver") == null || properties.get("url") == null | properties.get("user") == null);
    }

    private static void renderConfigLink(A a, Map<String, String> properties) {
        if (isComplete(properties)) {
            a.setLabel("Complete");
            a.setStyle("color:#419641");
        } else {
            a.setLabel("Missing");
            a.setStyle("color:red");
        }
    }

    private static void renderManagedClasses(String name, Listbox punitList, Listbox managedList) {
        punitList.clearSelection();
        managedList.getItems().clear();
        Clients.evalJavaScript("jq('$jpacontroller .badge').remove()");

        EntityManagerFactory emf = getEntityManagerFactory(name);
        if (emf == null) return;

        Metamodel metamodel = emf.getMetamodel();
        SortedSet<EntityType> entitiesSortedSet = new TreeSet<>(entityComparator);
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

    private void renderState(String name, Hlayout hlayout) {
        hlayout.getChildren().clear();
        boolean started = getEntityManagerFactory(name) != null;
        Label state = new Label(started ? "Started" : "Stopped");
        state.setSclass("label label-" + (started ? "success" : "default"));
        state.setParent(hlayout);
        if (!started) {
            A a = new A("Start?");
            a.setParent(hlayout);
            a.addEventListener(Events.ON_CLICK, new StartStopEMFHandler(name, hlayout, true));
            a.setWidgetListener(Events.ON_CLICK, "zAu.cmd0.showBusy();");
        } else {
            A a = new A("Stop?");
            a.setParent(hlayout);
            a.addEventListener(Events.ON_CLICK, new StartStopEMFHandler(name, hlayout, false));
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
            renderState(name, hlayout);
            new Listcell(url).setParent(listitem);

            listitem.setParent(punitList);

        }
    }

    private static class ManagedClassClickHandler implements EventListener<MouseEvent> {

        @Override
        public void onEvent(MouseEvent event) throws Exception {
            EntityType<?> entityType = (EntityType<?>) event.getTarget().getAttribute("entityType");
            SingularAttribute key = null;
            if (entityType.hasSingleIdAttribute()) {
                key = entityType.getId(entityType.getIdType().getJavaType());
            }

            String id = "jpa-" + entityType.getJavaType().getCanonicalName();
            id = id.replaceAll("\\.", "");
            Component comp = null;
            for (Component c : Executions.getCurrent().getDesktop().getComponents()) {
                if (id.equals(c.getId())) {
                    comp = c;
                    break;
                }
            }
            if (comp != null) comp.detach();

            Map<String, Object> args = new HashMap<>();
            args.put("id", id);
            args.put("name", entityType.getName());
            args.put("attrsize", entityType.getAttributes().size());
            Window window = (Window) Executions.getCurrent().createComponents("/includes/data.zul", null, args);

            Tree tree = (Tree) window.getFirstChild().getFellow("entityAttributesTree");
            tree.getTreechildren().getChildren().clear();

            SortedSet<javax.persistence.metamodel.Attribute> attributeSortedSet = new TreeSet<>(attributeComparator);
            attributeSortedSet.addAll(entityType.getAttributes());
            for (javax.persistence.metamodel.Attribute attribute : attributeSortedSet) {

                Treeitem treeitem = new Treeitem();
                treeitem.setParent(tree.getTreechildren());
                Treerow treerow = new Treerow();
                treerow.setParent(treeitem);
                Treecell treecell = new Treecell();
                treecell.setAttribute("attribute", attribute);
                treecell.setSclass("jpa-attribute");
                treecell.setParent(treerow);
                Html html = new Html();
                html.setContent("<span class=\"label label-primary\">" + attribute.getName() + "</span>");
                html.setParent(treecell);

                if (!attribute.equals(key)) {
                    new Treecell().setParent(treerow);
                } else {
                    Treecell keycell = new Treecell();
                    keycell.setParent(treerow);
                    keycell.setIconSclass("z-icon-key");
                }

                new Treecell(attribute.getJavaType().getName()).setParent(treerow);

                new Treechildren().setParent(treeitem);
                treeitem.setOpen(false);
                for (int i = 1; i <= 3; i++) {
                    String bindType = "";
                    switch (i) {
                        case 1:
                            bindType = "@load";
                            break;
                        case 2:
                            bindType = "@save";
                            break;
                        case 3:
                            bindType = "@bind";
                            break;
                    }

                    Treeitem binditem = new Treeitem();
                    binditem.setParent(treeitem.getTreechildren());
                    treerow = new Treerow();
                    treerow.setParent(binditem);
                    treecell = new Treecell();
                    treecell.setAttribute("attribute", attribute);
                    treecell.setAttribute("bindType", bindType);
                    treecell.setSclass("jpa-bindtype");
                    treecell.setParent(treerow);
                    html = new Html("<span class=\"label label-success\">" + bindType + "</span>");
                    html.setParent(treecell);

                    new Treecell().setParent(treerow);
                    new Treecell().setParent(treerow);
                }

            }

            Clients.evalJavaScript("prepareDataToolbox('" + id + "')");
        }
    }

    public static class AttributeComparator implements Comparator<javax.persistence.metamodel.Attribute> {

        @Override
        public int compare(javax.persistence.metamodel.Attribute o1, javax.persistence.metamodel.Attribute o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    public static class EntityComparator implements Comparator<EntityType> {

        @Override
        public int compare(EntityType o1, EntityType o2) {
            return o1.getJavaType().getCanonicalName().compareTo(o2.getJavaType().getCanonicalName());
        }
    }

    private class StartStopEMFHandler implements EventListener<MouseEvent> {
        private String name;
        private Hlayout hlayout;
        private boolean start;


        public StartStopEMFHandler(String name, Hlayout hlayout, boolean start) {
            this.name = name;
            this.hlayout = hlayout;
            this.start = start;
        }

        @Override
        public void onEvent(MouseEvent event) throws Exception {
            Clients.clearBusy();
            if (start) {
                Map<String, String> properties = getConnectionProperties(name);
                if (!isComplete(properties)) {
                    StudioUtil.showNotification("warning", "Incomplete connection info.", "Fill in the connection info to continue.", true);
                    return;
                }

                Map<String, String> props = new HashMap<>();
                props.put("javax.persistence.jdbc.driver", properties.get("driver"));
                props.put("javax.persistence.jdbc.url", properties.get("url"));
                props.put("javax.persistence.jdbc.user", properties.get("user"));
                props.put("javax.persistence.jdbc.password", properties.get("password"));
                try {
                    EntityManagerFactory emf = Persistence.createEntityManagerFactory(name, props);
                    setEntityManagerFactory(name, emf);
                    renderState(name, hlayout);
                } catch (Exception e) {
                    e.printStackTrace();
                    StudioUtil.showError(e);
                }
            } else {
                EntityManagerFactory emf = getEntityManagerFactory(name);
                if (emf != null && emf.isOpen()) {
                    emf.close();
                    removeEntityManagerFactory(name);
                    renderState(name, hlayout);
                }
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
            Map<String, String> properties = getConnectionProperties(name);

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
