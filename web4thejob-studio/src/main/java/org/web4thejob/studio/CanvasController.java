package org.web4thejob.studio;

import org.web4thejob.studio.base.AbstractController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.sys.WebAppCtrl;

import java.util.Map;

import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.ControllerEnum.CANVAS_CONTROLLER;
import static org.web4thejob.studio.support.StudioUtil.ATTR_PAIRED_DESKTOP;
import static org.web4thejob.studio.support.StudioUtil.getComponentByUuid;

/**
 * Created by Veniamin on 9/5/2014.
 */
public class CanvasController extends AbstractController {

    @Override
    public ControllerEnum getId() {
        return CANVAS_CONTROLLER;
    }

    @Listen("onPairedWithDesigner=#canvas")
    public void onPairedWithDesigner(Event event) {
        String canvasDesktopId = Executions.getCurrent().getDesktop().getId();
        notNull(canvasDesktopId);
        Desktop canvasDesktop = Executions.getCurrent().getDesktop();
        notNull(canvasDesktop);

        String designerDesktopId = (String) ((Map) event.getData()).get("designerDesktopId");
        notNull(designerDesktopId);
        Desktop designerDesktop = ((WebAppCtrl) Executions.getCurrent().getDesktop().getWebApp()).getDesktopCache
                (Executions.getCurrent()
                        .getSession()).getDesktop(designerDesktopId);
        notNull(designerDesktop);

        canvasDesktop.setAttribute(ATTR_PAIRED_DESKTOP, designerDesktop);
        designerDesktop.setAttribute(ATTR_PAIRED_DESKTOP, canvasDesktop);

    }

    @Listen("onTemplateDropped=#canvas")
    public void onTemplateDropped(Event event) {
        String template = (String) ((Map) event.getData()).get("template");
        notNull(template);
        String parentUuid = (String) ((Map) event.getData()).get("parent");
        notNull(parentUuid);

        String templatePath = "/template/" + template;
        if (Executions.getCurrent().getDesktop().getWebApp().getResource(templatePath) != null) {
            Component parent = getComponentByUuid(parentUuid);
            Executions.getCurrent().createComponents(templatePath, parent, null);
        }
    }

    @Listen("onWidgetSelected=#canvas")
    public void onWidgetSelected(Event event) throws InterruptedException {
        String target = (String) ((Map) event.getData()).get("target");
        notNull(target);

//        Desktop designerDesktop= (Desktop) Executions.getCurrent().getDesktop().getAttribute
//                (ATTR_PAIRED_DESKTOP);
//        designerDesktop.enableServerPush(true);
//        Executions.getCurrent().getDesktop().enableServerPush(true);
//        Executions.activate(designerDesktop);
//        queue.publish(event);
//        Executions.deactivate(designerDesktop);
    }
}
