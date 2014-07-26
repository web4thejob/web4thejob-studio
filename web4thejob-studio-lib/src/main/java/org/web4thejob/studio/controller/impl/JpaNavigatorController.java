package org.web4thejob.studio.controller.impl;

import org.web4thejob.studio.support.JpaUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.util.*;

import static org.web4thejob.studio.support.JpaUtil.ENTITY_SORTER_INSTANCE;

/**
 * Created by e36132 on 3/7/2014.
 */
public class JpaNavigatorController extends SelectorComposer<Tree> {
    private static ManagedClassClickHandler managedClassClick = new ManagedClassClickHandler();
    @Wire
    private Tree entityTree;

    @Override
    public void doAfterCompose(Tree comp) throws Exception {
        super.doAfterCompose(comp);
        populate();
    }

    private void populate() {
        Map<String, EntityManagerFactory> emfs = JpaUtil.getEntityManagerFactories();
        if (emfs == null) {
            Executions.createComponents("~./include/nojpa.zul", entityTree.getParent(), null);
            entityTree.detach();
            return;
        }

        entityTree.clear();
        List<String> names = new ArrayList<>(emfs.keySet());
        Collections.sort(names);
        for (String name : names) {
            renderPersistenceUnit(name, emfs.get(name));
        }
    }

    private void renderPersistenceUnit(String name, EntityManagerFactory emf) {
        Treeitem treeitem = new Treeitem();
        treeitem.setParent(entityTree.getTreechildren());
        Treerow treerow = new Treerow();
        treerow.setParent(treeitem);
        Treecell treecell = new Treecell();
        treecell.setSclass("punit");
        treecell.setParent(treerow);
        Html html = new Html();
        html.setParent(treecell);
        html.setContent("<span class=\"jpa-joblet label label-default\"><i class=\"fa fa-database\" style=\"margin-right:5px\"></i>" + name + "</span>");
        //html.setStyle("margin-left: 5px");

        Metamodel metamodel = emf.getMetamodel();
        SortedSet<EntityType> entitiesSortedSet = new TreeSet<>(ENTITY_SORTER_INSTANCE);
        entitiesSortedSet.addAll(metamodel.getEntities());
        for (EntityType<?> entityType : entitiesSortedSet) {
            renderEntity(treeitem, entityType);
        }

    }

    private void renderEntity(Treeitem parent, EntityType<?> entityType) {
        if (parent.getTreechildren() == null) {
            new Treechildren().setParent(parent);
        }

        Treeitem treeitem = new Treeitem();
        treeitem.setParent(parent.getTreechildren());
        Treerow treerow = new Treerow();
        treerow.setParent(treeitem);
        Treecell treecell = new Treecell();
        treecell.setParent(treerow);
        treecell.setStyle("white-space:nowrap");
        A a = new A(entityType.getJavaType().getSimpleName());
        a.setTooltiptext(entityType.getJavaType().getCanonicalName());
        a.setParent(treecell);
        a.setAttribute("entityType", entityType);
        a.addEventListener(Events.ON_CLICK, managedClassClick);

    }

    private static class ManagedClassClickHandler implements EventListener<MouseEvent> {

        @Override
        public void onEvent(MouseEvent event) throws Exception {
            EntityType<?> entityType = (EntityType<?>) event.getTarget().getAttribute("entityType");
            if (entityType == null) return;

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

            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("entityType", entityType);
            Executions.createComponents("~./include/jpaentitybox.zul", null, data);
        }
    }
}

