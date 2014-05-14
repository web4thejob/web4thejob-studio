package org.web4thejob.studio;

import org.web4thejob.studio.base.AbstractController;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.json.JSONValue;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;

import java.util.Date;
import java.util.Map;

import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.ControllerEnum.DESIGNER_CONTROLLER;
import static org.web4thejob.studio.MessageEnum.*;
import static org.web4thejob.studio.support.StudioUtil.*;

/**
 * Created by Veniamin on 10/5/2014.
 */
public class DesignerController extends AbstractController {

    @Wire
    private Iframe canvasHolder;

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

        Desktop canvasDesktop = StudioUtil.getPairedDesktop();
        Component comp = canvasDesktop.getComponentByUuid(target);
        showNotification("success", event.getName(), comp.getUuid(), true);
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
        showNotification("success", event.getName(), JSONValue.toJSONString(event.getData()), true);
        pubish(COMPONENT_ADDED, event.getData());
        clearCanvasBusy(null);
    }

    @Listen("onCanvasReady=#designer")
    public void onCanvasReady(Event event) {
        Clients.clearBusy();
        clearAlerts();

        String message = (String) event.getData();
        if (message == null) return;
        if (EVALUATE_CODE == valueOf(message)) {
            pubish(CODE_EVAL_SUCCEDED);
        }
    }

    @Listen("onCanvasFailed=#designer")
    public void onCanvasFailed(Event event) {
        Clients.clearBusy();
        clearAlerts();

        showError((String) event.getData(), false);
        pubish(CODE_EVAL_FAILED);
    }

    @Override
    protected void process(Message message) {
        switch (message.getId()) {
            case EVALUATE_CODE:
                canvasHolder.setSrc("include/canvas.zul?m=" + message.getId().name() + "&t=" + new Date().getTime());
                break;
            case CODE_EVAL_SUCCEDED:
                Clients.evalJavaScript("w4tjStudioDesigner.codeSuccessEffect()");
                break;
            default:
                break;
        }


    }

    @Listen("onParseZulClicked=#designer")
    public void onParseZulClicked() {
        pubish(EVALUATE_CODE);
    }
}
