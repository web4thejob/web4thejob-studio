package org.web4thejob.studio;

import java.util.Map;

import static org.zkoss.lang.Generics.cast;

/**
 * Created by e36132 on 14/5/2014.
 */
public class Message {
    private final MessageEnum id;
    private final Controller sender;
    private final Object data;
    private boolean stopPropagation;

    public Message(MessageEnum id, Controller sender) {
        this.id = id;
        this.sender = sender;
        this.data = null;
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

    public boolean isStopPropagation() {
        return stopPropagation;
    }

    public void setStopPropagation(boolean stopPropagation) {
        this.stopPropagation = stopPropagation;
    }
}
