package org.web4thejob.studio.canvas;

import nu.xom.*;
import org.apache.commons.io.FileUtils;
import org.web4thejob.studio.support.ChildDelegate;
import org.web4thejob.studio.support.CodeFormatter;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuService;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.sys.DesktopCtrl;
import org.zkoss.zk.ui.util.Clients;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.Validate.notNull;
import static org.web4thejob.studio.controller.impl.DesignerController.*;
import static org.web4thejob.studio.support.StudioUtil.*;

/**
 * Created by e36132 on 29/5/2014.
 */
public class CanvasAuService implements AuService {

    private static void onCanvasReady(AuRequest request) {
        Desktop canvasDesktop = Executions.getCurrent().getDesktop();
        String prodFile = canvasDesktop.getWebApp().getRealPath(canvasDesktop.getRequestPath());
        String queryString = Executions.getCurrent().getDesktop().getQueryString();
        String message = getQueryParam(queryString, PARAM_MESSAGE);
        String hint = getQueryParam(queryString, PARAM_HINT);
        String workFile = getQueryParam(queryString, PARAM_WORK_FILE);
        String xpath = getQueryParam(queryString, PARAM_XPATH);

        Map<String, String> data = new HashMap<>();
        if (prodFile != null) data.put(PARAM_PRODUCTION_FILE, prodFile);
        if (message != null) data.put(PARAM_MESSAGE, message);
        if (hint != null) data.put(PARAM_HINT, hint);
        if (workFile != null) data.put(PARAM_WORK_FILE, workFile);
        if (xpath != null) data.put(PARAM_XPATH, xpath);

        String result = "onCanvasSucceeded";
        try {

            Exception e = getExceptionIfAny();

            if (e == null) {
                Document document = mapZulToComponents(workFile != null ? workFile : prodFile);
                if (workFile != null) {
                    //update workfile with uuids
                    FileUtils.writeStringToFile(new File(workFile), CodeFormatter.formatXML(document), "UTF-8");

                    //update production file without uuids
                    cleanUUIDs(document.getRootElement());
                    File fileProd = new File(prodFile);
                    if (fileProd.exists() && fileProd.canWrite()) {
                        FileUtils.writeStringToFile(fileProd, CodeFormatter.formatXML(document), "UTF-8");
                    }
                } else {
                    workFile = buildWorkingFile(document).getAbsolutePath();
                    data.put(PARAM_WORK_FILE, workFile);
                }
            } else {
                result = "onCanvasFailed";
                data.put("exception", (e.getMessage() != null ? e.getMessage() : e.toString()));
            }
        } catch (Exception e) {
            result = "onCanvasFailed";
            data.put("exception", (e.getMessage() != null ? e.getMessage() : e.toString()));
            e.printStackTrace();
        } finally {
            sendToDesigner(result, data);
        }

    }

    private static void onTemplateDropped(AuRequest event) {
        String template = (String) ((Map) event.getData()).get("template");
        notNull(template);
        String parentUuid = (String) ((Map) event.getData()).get("parent");
        notNull(parentUuid);


        if (template.startsWith("@")) {
            Component target = getComponentByUuid(parentUuid);
            String property = getDefaultBindingProperty(target.getDefinition());
            if (property != null) {
                Map<String, String> data = new HashMap<>();
                data.put("target", parentUuid);
                data.put("binding", template);
                data.put("property", property);

                sendToDesigner("onBindingDropped", data);
            } else {
                showNotification("warning", "Not quite", "This component does not declare a default binding property.</br>Drop the binding on a Property Editor row directly to resolve.", true, true);
            }
            return;
        }


        if (Executions.getCurrent().getDesktop().getPageIfAny(parentUuid) != null) {
            parentUuid = "_canvas_";
        }

        String templatePath = "~./template/" + template;
        try {
            Component parent = !"_canvas_".equals(parentUuid) ? getComponentByUuid(parentUuid) : null;
            Map<String, Object> args = new HashMap<>();
            args.put("parent", parent);

            Map<String, String> data = new HashMap<>();
            data.put("parent", parentUuid);
            data.put("template", templatePath);

            if (!"zscript.zul".equals(template)) {
                Component target = Executions.getCurrent().createComponents(templatePath, parent, args);
                if (target != null) {
                    data.put("target", target.getUuid());
                } else {
                    return;
                }
            }
            sendToDesigner("onCanvasAddition", data);


        } catch (Exception e) {
            e.printStackTrace();
            Clients.evalJavaScript("top.zAu.cmd0.clearBusy()");
            showError(e, true);
        } finally {
            Clients.clearBusy();
        }
    }

    private static Document mapZulToComponents(String src) throws IOException, ParsingException {
        final Desktop canvas = Executions.getCurrent().getDesktop();
        Document document = buildDocument(new FileInputStream(src));
        if (document.getRootElement() == null) return document;
        cleanUUIDs(document.getRootElement());

        Map<String, Object> params = new HashMap<>();
        traverseChildren(document.getRootElement(), params, new ChildDelegate<Element>() {
            @Override
            public void onChild(Element child, Map<String, Object> params) {
                if (child.getParent() instanceof Document) {
                    if ("zk".equals(child.getLocalName())) {
                        //special case when root element is a zk element
                        child.addAttribute(new Attribute("uuid", "_canvas_"));
                    }
                }
                if ("zk".equals(child.getLocalName()))
                    return;
                else if ("zscript".equals(child.getLocalName())) {
                    //although zscripts are not components I assign a fake uuid so that their handling is
                    //similar as much as possible with that of regular components.
                    child.addAttribute(new Attribute("uuid", "zscript_" + getNextUuid()));
                    return;
                } else if ((isNative(child) && isNative((Element) child.getParent())) && (!hasNonNativeSiblings(child) || child.getChildElements().size() == 0)) {
                    child.addAttribute(new Attribute("uuid", "native_" + getNextUuid()));
                    return;
                }

                Element parent = getParent(child);
                if (parent == null) {
                    for (Component root : canvas.getFirstPage().getRoots()) {
                        if (doMatch(child, root)) {
                            child.addAttribute(new Attribute("uuid", root.getUuid()));
                            break;
                        }
                    }
                } else {
                    String uuid = parent.getAttributeValue("uuid");
                    if (uuid != null) {

                        if (uuid.startsWith("native_")) {
                            parent = (Element) parent.getParent();
                            while (parent.getAttributeValue("uuid").startsWith("native_")) {
                                parent = (Element) parent.getParent();
                            }
                            uuid = parent.getAttributeValue("uuid");
                        }

                        Collection<Component> children = getComponentByUuid(uuid).getChildren();
                        for (Component comp : children) {
                            if (doMatch(child, comp)) {
                                child.addAttribute(new Attribute("uuid", comp.getUuid()));
                                break;
                            }

                        }
                    }
                }
            }
        });


//        for (Component component : Executions.getCurrent().getDesktop().getComponents()) {
//            if (isAvailable(document.getRootElement(), component.getUuid())) {
//
//            }
//        }

        return document;
    }

    private static boolean hasNonNativeSiblings(Element element) {
        Node parent = element.getParent();
        if (!(parent instanceof Element)) return false;

        for (int i = 0; i < ((Element) parent).getChildElements().size(); i++) {
            Element child = ((Element) parent).getChildElements().get(i);
            if (!child.equals(element) && !isNative(child)) {
                return true;
            }
        }
        return false;
    }

//    private static void identifyDirectNativeDescendenats(Element element) {
//        for (int i = 0; i < element.getChildElements().size(); i++) {
//            Element child = element.getChildElements().get(i);
//            if (isNative(child)) {
//                child.addAttribute(new Attribute("uuid", getNextUuid()));
//                identifyDirectNativeDescendenats(child);
//            }
//        }
//
//    }

    private static Element getParent(Element element) {
        Node parent = element.getParent();
        while (parent != null) {
            if (parent instanceof Element && !"zk".equals(((Element) parent).getLocalName())) {
                break;
            }
            parent = parent.getParent();
        }

        return parent instanceof Element ? (Element) parent : null;
    }

    private static boolean doMatch(Element element, Component component) {
        boolean match;
        match = isAvailable(element.getDocument().getRootElement(), component.getUuid());

        if (!match) return false;

        if (component instanceof HtmlNativeComponent) {
            return element.getLocalName().equals(((HtmlNativeComponent) component).getTag()) && isNative(element);
        } else {
            return element.getLocalName().equals(component.getDefinition().getName());
        }
    }

    private static boolean isAvailable(Element root, String uuid) {
//        XPathContext xpathContext = new XPathContext("zul", ZUL_NS);
        Nodes nodes = root.query("//*[@uuid='" + uuid + "']", null);
        return nodes.size() == 0;
    }

    private static void updateProductionFile(String src, String dest) {
        File srcFile = new File(src);
        File destFile = new File(dest);
        if (srcFile.exists() && destFile.exists()) {
            try {
                FileUtils.copyFile(srcFile, destFile);
                srcFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
                showError(e, false);
            }
        }
    }

    private static String getQueryParam(String queryString, String param) {
        if (queryString == null) return null;

        for (String s : queryString.split("&")) {
            if (s.startsWith(param + "=")) {
                String v = s.substring(s.indexOf("=") + 1);
                if (v.length() > 0) {
                    try {
                        v = URLDecoder.decode(v, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    return v;
                } else {
                    return v;
                }
            }
        }

        return null;
    }

    private static Exception getExceptionIfAny() {
        Exception e = null;
        for (Page page : Executions.getCurrent().getDesktop().getPages()) {
            if (page.hasAttribute("javax.servlet.error.exception")) {
                e = (Exception) page.getAttribute("javax.servlet.error.exception");
                break;
            }

        }
        return e;
    }

    private static String getNextUuid() {
        return ((DesktopCtrl) Executions.getCurrent().getDesktop()).getNextUuid(Executions.getCurrent().getDesktop().getFirstPage());
    }

    @Override
    public boolean service(AuRequest request, boolean everError) {
        String cmd = request.getCommand();

        switch (cmd) {
            case "onCanvasReady":
                onCanvasReady(request);
                return true;
            case "onTemplateDropped":
                onTemplateDropped(request);
        }

        return false;
    }
}