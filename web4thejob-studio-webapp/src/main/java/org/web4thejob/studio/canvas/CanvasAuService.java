package org.web4thejob.studio.canvas;

import nu.xom.*;
import org.apache.commons.io.FileUtils;
import org.web4thejob.studio.support.ChildDelegate;
import org.web4thejob.studio.support.CodeFormatter;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.util.Clients;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.controller.impl.DesignerController.*;
import static org.web4thejob.studio.support.StudioUtil.*;

/**
 * Created by e36132 on 29/5/2014.
 */
public class CanvasAuService implements AuService {
    private static void onCanvasReady(AuRequest request) {
//        String canvasDesktopId = Executions.getCurrent().getDesktop().getId();
//        notNull(canvasDesktopId);
//        Desktop canvasDesktop = Executions.getCurrent().getDesktop();
//        notNull(canvasDesktop);
//        canvasDesktop.setAttribute(ATTR_CANVAS_DESKTOP, true);
//        canvasDesktop.setAttribute(ATTR_CANVAS_FILE, canvasDesktop.getWebApp().getRealPath(canvasDesktop
//                .getRequestPath()));

//        String designerDesktopId = (String) ((Map) request.getData()).get("designerDesktopId");
//        notNull(designerDesktopId);
//        Desktop designerDesktop = ((WebAppCtrl) Executions.getCurrent().getDesktop().getWebApp()).getDesktopCache
//                (Executions.getCurrent().getSession()).getDesktop(designerDesktopId);
//        notNull(designerDesktop);
//
//        canvasDesktop.setAttribute(ATTR_PAIRED_DESKTOP, designerDesktop);
//        designerDesktop.setAttribute(ATTR_PAIRED_DESKTOP, canvasDesktop);

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
                    StudioUtil.cleanUUIDs(document.getRootElement());
                    FileUtils.writeStringToFile(new File(prodFile), CodeFormatter.formatXML(document), "UTF-8");
                } else {
                    workFile = StudioUtil.buildWorkingFile(document).getAbsolutePath();
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

        if (Executions.getCurrent().getDesktop().getPageIfAny(parentUuid) != null) {
            parentUuid = "_canvas_";
        }

        String templatePath = "~./template/" + template;
        try {
            Component parent = !"_canvas_".equals(parentUuid) ? getComponentByUuid(parentUuid) : null;
            Map<String, Object> args = new HashMap<>();
            args.put("parent", parent);
            Component target = Executions.getCurrent().createComponents(templatePath, parent, args);
            if (target != null) {
                Map<String, String> data = new HashMap<>();
                data.put("parent", parentUuid);
                data.put("target", target.getUuid());
                data.put("template", templatePath);
                sendToDesigner("onCanvasAddition", data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError(e, true);
        } finally {
            Clients.clearBusy();
        }
    }

    private static Document mapZulToComponents(String src) throws IOException, ParsingException {
        final Desktop canvas = Executions.getCurrent().getDesktop();
        Document document = StudioUtil.buildDocument(new FileInputStream(src));
        if (document.getRootElement() == null) return document;

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
                if ("zk".equals(child.getLocalName())) return;

                Element parent = getParent(child);
                if (parent == null) {
                    for (Component root : canvas.getFirstPage().getRoots()) {
                        if (doMatch(child, root)) {
                            child.addAttribute(new Attribute("uuid", root.getUuid()));
                            break;
                        }
                    }
                } else {
                    if (parent.getAttributeValue("uuid") != null) {
                        Collection<Component> children = getComponentByUuid(parent.getAttributeValue("uuid"))
                                .getChildren();
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

        return document;
    }

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
        match = isAvailable(element.getDocument().getRootElement(), component.getUuid()) && component.getDefinition()
                .getName().equals(element.getLocalName());
        return match;
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