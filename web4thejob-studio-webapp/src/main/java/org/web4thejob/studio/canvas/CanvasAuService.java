package org.web4thejob.studio.canvas;

import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.sys.WebAppCtrl;
import org.zkoss.zk.ui.util.Clients;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.controller.impl.DesignerController.PARAM_HINT;
import static org.web4thejob.studio.controller.impl.DesignerController.PARAM_MESSAGE;
import static org.web4thejob.studio.support.StudioUtil.*;

/**
 * Created by e36132 on 29/5/2014.
 */
public class CanvasAuService implements AuService {
    private static void onPairedWithDesigner(AuRequest request) {
        String canvasDesktopId = Executions.getCurrent().getDesktop().getId();
        notNull(canvasDesktopId);
        Desktop canvasDesktop = Executions.getCurrent().getDesktop();
        notNull(canvasDesktop);
        canvasDesktop.setAttribute(ATTR_CANVAS_DESKTOP, true);
        canvasDesktop.setAttribute(ATTR_CANVAS_FILE, canvasDesktop.getWebApp().getRealPath(canvasDesktop
                .getRequestPath()));

        String designerDesktopId = (String) ((Map) request.getData()).get("designerDesktopId");
        notNull(designerDesktopId);
        Desktop designerDesktop = ((WebAppCtrl) Executions.getCurrent().getDesktop().getWebApp()).getDesktopCache
                (Executions.getCurrent().getSession()).getDesktop(designerDesktopId);
        notNull(designerDesktop);

        canvasDesktop.setAttribute(ATTR_PAIRED_DESKTOP, designerDesktop);
        designerDesktop.setAttribute(ATTR_PAIRED_DESKTOP, canvasDesktop);

        String result = "", query = Executions.getCurrent().getDesktop().getQueryString();
        String message = getQueryParam(query, PARAM_MESSAGE);
        String hint = getQueryParam(query, PARAM_HINT);
        Map<String, String> data = new HashMap<>();

        try {
//            refresh();
            if (message != null) data.put(PARAM_MESSAGE, message);
            if (hint != null) data.put(PARAM_HINT, hint);
            result = "onCanvasSucceeded";
        } catch (Exception e) {
//            e.printStackTrace();
            if (message != null) data.put(PARAM_MESSAGE, message);
            data.put("exception", (e.getMessage() != null ? e.getMessage() : e.toString()));
            result = "onCanvasFailed";
        } finally {
            sendToDesigner(result, data);
        }

    }

    private static void onTemplateDropped(AuRequest event) {
        String template = (String) ((Map) event.getData()).get("template");
        notNull(template);
        String parentUuid = (String) ((Map) event.getData()).get("parent");
        notNull(parentUuid);

        String templatePath = "~./template/" + template;
        try {
            Component parent = getComponentByUuid(parentUuid);
            Map<String, Object> args = new HashMap<>();
            args.put("parent", parent);
            Component target = Executions.getCurrent().createComponents(templatePath, parent, args);
            if (target != null) {
                StudioUtil.sendToDesigner("onCanvasAddition", target.getUuid());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError(e, true);
        } finally {
            Clients.clearBusy();
        }
    }

    @Override
    public boolean service(AuRequest request, boolean everError) {
        String cmd = request.getCommand();

        switch (cmd) {
            case "onPairedWithDesigner":
                onPairedWithDesigner(request);
                return true;
            case "onTemplateDropped":
                onTemplateDropped(request);
        }

        return false;
    }
}
