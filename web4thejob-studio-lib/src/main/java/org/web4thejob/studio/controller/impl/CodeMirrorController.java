/*
 * Copyright 2014 Veniamin Isaias
 *
 * This file is part of Web4thejob Studio.
 *
 * Web4thejob Studio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Web4thejob Studio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Web4thejob Studio.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.web4thejob.studio.controller.impl;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Text;
import org.apache.commons.lang3.StringUtils;
import org.web4thejob.studio.controller.AbstractController;
import org.web4thejob.studio.controller.ControllerEnum;
import org.web4thejob.studio.message.MessageEnum;
import org.web4thejob.studio.support.CodeFormatter;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.Validate.notNull;
import static org.web4thejob.studio.controller.ControllerEnum.CODE_MIRROR_CONTROLLER;
import static org.web4thejob.studio.support.StudioUtil.getEventCodeNode;

/**
 * Created by e36132 on 17/6/2014.
 */
public class CodeMirrorController extends AbstractController {
    private String mode;
    private Element element;
    private String eventName;
    private Element codeBlock;
    private boolean isServerSide;
    @Wire
    private Textbox editor;
    @Wire
    private Button btnSave;

    @Override
    public ControllerEnum getId() {
        return CODE_MIRROR_CONTROLLER;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        element = (Element) Executions.getCurrent().getArg().get("element");
        if (element == null) return;
        mode = (String) Executions.getCurrent().getArg().get("mode");
        notNull(element);
        eventName = (String) Executions.getCurrent().getArg().get("event");

        if (eventName != null) {
            isServerSide = !"javascript".equals(mode);
            codeBlock = (Element) getEventCodeNode(element, eventName, isServerSide);
            if (codeBlock != null) {
                editor.setValue(CodeFormatter.formatJS(codeBlock.getValue().trim()));
            }
        } else {
            codeBlock = element;
            switch (mode) {
                case "css":
                    editor.setValue(CodeFormatter.formatCSS(codeBlock.getValue().trim()));
                    break;
                case "text/html":
                    editor.setValue(CodeFormatter.formatHTML(codeBlock.getValue().trim()));
                    break;
                default:
                    editor.setValue(CodeFormatter.formatJS(codeBlock.getValue().trim()));
                    break;
            }
        }

        Clients.evalJavaScript("var cm=zk('" + editor.getUuid() + "').$(); if(cm) {cm=cm.get('codemirror'); cm.refresh(); cm.focus();};");

        btnSave.addEventListener(Events.ON_CLICK, new onSaveClicked());

    }

    @Listen("onClick=#btnDialog")
    public void onToDialogClicked(MouseEvent event) {
        Map<String, Object> args = new HashMap<>();
        args.put("mode", mode);
        args.put("element", element);
        args.put("event", eventName);
        Executions.createComponents("~./include/codedialog.zul", null, args);
    }

    private class onSaveClicked implements EventListener<MouseEvent> {

        @Override
        public void onEvent(MouseEvent event) throws Exception {
            if (StringUtils.isBlank(editor.getValue())) {
                if (codeBlock != null) codeBlock.detach();
            } else {

                Text cdata = new Text(editor.getValue().trim());
                if (codeBlock != null) {
                    codeBlock.removeChildren();
                } else if (eventName != null) {
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


            publish(MessageEnum.EVALUATE_XML);

        }


    }
}
