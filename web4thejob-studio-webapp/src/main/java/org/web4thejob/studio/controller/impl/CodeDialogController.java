package org.web4thejob.studio.controller.impl;

import nu.xom.Element;
import org.web4thejob.studio.controller.AbstractController;
import org.web4thejob.studio.controller.ControllerEnum;
import org.web4thejob.studio.message.Message;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Window;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.controller.ControllerEnum.CODE_DIALOG_CONTROLLER;

/**
 * Created by e36132 on 19/5/2014.
 */
public class CodeDialogController extends AbstractController {
    @Wire
    private Window editorWindow;
    @Wire
    private Panel editorPanel;
    private Element element;
    private String eventName;
    private String mode;
    private boolean isServerSide;


    @Override
    public ControllerEnum getId() {
        return CODE_DIALOG_CONTROLLER;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        editorWindow.setAttribute("controller", this);
        mode = (String) Executions.getCurrent().getArg().get("mode");
        notNull(mode);
        element = (Element) Executions.getCurrent().getArg().get("element");
        notNull(element);
        eventName = (String) Executions.getCurrent().getArg().get("event");
        notNull(eventName);

        isServerSide = !"javascript".equals(mode);

        editorPanel.setTitle("<strong class=\"label label-primary\" style=\"font-size:120%;font-family:monospace\">"
                + eventName + "</strong> <strong>" + (isServerSide ? "Java" : "Javascript") + "</strong> handler on " +
                "the " + (isServerSide ? "server" : "browser"));
        editorWindow.addEventListener(Events.ON_CANCEL, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                editorWindow.detach();
            }
        });

        Map<String, Object> data = new HashMap<>();
        data.put("element", element);
        data.put("mode", mode);
        data.put("event", eventName);
        Executions.getCurrent().createComponents("~./include/codemirror.zul", editorPanel.getPanelchildren(), data);

    }

    @Override
    public void process(Message message) {
        switch (message.getId()) {
            case EVALUATE_XML:
                if (editorWindow != null) editorWindow.detach();
                break;
        }
    }
}
