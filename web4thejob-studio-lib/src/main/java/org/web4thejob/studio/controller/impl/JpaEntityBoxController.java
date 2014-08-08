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

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by Veniamin on 7/7/2014.
 */
public class JpaEntityBoxController extends SelectorComposer<Panel> {
    private static AttributeComparator attributeComparator = new AttributeComparator();

    private Panel self;
    private Tree entityAttributesTree;

    @Override
    public void doAfterCompose(Panel comp) throws Exception {
        super.doAfterCompose(comp);
        self = comp;
        entityAttributesTree = (Tree) self.getPanelchildren().getFirstChild();
        entityAttributesTree.clear();

        EntityType entityType = (EntityType) Executions.getCurrent().getArg().get("entityType");
        if (entityType == null) return;

        String id = Executions.getCurrent().getArg().get("id").toString();
        self.setTitle(entityType.getJavaType().getSimpleName());
        Clients.evalJavaScript("top.w4tjStudioDesigner.prepareEntityToolbox('" + id + "')");

        render(entityType);
    }

    private void render(EntityType entityType) {
        SingularAttribute key = null;
        if (entityType.hasSingleIdAttribute()) {
            key = entityType.getId(entityType.getIdType().getJavaType());
        }
        SortedSet<Attribute> attributeSortedSet = new TreeSet<>(attributeComparator);
        attributeSortedSet.addAll(entityType.getAttributes());
        for (javax.persistence.metamodel.Attribute attribute : attributeSortedSet) {

            Treeitem treeitem = new Treeitem();
            treeitem.setParent(entityAttributesTree.getTreechildren());
            Treerow treerow = new Treerow();
            treerow.setParent(treeitem);
            Treecell treecell = new Treecell();
            treecell.setTooltiptext(attribute.getJavaType().getName());
            treecell.setAttribute("attribute", attribute);
            treecell.setStyle("white-space:nowrap;");
            treecell.setParent(treerow);
            Html html = new Html();
            String keyHtml = attribute.equals(key) ? "&nbsp;&nbsp;&nbsp;<i class=\"fa fa-key\" style=\"margin-left:0px\"></i>" : "";
            html.setContent("<span class=\"jpa-attribute label label-default\">" + attribute.getName() + keyHtml + "</span>");
            html.setParent(treecell);

//            if (!attribute.equals(key)) {
//                new Treecell().setParent(treerow);
//            } else {
//                Treecell keycell = new Treecell();
//                keycell.setParent(treerow);
//                keycell.setIconSclass("z-icon-key");
//                keycell.setStyle("text-align:center");
//            }

//            new Treecell(attribute.getJavaType().getName()).setParent(treerow);

            new Treechildren().setParent(treeitem);
            treeitem.setOpen(false);
            for (int i = 1; i <= 3; i++) {
                String bindType = "";
                switch (i) {
                    case 1:
                        bindType = "@bind";
                        break;
                    case 2:
                        bindType = "@load";
                        break;
                    case 3:
                        bindType = "@save";
                        break;
                }

                Treeitem binditem = new Treeitem();
                binditem.setParent(treeitem.getTreechildren());
                treerow = new Treerow();
                treerow.setParent(binditem);
                treecell = new Treecell();
                treecell.setAttribute("attribute", attribute);
                treecell.setAttribute("bindType", bindType);
                treecell.setSclass("");
                treecell.setParent(treerow);
                String vm = bindType + "(vm." + attribute.getName() + ")";
                html = new Html("<span bind-data=\"" + vm + "\" class=\"jpa-bindtype label label-default\"><i class=\"fa fa-hand-o-right\" style=\"margin-right:3px\"/>" + bindType + "</span>");
                html.setParent(treecell);

//                new Treecell().setParent(treerow);
//                new Treecell().setParent(treerow);
            }

        }

        // Clients.evalJavaScript("prepareDataToolbox('" + id + "')");


    }

    public static class AttributeComparator implements Comparator<Attribute> {

        @Override
        public int compare(javax.persistence.metamodel.Attribute o1, javax.persistence.metamodel.Attribute o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

}
