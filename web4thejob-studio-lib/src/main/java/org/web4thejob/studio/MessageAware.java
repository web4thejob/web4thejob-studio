package org.web4thejob.studio;

/**
 * Created by e36132 on 15/5/2014.
 */
public interface MessageAware {
    void publish(MessageEnum id);

    void publish(MessageEnum id, Object data);

    void process(Message message) throws Exception;
}
