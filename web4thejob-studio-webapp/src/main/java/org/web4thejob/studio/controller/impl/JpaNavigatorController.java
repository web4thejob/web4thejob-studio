package org.web4thejob.studio.controller.impl;

import org.web4thejob.studio.support.JpaUtil;
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
    private static ManagedClassClickHandler manegedClassClick = new ManagedClassClickHandler();
    @Wire
    private Tree entityTree;

    @Override
    public void doAfterCompose(Tree comp) throws Exception {
        super.doAfterCompose(comp);
        populate();
    }

    private void populate() {
        entityTree.clear();
        Map<String, EntityManagerFactory> emfs = JpaUtil.getEntityManagerFactories();
        if (emfs == null) return;

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
        treecell.setIconSclass("fa fa-database");
        treecell.setParent(treerow);
        Label label = new Label(name);
        label.setParent(treecell);
        label.setSclass("label label-default");
        label.setStyle("margin-left: 5px");

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
        A a = new A(entityType.getJavaType().getCanonicalName());
        a.setParent(treecell);
        a.setAttribute("entityType", entityType);
        a.addEventListener(Events.ON_CLICK, manegedClassClick);

    }

    private static class ManagedClassClickHandler implements EventListener<MouseEvent> {

        @Override
        public void onEvent(MouseEvent event) throws Exception {

        }
    }
}
