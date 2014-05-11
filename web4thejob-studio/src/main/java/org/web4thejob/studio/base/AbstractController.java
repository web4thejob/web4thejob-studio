package org.web4thejob.studio.base;

import org.web4thejob.studio.Controller;
import org.web4thejob.studio.ControllerEnum;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
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
    protected EventQueue<Event> queue;

    private static void register(Controller controller) {
        SortedMap<ControllerEnum, Controller> controllers = cast(Executions.getCurrent().getDesktop().getAttribute
                (ATTR_STUDIO_CONTROLLERS));
        if (controllers == null) {
            controllers = new TreeMap<ControllerEnum, Controller>();
        }
        controllers.put(controller.getId(), controller);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        register(this);
        startQueue();
        init();
    }

    @Override
    public int compareTo(Controller o) {
        return this.getId().compareTo(o.getId());
    }

    protected void init() {
        //override
    }

    private void startQueue() {
        queue = EventQueues.lookup("studio", EventQueues.DESKTOP, true);
    }

}
