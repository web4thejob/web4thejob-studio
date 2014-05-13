package org.web4thejob.studio;

import org.web4thejob.studio.base.AbstractController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;

import java.util.Map;

import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.ControllerEnum.CANVAS_CONTROLLER;
import static org.web4thejob.studio.support.StudioUtil.ATTR_PAIRED_DESKTOP;
import static org.web4thejob.studio.support.StudioUtil.showNotification;

/**
 * Created by Veniamin on 10/5/2014.
 */
public class DesignerController extends AbstractController {

    @Override
    public ControllerEnum getId() {
        return CANVAS_CONTROLLER;
    }

    @Override
    protected void init() {
        super.init();
        queue.subscribe(new EventQueueHandler());
    }

    @Listen("onWidgetSelected=#designer")
    public void onWidgetSelected(Event event) throws InterruptedException {
        String target = (String) ((Map) event.getData()).get("target");
        notNull(target);

        Desktop canvasDesktop = (Desktop) Executions.getCurrent().getDesktop().getAttribute(ATTR_PAIRED_DESKTOP);
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

    private class EventQueueHandler implements EventListener<Event> {

        @Override
        public void onEvent(Event event) throws Exception {
            Clients.alert(event.getName());
        }
    }
}
