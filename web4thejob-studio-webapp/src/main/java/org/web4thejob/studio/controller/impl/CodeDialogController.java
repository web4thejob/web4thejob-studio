package org.web4thejob.studio.controller.impl;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Text;
import org.springframework.util.StringUtils;
import org.web4thejob.studio.controller.AbstractController;
import org.web4thejob.studio.controller.ControllerEnum;
import org.web4thejob.studio.message.MessageEnum;
import org.web4thejob.studio.support.CodeFormatter;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.controller.ControllerEnum.CODE_DIALOG_CONTROLLER;
import static org.web4thejob.studio.support.StudioUtil.getEventCodeNode;

/**
 * Created by e36132 on 19/5/2014.
 */
public class CodeDialogController extends AbstractController {
    @Wire
    private Window editorWindow;
    @Wire
    private Panel editorPanel;
    @Wire
    private Button bntCancel;
    @Wire
    private Button btnOK;
    @Wire
    private Textbox editor;
    private Element element;
    private String eventName;
    private String side;
    private Element codeBlock;
    private Button button;
    private boolean isServerSide;


    @Override
    public ControllerEnum getId() {
        return CODE_DIALOG_CONTROLLER;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        editorWindow.setAttribute("controller", this);
        button = (Button) Executions.getCurrent().getArg().get("button");
        notNull(button);
        side = (String) Executions.getCurrent().getArg().get("side");
        notNull(side);
        element = (Element) Executions.getCurrent().getArg().get("element");
        notNull(element);
        eventName = (String) Executions.getCurrent().getArg().get("property");
        notNull(eventName);

        isServerSide = "server".equals(side);
        codeBlock = (Element) getEventCodeNode(element, eventName, isServerSide);
        if (codeBlock != null) {
            editor.setValue(CodeFormatter.formatJS(codeBlock.getValue().trim()));
        }

        editorPanel.setTitle("<strong class=\"label label-primary\" style=\"font-size:120%;font-family:monospace\">"
                + eventName + "</strong> <strong>" + (isServerSide ? "Java" : "Javascript") + "</strong> handler on " +
                "the " + side);
        Clients.evalJavaScript("eventEditor.refresh();eventEditor.focus();");

        editorWindow.addEventListener(Events.ON_CANCEL, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                editorWindow.detach();
            }
        });

        btnOK.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {

                if (!StringUtils.hasText(editor.getValue())) {
                    if (codeBlock != null) codeBlock.detach();
                } else {

                    Text cdata = new Text(editor.getValue().trim());
                    if (codeBlock != null) {
                        codeBlock.removeChildren();
                    } else {
                        codeBlock = new Element("attribute");
                        if (isServerSide) {
                            codeBlock.addAttribute(new Attribute("name", eventName));
                        } else {
                            String clientPrefix = StudioUtil.getClientNamespacePrefix((org.web4thejob.studio.dom.Element) element);
                            if (clientPrefix == null) {
                                clientPrefix = "c";
                                element.getDocument().getRootElement().addNamespaceDeclaration(clientPrefix, "client");
                            }

                            codeBlock.addAttribute(new Attribute(clientPrefix + ":name", "client", eventName));
                        }
                        element.insertChild(codeBlock, 0);
                    }
                    codeBlock.appendChild(cdata);
                }

                editorWindow.detach();

                if (getEventCodeNode(element, eventName, isServerSide) != null) {
                    button.setSclass("btn-primary btn-xs");
                } else {
                    button.setSclass("btn-default btn-xs");
                }

                publish(MessageEnum.EVALUATE_XML);
            }
        });
    }

}
