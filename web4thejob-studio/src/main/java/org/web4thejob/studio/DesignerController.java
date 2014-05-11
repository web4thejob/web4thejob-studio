package org.web4thejob.studio;

import org.web4thejob.studio.base.AbstractController;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Window;

import java.util.Map;

import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.ControllerEnum.CANVAS_CONTROLLER;

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

        ((Window) event.getTarget()).getChildren().clear();

//        Desktop designerDesktop= (Desktop) Executions.getCurrent().getDesktop().getAttribute
//                (ATTR_PAIRED_DESKTOP);
//        designerDesktop.enableServerPush(true);
//        Executions.getCurrent().getDesktop().enableServerPush(true);
//        Executions.activate(designerDesktop);
//        queue.publish(event);
//        Executions.deactivate(designerDesktop);
    }

    private class EventQueueHandler implements EventListener<Event> {

        @Override
        public void onEvent(Event event) throws Exception {
            Clients.alert(event.getName());
        }
    }

}
