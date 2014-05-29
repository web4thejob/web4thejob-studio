package org.web4thejob.studio.support;

import org.web4thejob.studio.canvas.CanvasAuService;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.util.DesktopInit;

/**
 * Created by e36132 on 29/5/2014.
 */
public class DesktopInitListener implements DesktopInit {
    private static CanvasAuService canvasAuService = new CanvasAuService();

    @Override
    public void init(Desktop desktop, Object request) throws Exception {
        desktop.addListener(canvasAuService);
    }
}
