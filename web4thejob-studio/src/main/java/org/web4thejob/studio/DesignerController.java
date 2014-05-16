package org.web4thejob.studio;

import org.web4thejob.studio.support.AbstractController;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import java.util.Date;
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

    @Override
    protected void init() {
        super.init();
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
        clearCanvasBusy(null);
    }

    @Listen("onCanvasReady=#designer")
    public void onCanvasReady(Event event) {
        Clients.clearBusy();
        clearAlerts();

        String message = (String) event.getData();
        if (message == null) {
            publish(RESET);
        } else {
            MessageEnum id = MessageEnum.valueOf(message);
            switch (id) {
                case EVALUATE_ZUL:
                    publish(ZUL_EVAL_SUCCEEDED);
                    break;
            }

        }

        //this is a message execution
//        if (EVALUATE_XML == valueOf(message)) {
//            pubish(XML_EVAL_SUCCEDED);
//        }
    }

    @Listen("onCanvasFailed=#designer")
    public void onCanvasFailed(Event event) {
        Clients.clearBusy();
        clearAlerts();

        Map<String, String> data = cast(event.getData());
        showError(data.get("exception"), false);

        String message = data.get("message");
        MessageEnum id = MessageEnum.valueOf(message);
        switch (id) {
            case EVALUATE_ZUL:
                publish(ZUL_EVAL_FAILED);
                break;
        }
    }

    @Override
    public void process(Message message) {
        switch (message.getId()) {
            case ZUL_EVAL_SUCCEEDED:
                canvasView.setDisabled(false);
                outlineView.setDisabled(false);
                Clients.evalJavaScript("w4tjStudioDesigner.codeSuccessEffect()");
                break;
            case XML_EVAL_FAILED:
                canvasView.setDisabled(true);
                outlineView.setDisabled(true);
                Clients.clearBusy();
                clearAlerts();
                showError((Exception) message.getData(), false);
                break;
            case XML_EVAL_SUCCEEDED:
                publish(EVALUATE_ZUL);
                break;
            case EVALUATE_ZUL:
                Clients.evalJavaScript("w4tjStudioDesigner.monitorCanvasHealth()");
                canvasHolder.setSrc("include/canvas.zul?m=" + EVALUATE_ZUL + "&t=" + new Date().getTime());
                break;
            case CODE_CHANGED:
                canvasView.setDisabled(true);
                outlineView.setDisabled(true);
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
        codeView.setSelected(true);
        canvasView.setDisabled(true);
        outlineView.setDisabled(true);
    }
}
