package org.web4thejob.studio;

import nu.xom.Element;
import org.web4thejob.studio.message.Message;
import org.web4thejob.studio.support.AbstractController;
import org.web4thejob.studio.support.ChildDelegate;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import java.util.Map;

import static org.web4thejob.studio.ControllerEnum.OUTLINE_CONTROLLER;
import static org.web4thejob.studio.message.MessageEnum.COMPONENT_SELECTED;
import static org.web4thejob.studio.support.StudioUtil.*;

/**
 * Created by e36132 on 15/5/2014.
 */
public class OutlineController extends AbstractController {
    private static OutlineClickHandler outlineClickHandler;
    private static DroppableHandler droppableHandler;

    @Wire
    private Tree outline;
    private Element selection;


    {
        outlineClickHandler = new OutlineClickHandler();
        droppableHandler = new DroppableHandler();
    }

    private static Treeitem findTreeitemParent(Component child) {
        if (child == null) return null;

        if (child instanceof Treeitem) {
            return (Treeitem) child;
        } else {
            return findTreeitemParent(child.getParent());
        }
    }

    @Override
    public ControllerEnum getId() {
        return OUTLINE_CONTROLLER;
    }

    public void refresh() {
        reset();
        Element zk = getCode().getRootElement();
        for (int i = 0; i < zk.getChildElements().size(); i++) {
            //TODO check for baseGroup elements like <attribute>, <custom-attributes> etc
            includeComponent(zk.getChildElements().get(i));
        }

        selectItem(selection);
    }

    private Treeitem toTreeitem(Element element) {
        Treeitem item = new Treeitem();
        Treerow treerow = new Treerow();
        treerow.setParent(item);
        //Supports the d 'n d of templates in outline view in the same way as in canvas
        treerow.setWidgetAttribute("canvas-uuid", element.getAttributeValue("uuid"));

        Treecell cell = new Treecell();
        cell.setStyle("white-space: nowrap;");
        String i = "/img/zul/" + element.getLocalName() + ".png";
        cell.setImage("/w4tjstudio-support/img?f=" + element.getLocalName() + ".png");
        cell.setParent(item.getTreerow());

        Html html = new Html(describeElement(element));
        html.setStyle("margin-left: 5px;");
        html.setParent(cell);

        item.setValue(element);
        item.setDraggable("true");
        item.setDroppable("true");
        item.addEventListener(Events.ON_CLICK, outlineClickHandler);
        item.addEventListener(Events.ON_DROP, droppableHandler);

        return item;
    }

    @Override
    public void process(Message message) {
        switch (message.getId()) {
            case COMPONENT_SELECTED:
                selectItem((Element) message.getData());
                break;
            case COMPONENT_ADDED:
                includeComponent(getElementByUuid((String) message.getData()));
                break;
            case RESET:
                reset();
                selection = null;
                break;
            case ZUL_EVAL_SUCCEEDED:
                refresh();
                break;
            case COMPONENT_DETACHED:
                removeItem((Element) message.getData());
                break;
            case XML_EVAL_FAILED:
                outline.clear();
                break;
            case ZUL_EVAL_FAILED:
                outline.clear();
                break;
        }
    }

    private void includeComponent(Element element) {
        traverseChildren(element, null, new ChildDelegate<Element>() {

            @Override
            public void onChild(Element child, Map<String, Object> params) {
                Treeitem item = toTreeitem(child);

                if (child.getParent() instanceof Element && !child.getParent().equals(getCode().getRootElement())) {
                    Treeitem parent = getTreeitemByElement((Element) child.getParent());
                    if (parent.getTreechildren() == null) {
                        new Treechildren().setParent(parent);
                    }
                    item.setParent(parent.getTreechildren());
                } else {
                    Treeitem parent = (Treeitem) outline.getTreechildren().getFirstChild();
                    if (parent.getTreechildren() == null) {
                        new Treechildren().setParent(parent);
                    }
                    item.setParent(parent.getTreechildren());
                }
            }
        });
    }

    private Treeitem getTreeitemByElement(Element element) {
        for (Treeitem item : outline.getItems()) {
            if (element.equals((item.getValue()))) {
                return item;
            }
        }
        return null;
    }

    private void removeItem(Element element) {
        getTreeitemByElement(element).detach();
    }

    public void selectItem(Element element) {
        selection = element;
        outline.setSelectedItem(null);
        if (element != null) {
            getTreeitemByElement(element).setSelected(true);
        }
    }

    public void reset() {
        outline.clear();
        Element zk = getCode().getRootElement();
        Treeitem root = new Treeitem();
        Treerow treerow = new Treerow();
        treerow.setParent(root);
        //Supports the d 'n d of templates in outline view in the same way as in canvas
        treerow.setWidgetAttribute("canvas-uuid", zk.getAttributeValue("uuid"));
        Treecell cell = new Treecell();
        cell.setParent(root.getTreerow());
        Html html = new Html("Canvas");
        html.setStyle("margin-left: 5px;");
        html.setParent(cell);
        cell.setImage("/w4tjstudio-support/img?f=window.png");

        root.setParent(outline.getTreechildren());
        root.setValue(zk);
        root.addEventListener(Events.ON_CLICK, outlineClickHandler);
        root.addEventListener(Events.ON_DROP, droppableHandler);
        root.setDroppable("true");
    }

    private class DroppableHandler implements EventListener<DropEvent> {

        @Override
        public void onEvent(DropEvent event) throws Exception {
            Element dragged = ((Treeitem) event.getDragged()).getValue();
            if (dragged.getAttributeValue("uuid") == null) return;
            Element dropped = ((Treeitem) event.getTarget()).getValue();
            if (dropped.getAttributeValue("uuid") == null) return;

//            Component draggedComp = getCanvasComponentByUuid(dragged.getAttributeValue("uuid"));
//            Component droppedComp = getCanvasComponentByUuid(dropped.getAttributeValue("uuid"));
//            if (droppedComp.equals(draggedComp.getParent())) return;
//            draggedComp.setParent(droppedComp);
//
//            dragged.detach();
//            dropped.appendChild(dragged);
//
//            Treeitem draggedItem = (Treeitem) event.getDragged();
//            Treeitem droppedItem = (Treeitem) event.getTarget();
//            if (droppedItem.getTreechildren() == null) {
//                new Treechildren().setParent(droppedItem);
//            }
//            draggedItem.setParent(droppedItem.getTreechildren());
//            draggedItem.setSelected(true);
//
//            publish(SET_BOOKMARK);
        }
    }

    private class OutlineClickHandler implements EventListener<MouseEvent> {

        @Override
        public void onEvent(MouseEvent event) throws Exception {
            Element element = ((Treeitem) event.getTarget()).getValue();
            publish(COMPONENT_SELECTED, element);
        }
    }
}
