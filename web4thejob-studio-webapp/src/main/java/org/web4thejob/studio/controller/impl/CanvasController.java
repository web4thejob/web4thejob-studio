package org.web4thejob.studio.controller.impl;

import nu.xom.*;
import org.web4thejob.studio.controller.AbstractController;
import org.web4thejob.studio.controller.ControllerEnum;
import org.web4thejob.studio.support.ChildDelegate;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.sys.PageCtrl;
import org.zkoss.zk.ui.sys.WebAppCtrl;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Window;

import java.net.URLDecoder;
import java.util.*;

import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.controller.ControllerEnum.CANVAS_CONTROLLER;
import static org.web4thejob.studio.support.StudioUtil.*;

/**
 * Created by Veniamin on 9/5/2014.
 */
public class CanvasController extends AbstractController {

    @Wire
    private Window canvas;

    /**
     * Clears white space for all code blocks so that pretty formatting can work recursively.
     */
    private static void clearWitespaces(Element element) {
        for (int i = 0; i < element.getChildElements().size(); i++) {
            Element child = element.getChildElements().get(i);
            if (isCodeElement(child)) {
                for (int j = 0; j < child.getChildCount(); j++) {
                    Node node = child.getChild(j);
                    if (node instanceof Text) {
                        String value = node.getValue();
                        StringBuilder sb = new StringBuilder();
                        StringTokenizer tokenizer = new StringTokenizer(value, "\n");
                        while (tokenizer.hasMoreTokens()) {
                            sb.append(tokenizer.nextToken().trim());
                        }
                        ((Text) node).setValue(sb.toString());
                    }
                }
            }
        }
    }

    private static void clearIndiChildren(Element element) {
        List<Element> toDetachList = new ArrayList<>(element.getChildElements().size());
        for (int i = 0; i < element.getChildElements().size(); i++) {
            Element child = element.getChildElements().get(i);
            if (isEventOrAttributeElement(child)) continue;
            toDetachList.add(child);
        }

        for (Element toDetach : toDetachList) {
            toDetach.detach();
        }
    }

    @Override
    protected void init() throws Exception {
        super.init();

        //init() ensures that the page has not been created yet, thus it is a convenient place to apply
        //any processing instructions that live in the header section of the page.
        String instructions = getQueryParam(Executions.getCurrent().getDesktop().getQueryString(), "pi");
        if (instructions != null) {
            instructions = URLDecoder.decode(instructions, "UTF-8");
            ((PageCtrl) canvas.getPage()).addAfterHeadTags(processProcessingInstructions(instructions.split("\n")));
        }
    }

    @Override
    public ControllerEnum getId() {
        return CANVAS_CONTROLLER;
    }

    @Listen("onPairedWithDesigner=#canvas")
    public void onPairedWithDesigner(Event event) {
        String canvasDesktopId = Executions.getCurrent().getDesktop().getId();
        notNull(canvasDesktopId);
        Desktop canvasDesktop = Executions.getCurrent().getDesktop();
        notNull(canvasDesktop);

        String designerDesktopId = (String) ((Map) event.getData()).get("designerDesktopId");
        notNull(designerDesktopId);
        Desktop designerDesktop = ((WebAppCtrl) Executions.getCurrent().getDesktop().getWebApp()).getDesktopCache
                (Executions.getCurrent().getSession()).getDesktop(designerDesktopId);
        notNull(designerDesktop);

        canvasDesktop.setAttribute(ATTR_PAIRED_DESKTOP, designerDesktop);
        designerDesktop.setAttribute(ATTR_PAIRED_DESKTOP, canvasDesktop);
        designerDesktop.setAttribute(ATTR_CANVAS_UUID, canvas.getUuid());

        String result = "";
        String message = getQueryParam(Executions.getCurrent().getDesktop().getQueryString(), "m");
        String hint = getQueryParam(Executions.getCurrent().getDesktop().getQueryString(), "h");
        Map<String, String> data = new HashMap<>();
        try {
            refresh();
            data.put("message", message);
            data.put("hint", hint);
            result = "onCanvasSucceeded";
        } catch (Exception e) {
            e.printStackTrace();
            data.put("message", message);
            data.put("exception", (e.getMessage() != null ? e.getMessage() : e.toString()));
            result = "onCanvasFailed";
        } finally {
            sendToDesigner(result, data);
        }

    }

    @Listen("onTemplateDropped=#canvas")
    public void onTemplateDropped(Event event) {
        String template = (String) ((Map) event.getData()).get("template");
        notNull(template);
        String parentUuid = (String) ((Map) event.getData()).get("parent");
        notNull(parentUuid);

        String templatePath = "~./template/" + template;
        try {
            Component parent = getComponentByUuid(parentUuid);
            Map<String, Object> args = new HashMap<>();
            args.put("parent", parent);
            Component target = Executions.getCurrent().createComponents(templatePath, parent, args);
            if (target != null) {
                StudioUtil.sendToDesigner("onCanvasAddition", target.getUuid());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError(e, true);
        } finally {
            Clients.clearBusy();
        }
    }

    @Listen("onWidgetSelected=#canvas")
    public void onWidgetSelected(Event event) throws InterruptedException {
        String target = (String) ((Map) event.getData()).get("target");
        notNull(target);
        StudioUtil.sendToDesigner(event.getName(), event.getData());
    }

    private void refresh() throws Exception {
        final Document doc = getCode();
        if (doc == null) return;

        cleanUUIDs(doc.getRootElement());
        doc.getRootElement().addAttribute(new Attribute("uuid", canvas.getUuid()));

        final Document docTag = ((Document) doc.copy());
        docTag.getRootElement().removeChildren();
        cleanUUIDs(docTag.getRootElement());

        Map<String, Object> params = new HashMap<>();
        params.put("parent", canvas);

        traverseChildren(doc.getRootElement(), params, new ChildDelegate<Element>() {
            @Override
            public void onChild(Element child, Map<String, Object> params) {
                if (params.get("parent") == null) return;
                if (child.equals(doc.getRootElement())) return;
                if (isEventOrAttributeElement(child)) return;
                clearWitespaces(child);

                Element clone = (Element) child.copy();
                clearIndiChildren(clone);

                Document zk = (Document) docTag.copy();
                zk.getRootElement().appendChild(clone);

                Component target = Executions.createComponentsDirectly(zk.toXML(), null,
                        (Component) params.get("parent"), null);
                params.put("parent", target);

                if (target == null) return;
                child.addAttribute(new Attribute("uuid", target.getUuid()));
            }
        });
    }


}
