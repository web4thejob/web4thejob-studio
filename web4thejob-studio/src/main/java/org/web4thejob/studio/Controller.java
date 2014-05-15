package org.web4thejob.studio;

/**
 * Created by Veniamin on 10/5/2014.
 */
public interface Controller extends MessageAware, Comparable<Controller> {
    ControllerEnum getId();


}
