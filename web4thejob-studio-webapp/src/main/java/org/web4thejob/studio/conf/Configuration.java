package org.web4thejob.studio.conf;

import java.io.Serializable;

/**
 * Created by e36132 on 24/6/2014.
 */
public class Configuration implements Serializable {
    private boolean alwaysReturnToCanvas;

    public boolean isAlwaysReturnToCanvas() {
        return alwaysReturnToCanvas;
    }

    public void setAlwaysReturnToCanvas(boolean alwaysReturnToCanvas) {
        this.alwaysReturnToCanvas = alwaysReturnToCanvas;
    }

}
