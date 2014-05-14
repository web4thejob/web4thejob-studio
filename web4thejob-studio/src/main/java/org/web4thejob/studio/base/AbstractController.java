package org.web4thejob.studio.base;

import org.web4thejob.studio.Controller;
import org.web4thejob.studio.ControllerEnum;
import org.web4thejob.studio.Message;
import org.web4thejob.studio.MessageEnum;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;

import java.util.SortedMap;
import java.util.TreeMap;

import static org.web4thejob.studio.support.StudioUtil.ATTR_STUDIO_CONTROLLERS;
import static org.zkoss.lang.Generics.cast;

/**
 * Created by Veniamin on 10/5/2014.
 */
public abstract class AbstractController extends SelectorComposer<Component> implements Controller {
    private final EventQueue<EventWrapper> pipeline = EventQueues.lookup("studio-pipeline", EventQueues.DESKTOP, true);
    private final EventWrapperListener EVENT_WRAPPER_LISTENER = new EventWrapperListener();

    private static void register(Controller controller) {
        synchronized (Executions.getCurrent().getDesktop()) {
            SortedMap<ControllerEnum, Controller> controllers = cast(Executions.getCurrent().getDesktop().getAttribute
                    (ATTR_STUDIO_CONTROLLERS));
            if (controllers == null) {
                Executions.getCurrent().getDesktop().setAttribute(ATTR_STUDIO_CONTROLLERS,
                        controllers = new TreeMap<>());

            }
            controllers.put(controller.getId(), controller);
        }
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        register(this);
        pipeline.subscribe(EVENT_WRAPPER_LISTENER);
        init();
    }

    protected void pubish(MessageEnum msgid, Object data) {
        //marshall message to event
        pipeline.publish(new EventWrapper(this, "on" + msgid.name(), null, data));
    }

    protected void pubish(MessageEnum msgid) {
        //marshall message to event
        pipeline.publish(new EventWrapper(this, "on" + msgid.name(), null, null));
    }

    protected void process(Message message) {
        //override
    }


    protected void init() {
        //override
    }


    @Override
    public int compareTo(Controller o) {
        return this.getId().compareTo(o.getId());
    }

    private class EventWrapperListener implements EventListener<EventWrapper> {

        @Override
        public void onEvent(EventWrapper event) throws Exception {
            MessageEnum id = MessageEnum.valueOf(event.getName().substring(2)); //exclude "on"
            process(new Message(id, event.getSender(), event.getData()));
        }
    }


}
