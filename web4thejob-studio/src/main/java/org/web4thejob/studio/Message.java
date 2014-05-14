package org.web4thejob.studio;

import java.util.Map;

import static org.zkoss.lang.Generics.cast;

/**
 * Created by e36132 on 14/5/2014.
 */
public class Message {
    private final MessageEnum id;
    private final Controller sender;
    private Object data;

    public Message(MessageEnum id, Controller sender) {
        this.id = id;
        this.sender = sender;
    }

    public Message(MessageEnum id, Controller sender, Object data) {
        this.id = id;
        this.sender = sender;
        this.data = data;
    }

    public MessageEnum getId() {
        return id;
    }

    public Controller getSender() {
        return sender;
    }

    public <T> T getData() {
        return cast(data);
    }

    public <T> T getData(Class<T> clazz) {
        return cast(data);
    }

    public <T> T getData(String key) {
        return cast(((Map) data).get(key));
    }

}
