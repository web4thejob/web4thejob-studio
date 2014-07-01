package org.web4thejob.studio.controller.impl;

import nu.xom.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.event.SelectEvent;
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
                    //EntityManagerFactory emf = Persistence.createEntityManagerFactory(name);
                    //names.put(name + "|" + resource.getURL(), emf);

                    Map<String, String> properties = new HashMap<>();
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


                    names.put(name + "|" + resource.getURL(), properties);
                }
            }

        } catch (Exception e) {
            StudioUtil.showError(e);
        }

        return names;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        init();
    }

    protected void init() {

        punitList.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<Listitem, Listitem>>() {
            @Override
            public void onEvent(SelectEvent<Listitem, Listitem> event) throws Exception {
                managedList.getItems().clear();
                Clients.evalJavaScript("jq('$jpacontroller .badge').remove()");

                Listitem selectedItem = event.getSelectedItems().iterator().next();
                Map<String, String> properties = cast(((Component) selectedItem.getAttribute("properties-holder")).getAttribute("properties"));
                String name = properties.get("name");
                String driver = properties.get("driver");
                String url = properties.get("url");
                String user = properties.get("user");
                String password = properties.get("password");
                if (name == null || driver == null || url == null | user == null) return;

                Map props = new HashMap();
                props.put("javax.persistence.jdbc.driver", driver);
                props.put("javax.persistence.jdbc.url", url);
                props.put("javax.persistence.jdbc.user", user);
                props.put("javax.persistence.jdbc.password", password);
                EntityManagerFactory emf = Persistence.createEntityManagerFactory(name, props);
                Metamodel metamodel = emf.getMetamodel();

                SortedSet<EntityType> entitiesSortedSet = new TreeSet<>(entityComparator);
                entitiesSortedSet.addAll(metamodel.getEntities());
                for (EntityType<?> entityType : entitiesSortedSet) {
                    Listitem listitem = new Listitem();
                    listitem.setParent(managedList);

                    Listcell listcell = new Listcell();
                    listcell.setParent(listitem);

                    A a = new A(entityType.getJavaType().getCanonicalName());
                    a.setParent(listcell);
                    a.setAttribute("entityType", entityType);
                    a.setSclass("jpa-managed-class");
//                    a.addEventListener(Events.ON_CLICK, managedClassClickHandler);
                }

                Clients.evalJavaScript("jq(\"$jpacontroller .z-center-header\").append('<span class=\"badge\" style=\"margin-left:10px\">" + metamodel.getEntities().size() + "</span>')");
            }
        });
    }

    @Listen("onJpaScan=#jpacontroller")
    public void onJpaScan() {
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

            Map<String, String> properties = units.get(unit);
            properties.put("name", name);

            Listitem listitem = new Listitem();
            new Listcell(name).setParent(listitem);

            Listcell cell = new Listcell();
            cell.setParent(listitem);
            cell.setStyle("text-align:center");

            A a = new A();
            listitem.setAttribute("properties-holder", a);
            a.setAttribute("properties", properties);
            a.addEventListener(Events.ON_CLICK, new ConnInfoConfigClickHandler());
            a.setParent(cell);
            if (units.get(unit).size() >= 4) {
                a.setLabel("Complete");
            } else {
                a.setLabel("Missing");
                a.setStyle("color:red");
            }


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

    private class ConnInfoConfigClickHandler implements EventListener<MouseEvent> {

        @Override
        public void onEvent(MouseEvent event) throws Exception {
            Map<String, String> properties = cast(event.getTarget().getAttribute("properties"));

            Panel panel = (Panel) Executions.getCurrent().createComponents("/jpaconninfo.zul", null, properties);
            panel.setAttribute("target", event.getTarget());
            Clients.evalJavaScript("showInPopover('" + event.getTarget().getUuid() + "','" + panel.getUuid() + "')");

        }
    }

}
