package org.web4thejob.studio.controller.impl;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import org.apache.commons.lang.ClassUtils;
import org.web4thejob.studio.controller.AbstractController;
import org.web4thejob.studio.controller.ControllerEnum;
import org.web4thejob.studio.message.Message;
import org.web4thejob.studio.support.ChildDelegate;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.json.JSONObject;
import org.zkoss.util.resource.Locators;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Textbox;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import static org.web4thejob.studio.message.MessageEnum.*;
import static org.web4thejob.studio.support.StudioUtil.*;
import static org.web4thejob.studio.support.ZulXsdUtil.ZUL_NS;
import static org.web4thejob.studio.support.ZulXsdUtil.getWidgetDescription;

/**
 * Created by e36132 on 14/5/2014.
 */
public class CodeController extends AbstractController {
    @Wire
    private Textbox zulBox;
    private boolean changed;
    private Document document;


    @Override
    public ControllerEnum getId() {
        return ControllerEnum.CODE_CONTROLLER;
    }

    public void reset(String file) {
        Builder parser = new Builder(false);
        try {
            document = parser.build(new FileInputStream(file), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        changed = false;
        print();
    }

    public Document getCode() {
        if (changed) {
            Builder parser = new Builder(false);
            try {
                document = parser.build(zulBox.getValue(), null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            changed = false;
        }
        return document;
    }

    public void print() {
        if (document == null) return;
        final Document doc = (Document) document.copy();
        //TODO activate for production
//        cleanUUIDs(doc.getRootElement());

        zulBox.setValue(StudioUtil.beautifyXml(doc));
        Clients.evalJavaScript("myCodeMirror.refresh()");
    }

    @Listen("onChange=#zulBox;")
    public void codeChanged() {
        if (!changed) {
            publish(COMPONENT_SELECTED);
            publish(CODE_CHANGED);
        }
        changed = true;
    }

    public boolean isChanged() {
        return changed;
    }

    private void includeComponent(String uuid) {
        Component component = getCanvasComponentByUuid(uuid);
        Element element = toElement(component);

        if (component.getParent() != null && !component.getParent().getUuid().equals(getCanvasUuid())) {
            String parentUuid = component.getParent().getUuid();
            getElementByUuid(parentUuid).appendChild(element);
        } else {
            document.getRootElement().appendChild(element);
        }
        print();
    }

    private Element toElement(Component component) {
        Map<String, Object> params = new HashMap<>();
        traverseChildren(component, params, new ChildDelegate<Component>() {
            @Override
            public void onChild(Component child, Map<String, Object> params) {
                if (ClassUtils.isInnerClass(child.getClass())) return;

                Element element = new Element(child.getDefinition().getName(), ZUL_NS);
                element.addAttribute(new Attribute("uuid", child.getUuid()));
                Element parent = (Element) params.get("parent");
                if (parent != null) {
                    parent.appendChild(element);
                } else {
                    params.put("element", element); //this is the top element
                }
                params.put("parent", element);


                SortedMap<String, SortedSet<Element>> propsMap = getWidgetDescription(child.getDefinition()
                        .getName());
                for (String group : propsMap.keySet()) {
                    for (Element property : propsMap.get(group)) {
                        String propertyName = property.getAttributeValue("name");
                        String type = property.getAttributeValue("type");
                        boolean isBoolean = "booleanType".equals(type);
//                        if (isBannedProperty(child.getClass(), propertyName, isBoolean)) continue;

                        if (hasProperty(child.getClass(), propertyName, isBoolean)) {
                            Object value = null;

                            try {
                                value = invokeGetter(child, propertyName, isBoolean);
                            } catch (Exception e) {
                                //do nothing, probably just wrong state like when you call pageSize on grid without
                                // paging mold
                            }

                            if (value != null && !isDefaultValueForProperty(child, propertyName, value.toString(),
                                    isBoolean) && isEligibleTypeForXml(value.getClass())) {
                                element.addAttribute(new Attribute(propertyName, value.toString()));
                            }
                        }
                    }
                }


            }
        });

        return (Element) params.get("element");
    }

    @Override
    public void process(Message message) {
        switch (message.getId()) {
            case COMPONENT_ADDED:
                JSONObject data = message.getData();
                Element parent = getElementByUuid((String) data.get("parent"));
                String template = ((String) data.get("template")).replace("~./", "web/");

                try {
                    Element target = new Builder(false).build(Locators.getDefault().getResourceAsStream(template), "UTF-8").getRootElement();
                    if ("zk".equals(target.getLocalName())) {
                        for (int i = 0; i < target.getChildCount(); i++) {
                            parent.appendChild(target.getChild(i).copy());
                        }
                    } else {
                        parent.appendChild(target.copy());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

                publish(EVALUATE_ZUL, COMPONENT_ADDED);
//                includeComponent(message.getData(String.class));
                //addBookmark();
                break;
            case RESET:
                reset((String) message.getData());
                break;
            case COMPONENT_DETACHED:
                Element element = message.getData();
                element.detach();
                print();
                //addBookmark();
                break;
            case EVALUATE_XML:
                getCode(); //will throw error if it fails
                break;
            case ZUL_EVAL_SUCCEEDED:
                reset((String) message.getData(DesignerController.PARAM_WORK_FILE));
                break;
            case ZUL_EVAL_FAILED:
                print();
                break;
            case ATTRIBUTE_CHANGED:
                print();
//                addBookmark();
                break;
            case SET_BOOKMARK:
                //addBookmark();
                break;
            case RESTORE_BOOKMARK:
                //restoreBookmark((String) message.getDefaultParam());
                break;
            case EVALUATE_ZUL:
                break;
        }
    }

}
