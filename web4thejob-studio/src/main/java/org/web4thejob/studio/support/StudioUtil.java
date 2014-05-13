package org.web4thejob.studio.support;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;

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

    public static void showNotification(String clazz, String title, String message, boolean autoclose) {
        Clients.evalJavaScript("top.w4tjStudioDesigner.alert('" + clazz + "','" + title +
                "','" + message + "'," + Boolean.valueOf(autoclose).toString() + ")");

    }
}
