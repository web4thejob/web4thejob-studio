package org.web4thejob.studio.controller.impl;

import nu.xom.Element;
import org.web4thejob.studio.controller.AbstractController;
import org.web4thejob.studio.controller.ControllerEnum;
import org.web4thejob.studio.message.Message;
import org.web4thejob.studio.message.MessageEnum;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.web.servlet.http.Encodes;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.URIEvent;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.controller.ControllerEnum.DESIGNER_CONTROLLER;
import static org.web4thejob.studio.message.MessageEnum.*;
import static org.web4thejob.studio.support.StudioUtil.*;
import static org.zkoss.lang.Generics.cast;

/**
 * Created by Veniamin on 10/5/2014.
 */
public class DesignerController extends AbstractController {
    private static final String PARAM_TIMESTAMP = "w4tjstudio_timestamp";
    public static final String PARAM_HINT = "w4tjstudio_hint";
    public static final String PARAM_MESSAGE = "w4tjstudio_message";
    public static final String PARAM_WORK_FILE = "w4tjstudio_workfile";
    public static final String PARAM_PRODUCTION_FILE = "w4tjstudio_prodfile";
    @Wire
    private Iframe canvasHolder;
    @Wire
    private Tabbox views;
    @Wire
    private Tab canvasView;
    @Wire
    private Tab outlineView;
    @Wire
    private Tab codeView;

    @Override
    public ControllerEnum getId() {
        return DESIGNER_CONTROLLER;
    }

    @Listen("onWidgetSelected=#designer")
    public void onWidgetSelected(Event event) throws InterruptedException {
        String target = (String) ((Map) event.getData()).get("target");
        notNull(target);
        publish(COMPONENT_SELECTED, getElementByUuid(target));
    }

    @Listen("onActionsClicked=#designer")
    public void onActionsClicked(Event event) {
        //showNotification("success", event.getName(), "", false);

        Menupopup popup = new Menupopup();
        popup.setMold("bs");
        popup.setSclass("custom-menupopup");
        popup.setPage(event.getTarget().getPage());
        Menuitem label = new Menuitem("hello");
        label.setParent(popup);

        int y = Integer.valueOf(((Map) event.getData()).get("top").toString());
        int x = Integer.valueOf(((Map) event.getData()).get("right").toString());
        popup.setStyle("min-width:200px");
        popup.open("auto", ((Map) event.getData()).get("top").toString() + "px");
        popup.setStyle("right:" + x + "px");
    }

    @Listen("onCanvasAddition=#designer")
    public void onCanvasAddition(Event event) {
        publish(COMPONENT_ADDED, event.getData());
//        publish(COMPONENT_SELECTED, getElementByUuid((String) event.getData()));
        clearCanvasBusy(null);
    }

    @Listen("onCanvasSucceeded=#designer")
    public void onCanvasSucceeded(Event event) {
        Clients.clearBusy();
        clearAlerts();

        Map<String, String> data = cast(event.getData());

        String message = null, workFile = null;
        if (data != null) {
            message = data.get(PARAM_MESSAGE);
            workFile = data.get(PARAM_WORK_FILE);
        }

        if (message == null) {
            //this is the initial canvas load
            publish(RESET, workFile);
        } else {
            MessageEnum id = MessageEnum.valueOf(message);
            switch (id) {
                case EVALUATE_ZUL:
                    data.remove(PARAM_MESSAGE);
                    publish(ZUL_EVAL_SUCCEEDED, data);
                    break;
            }
        }
    }

    @Listen("onCanvasFailed=#designer")
    public void onCanvasFailed(Event event) {
        Clients.clearBusy();
        clearAlerts();

        Map<String, String> data = cast(event.getData());
        String message = null;
        if (data != null) {
            message = data.get(PARAM_MESSAGE);
        }
        if (message == null) {
            publish(ZUL_EVAL_FAILED, "Unspecified error occurred");
            return;
        }

        MessageEnum id = MessageEnum.valueOf(message);
        switch (id) {
            case EVALUATE_ZUL:
                publish(ZUL_EVAL_FAILED, data.get("exception"));
                break;
        }
    }

    @Override
    public void process(Message message) {
        switch (message.getId()) {
            case EVALUATE_ZUL:
                Map<String, String> params = new LinkedHashMap<>();

                //1. Message id
                params.put(PARAM_MESSAGE, EVALUATE_ZUL.name());

                //2. Possible Hints
                if (message.getData() != null) {
                    params.put(PARAM_HINT, message.getData().toString());
                }

                //3. Timestamp of the request to prevent caching
                params.put(PARAM_TIMESTAMP, Long.valueOf(new Date().getTime()).toString());

                //4. Working file, this file will be read by CanvasUiFactory.getPageDefinition()
                params.put(PARAM_WORK_FILE, StudioUtil.buildWorkingFile(StudioUtil.getCode()).getAbsolutePath());
                try {
                    String src = getCanvasHolderURI();
                    canvasHolder.removeAttribute("src");
                    canvasHolder.setSrc(Encodes.setToQueryString(new StringBuffer(src), params).toString());
                    Clients.evalJavaScript("w4tjStudioDesigner.monitorCanvasHealth()");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            case XML_EVAL_SUCCEEDED:
                publish(EVALUATE_ZUL);
                break;
            case ZUL_EVAL_SUCCEEDED:
                canvasView.setDisabled(false);
                outlineView.setDisabled(false);
                if (message.getData(PARAM_HINT) == null) { //no hint, parse zul was clicked
                    Clients.evalJavaScript("w4tjStudioDesigner.codeSuccessEffect()");
                }
                break;
            case XML_EVAL_FAILED:
                canvasView.setDisabled(true);
                outlineView.setDisabled(true);
                Clients.clearBusy();
                clearAlerts();
                showError((Exception) message.getData(), false);
                break;
            case ZUL_EVAL_FAILED:
                if (!codeView.isSelected()) {
                    codeView.setSelected(true);
                    Clients.evalJavaScript("if (myCodeMirror) myCodeMirror.refresh()"); //setSelected does not
                    // trigger onSelect on client ?!?
                }
                canvasView.setDisabled(true);
                outlineView.setDisabled(true);
                if (message.getData() != null) {
                    showError(message.getData().toString(), false);
                }
//                publish(COMPONENT_SELECTED); //deselect
                break;
            case CODE_CHANGED:
                codeView.setSelected(true);
                canvasView.setDisabled(true);
                outlineView.setDisabled(true);
                break;
            case COMPONENT_SELECTED:
                if (message.getData() != null)
                    Clients.evalJavaScript("w4tjStudioDesigner.selectCanvasWidget('" + ((Element) message.getData())
                            .getAttributeValue("uuid") + "')");
                else
                    Clients.evalJavaScript("w4tjStudioDesigner.selectCanvasWidget()");
                break;
            case NON_ZK_PAGE_VISITED:
                codeView.setDisabled(true);
                outlineView.setDisabled(true);
                break;
            case ZK_PAGE_VISITED:
                codeView.setDisabled(false);
                outlineView.setDisabled(false);
                break;
            default:
                break;
        }


    }

    @Listen("onParseZulClicked=#designer")
    public void onParseZulClicked() {
        publish(EVALUATE_XML);
    }

    @Listen("onCanvasHang=#designer")
    public void onCanvasHang() {
        Clients.clearBusy();
        publish(ZUL_EVAL_FAILED);
    }

    @Listen("onURIChange=#canvasHolder")
    public void onCanvasURIChanged(URIEvent event) {
        canvasHolder.setAttribute("src", event.getURI());
    }

    private String getCanvasHolderURI() {
        String src = (String) canvasHolder.getAttribute("src");
        if (src == null) {
            src = canvasHolder.getSrc();
        }

        try {
            src = Encodes.removeFromQueryString(src, PARAM_MESSAGE);
            src = Encodes.removeFromQueryString(src, PARAM_HINT);
            src = Encodes.removeFromQueryString(src, PARAM_WORK_FILE);
            src = Encodes.removeFromQueryString(src, PARAM_TIMESTAMP);
            src = Encodes.removeFromQueryString(src, PARAM_PRODUCTION_FILE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return src;
    }

    @Listen("onNonZKPage=#designer")
    public void onNonZKPage() {
        publish(NON_ZK_PAGE_VISITED);
    }

    @Listen("onZKPage=#designer")
    public void onZKPage() {
        publish(ZK_PAGE_VISITED);
    }

}
