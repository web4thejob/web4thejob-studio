package org.web4thejob.studio;

import nu.xom.*;
import org.apache.commons.lang.ClassUtils;
import org.web4thejob.studio.support.AbstractController;
import org.web4thejob.studio.support.ChildDelegate;
import org.web4thejob.studio.support.MultiplexSerializer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Textbox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import static org.web4thejob.studio.MessageEnum.CODE_CHANGED;
import static org.web4thejob.studio.MessageEnum.COMPONENT_SELECTED;
import static org.web4thejob.studio.support.StudioUtil.*;
import static org.web4thejob.studio.support.ZulXsdUtil.ZUL_NS;
import static org.web4thejob.studio.support.ZulXsdUtil.getWidgetDescription;
import static org.zkoss.zk.ui.metainfo.LanguageDefinition.CLIENT_NAMESPACE;

/**
 * Created by e36132 on 14/5/2014.
 */
public class CodeController extends AbstractController {
    @Wire
    private Textbox zulBox;
    private boolean changed;
    private Document document;

    private static void cleanWellKnownErrors(Document document) {
        XPathContext xpathContext = new XPathContext("zul", ZUL_NS);
        Nodes nodes = document.query("//zul:panelchildren[@vflex]", xpathContext);
        for (int i = 0; i < nodes.size(); i++) {
            Attribute a = ((Element) nodes.get(i)).getAttribute("vflex");
            a.detach();
        }

        nodes = document.query("//zul:treeitem[@label]", xpathContext);
        for (int i = 0; i < nodes.size(); i++) {
            Attribute a = ((Element) nodes.get(i)).getAttribute("label");
            a.detach();
        }
        nodes = document.query("//zul:treerow[@label]", xpathContext);
        for (int i = 0; i < nodes.size(); i++) {
            Attribute a = ((Element) nodes.get(i)).getAttribute("label");
            a.detach();
        }
        nodes = document.query("//zul:tabbox[@selectedIndex]", xpathContext);
        for (int i = 0; i < nodes.size(); i++) {
            Attribute a = ((Element) nodes.get(i)).getAttribute("selectedIndex");
            a.detach();
        }

    }

    @Override
    public ControllerEnum getId() {
        return ControllerEnum.CODE_CONTROLLER;
    }

    public void reset() {
        Element zk = new Element("zk", ZUL_NS);
        zk.addAttribute(new Attribute("uuid", getCanvasUuid()));
        zk.addNamespaceDeclaration("client", CLIENT_NAMESPACE);
        document = new Document(zk);
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
        cleanWellKnownErrors(document);
        final Document doc = (Document) document.copy();
        cleanUUIDs(doc.getRootElement());

        final Serializer serializer;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            serializer = new MultiplexSerializer(out);
            serializer.setIndent(2);
            serializer.write(doc);
//            serializer.setMaxLength(120);
            serializer.flush();

            String zul = out.toString("UTF-8");
            zul = zul.replaceAll(" xmlns=\"\"", "");//xom bug?

            zulBox.setValue(zul);
            Clients.evalJavaScript("myCodeMirror.refresh()");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                includeComponent(message.getData(String.class));
                //addBookmark();
                break;
            case RESET:
                reset();
                break;
            case COMPONENT_DETACHED:
                Element element = message.getData();
                element.detach();
                print();
                //addBookmark();
                break;
            case EVALUATE_XML:
                getCode(); //will throw error if it fails
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
        }
    }

}
