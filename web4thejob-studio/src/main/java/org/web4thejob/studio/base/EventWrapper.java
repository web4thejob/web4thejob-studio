package org.web4thejob.studio.base;

import org.web4thejob.studio.Controller;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

import static org.springframework.util.Assert.notNull;

/**
 * Created by e36132 on 14/5/2014.
 */
/*package*/ class EventWrapper extends Event {
    private final Controller sender;

    private EventWrapper(String name) {
        super(name);
        throw new UnsupportedOperationException();
    }

    private EventWrapper(String name, Component component) {
        super(name, component);
        throw new UnsupportedOperationException();
    }

    private EventWrapper(String name, Component component, Object data) {
        super(name, component, data);
        throw new UnsupportedOperationException();
    }

    EventWrapper(Controller sender, String name, Component component, Object data) {
        super(name, component, data);
        notNull(sender);
        this.sender = sender;
    }

    Controller getSender() {
        return sender;
    }


}
