package org.web4thejob.studio;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ProcessingInstruction;
import org.web4thejob.studio.support.AbstractController;
import org.zkoss.web.servlet.http.Encodes;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.ControllerEnum.DESIGNER_CONTROLLER;
import static org.web4thejob.studio.MessageEnum.*;
import static org.web4thejob.studio.support.StudioUtil.*;
import static org.zkoss.lang.Generics.cast;

/**
 * Created by Veniamin on 10/5/2014.
 */
public class DesignerController extends AbstractController {

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
        publish(COMPONENT_SELECTED, getElementByUuid((String) event.getData()));
        clearCanvasBusy(null);
    }

    @Listen("onCanvasSucceeded=#designer")
    public void onCanvasSucceeded(Event event) {
        Clients.clearBusy();
        clearAlerts();

        Map<String, String> data = cast(event.getData());
        String message = data.get("message");
        String hint = data.get("hint");

        if (message == null) {
            //this is the initial canvas load
            publish(RESET);
        } else {
            MessageEnum id = MessageEnum.valueOf(message);
            switch (id) {
                case EVALUATE_ZUL:
                    publish(ZUL_EVAL_SUCCEEDED, hint);
                    break;
            }

        }
    }

    @Listen("onCanvasFailed=#designer")
    public void onCanvasFailed(Event event) {
        Clients.clearBusy();
        clearAlerts();

        Map<String, String> data = cast(event.getData());
        String message = data.get("message");
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
                params.put("m", EVALUATE_ZUL.name());

                //2. Possible Hints
                if (message.getData() != null) {
                    params.put("h", message.getData().toString());
                }

                //3. Provision Processing Instructions of the user's code
                final Document doc = getCode();
                if (doc != null) {

                    StringBuilder intructions = new StringBuilder();
                    for (int i = 0; i < doc.getChildCount(); i++) {
                        if (doc.getChild(i) instanceof ProcessingInstruction) {
                            ProcessingInstruction pi = (ProcessingInstruction) doc.getChild(i);
                            if ("style".equals(pi.getTarget())) {
                                intructions.append(pi.toXML()).append("\n");
                            } else if ("script".equals(pi.getTarget())) {
                                intructions.append(pi.toXML()).append("\n");
                            }
                        }
                    }

                    if (intructions.length() > 0) {
                        params.put("pi", intructions.toString());
                    }
                }

                //4. Timestamp of the request
                params.put("t", Long.valueOf(new Date().getTime()).toString());

                try {
                    canvasHolder.setSrc(Encodes.addToQueryString(new StringBuffer("include/canvas.zul"),
                            params).toString());
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
                if (message.getData() == null) { //no hint, parse zul was clicked
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
                    showError((String) message.getData(), false);
                }
                publish(COMPONENT_SELECTED); //deselect
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
}
