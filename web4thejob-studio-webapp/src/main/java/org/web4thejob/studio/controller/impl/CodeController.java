package org.web4thejob.studio.controller.impl;

import nu.xom.Document;
import nu.xom.Element;
import org.apache.commons.io.IOUtils;
import org.web4thejob.studio.controller.AbstractController;
import org.web4thejob.studio.controller.ControllerEnum;
import org.web4thejob.studio.message.Message;
import org.web4thejob.studio.support.CodeFormatter;
import org.web4thejob.studio.support.StudioUtil;
import org.web4thejob.studio.support.ZulXsdUtil;
import org.zkoss.io.FileReader;
import org.zkoss.json.JSONObject;
import org.zkoss.util.resource.Locators;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Textbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.web4thejob.studio.message.MessageEnum.*;
import static org.web4thejob.studio.support.StudioUtil.getElementByUuid;
import static org.web4thejob.studio.support.StudioUtil.getWorkFile;

/**
 * Created by e36132 on 14/5/2014.
 */
public class CodeController extends AbstractController {
    @Wire
    private Textbox zulBox;
    private boolean changed;
    private Document document;
    private Element selection;


    @Override
    public ControllerEnum getId() {
        return ControllerEnum.CODE_CONTROLLER;
    }

    public void reset(String file) {
        try {
            document = StudioUtil.buildDocument(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        changed = false;
        print();
    }

    public Document getCode() {
        if (changed) {
            document = StudioUtil.buildDocument(zulBox.getValue());
            changed = false;
        }
        return document;
    }

    public void print() {
        if (document == null) return;
        final Document doc = (Document) document.copy();
        //TODO activate for production
        StudioUtil.cleanUUIDs(doc.getRootElement());

        zulBox.setValue(CodeFormatter.formatXML(doc));
        Clients.evalJavaScript("w4tjStudioDesigner.refreshCode()");
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

//    private void includeComponent(String uuid) {
//        Component component = getCanvasComponentByUuid(uuid);
//        Element element = toElement(component);
//
//        if (component.getParent() != null && !component.getParent().getUuid().equals(getCanvasUuid())) {
//            String parentUuid = component.getParent().getUuid();
//            getElementByUuid(parentUuid).appendChild(element);
//        } else {
//            document.getRootElement().appendChild(element);
//        }
//        print();
//    }
//
//    private Element toElement(Component component) {
//        Map<String, Object> params = new HashMap<>();
//        traverseChildren(component, params, new ChildDelegate<Component>() {
//            @Override
//            public void onChild(Component child, Map<String, Object> params) {
//                if (ClassUtils.isInnerClass(child.getClass())) return;
//
//                Element element = new Element(child.getDefinition().getName(), ZUL_NS);
//                element.addAttribute(new Attribute("uuid", child.getUuid()));
//                Element parent = (Element) params.get("parent");
//                if (parent != null) {
//                    parent.appendChild(element);
//                } else {
//                    params.put("element", element); //this is the top element
//                }
//                params.put("parent", element);
//
//
//                SortedMap<String, SortedSet<Element>> propsMap = getWidgetDescription(child.getDefinition()
//                        .getName());
//                for (String group : propsMap.keySet()) {
//                    for (Element property : propsMap.get(group)) {
//                        String propertyName = property.getAttributeValue("name");
//                        String type = property.getAttributeValue("type");
//                        boolean isBoolean = "booleanType".equals(type);
////                        if (isBannedProperty(child.getClass(), propertyName, isBoolean)) continue;
//
//                        if (hasProperty(child.getClass(), propertyName, isBoolean)) {
//                            Object value = null;
//
//                            try {
//                                value = invokeGetter(child, propertyName, isBoolean);
//                            } catch (Exception e) {
//                                //do nothing, probably just wrong state like when you call pageSize on grid without
//                                // paging mold
//                            }
//
//                            if (value != null && !isDefaultValueForProperty(child, propertyName, value.toString(),
//                                    isBoolean) && isEligibleTypeForXml(value.getClass())) {
//                                element.addAttribute(new Attribute(propertyName, value.toString()));
//                            }
//                        }
//                    }
//                }
//
//
//            }
//        });
//
//        return (Element) params.get("element");
//    }

    @Override
    public void process(Message message) {
        switch (message.getId()) {
            case COMPONENT_ADDED:
                JSONObject data = message.getData();
                Element parent = getElementByUuid((String) data.get("parent"));
                String template = ((String) data.get("template")).replace("~./", "web/");

                Element toSelect = null;
                try {
                    Element target = StudioUtil.buildDocument(Locators.getDefault().getResourceAsStream(template)).getRootElement();
                    if ("zk".equals(target.getLocalName())) {
                        for (int i = 0; i < target.getChildCount(); i++) {
                            Element clone = (Element) target.getChild(i).copy();
                            parent.appendChild(clone);
                            if (i == 0) toSelect = clone;
                        }
                    } else {
                        toSelect = (Element) target.copy();
                        parent.appendChild(toSelect);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

                Map<String, String> hints = new HashMap<>();
                hints.put(DesignerController.PARAM_HINT, COMPONENT_ADDED.name());
                hints.put(DesignerController.PARAM_XPATH, ZulXsdUtil.getXPath(toSelect));
                publish(EVALUATE_ZUL, hints);
                break;
            case RESET:
                reset((String) message.getData());
                break;
            case COMPONENT_DETACHED:
                Element element = message.getData();
                element.detach();
                print();
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
                break;
            case COMPONENT_SELECTED:
                selection = message.getData();
                break;
            case CODE_ACTIVATED:
                if (changed || selection == null || selection.getAttributeValue("uuid") == null) return;

                List<String> lines;
                try {
                    File workFile = new File(getWorkFile());
                    if (!workFile.exists()) return;
                    lines = IOUtils.readLines(new FileReader(workFile, "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                int lineNo = -1, charNo = -1;
                Pattern pattern = Pattern.compile("(^\\s*)(<" + selection.getLocalName() + "\\s{1,1})(.*)(\\s*uuid=\"" + selection.getAttributeValue("uuid") + "\")");
                Matcher matcher;
                for (String line : lines) {
                    lineNo++;

                    matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        for (int m = 0; m <= matcher.groupCount(); m++) {
                            if (matcher.group(m).startsWith("<")) {
                                charNo = matcher.start(m);
                            }
                        }
                        break;
                    }

                }

                if (lineNo >= 0 && charNo >= 0) {
                    Clients.evalJavaScript("myCodeMirror.setCursor({line:" + lineNo + ",ch:" + charNo + "});myCodeMirror.scrollIntoView(null,Math.round(jq(\".CodeMirror-scroll\").height() / 2))");
                }

                break;
        }
    }

}
