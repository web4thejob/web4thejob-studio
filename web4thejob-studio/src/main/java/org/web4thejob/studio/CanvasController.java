package org.web4thejob.studio;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.sys.WebAppCtrl;

import java.util.Map;

import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.StudioUtil.ATTR_PAIRED_DESKTOP_ID;

/**
 * Created by Veniamin on 9/5/2014.
 */
public class CanvasController extends SelectorComposer<Component> {


    @Listen("onPairedWithDesigner=#canvas")
    public void onPairedWithDesigner(Event event) {
        String designerDesktopId = (String) ((Map) event.getData()).get("designerDesktopId");
        notNull(designerDesktopId);
        String canvasDesktopId = Executions.getCurrent().getDesktop().getId();
        notNull(canvasDesktopId);

        Executions.getCurrent().getDesktop().setAttribute(ATTR_PAIRED_DESKTOP_ID, designerDesktopId);
        ((WebAppCtrl) Executions.getCurrent().getDesktop().getWebApp()).getDesktopCache(Executions.getCurrent()
                .getSession()).getDesktop(designerDesktopId).setAttribute(ATTR_PAIRED_DESKTOP_ID, canvasDesktopId);
    }

    @Listen("onTemplateDropped=#canvas")
    public void onTemplateDropped(Event event) {
        String template = (String) ((Map) event.getData()).get("template");
        notNull(template);
        String parentUuid = (String) ((Map) event.getData()).get("parent");
        notNull(parentUuid);

        String templatePath = "/template/" + template;
        if (Executions.getCurrent().getDesktop().getWebApp().getResource(templatePath) != null) {
            Component parent = Executions.getCurrent().getDesktop().getComponentByUuid(parentUuid);
            Executions.getCurrent().createComponents(templatePath, parent, null);
        }
    }
}
