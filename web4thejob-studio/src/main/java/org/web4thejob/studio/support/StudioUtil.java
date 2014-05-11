package org.web4thejob.studio.support;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;

/**
 * Created by Veniamin on 9/5/2014.
 */
public class StudioUtil {
    public static final String ATTR_PAIRED_DESKTOP = "paired-desktop-id";
    public static final String ATTR_STUDIO_CONTROLLERS = "studio-controllers";


    /**
     * convenience method
     */
    public static Component getComponentByUuid(String uuid) {
        return Executions.getCurrent().getDesktop().getComponentByUuid(uuid);
    }

}
