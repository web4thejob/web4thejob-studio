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

import org.web4thejob.studio.support.JpaUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Textbox;

import java.util.Map;

import static org.zkoss.lang.Generics.cast;

/**
 * Created by Veniamin on 1/7/2014.
 */
public class JpaConnectionConfigController extends SelectorComposer<Component> {
    @Wire
    private Panel jpaConnPanel;
    @Wire
    private Button btnCancel;
    @Wire
    private Textbox txtDriver;
    @Wire
    private Textbox txtUrl;
    @Wire
    private Textbox txtUser;
    @Wire
    private Textbox txtPassword;
    private String name;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        name = Executions.getCurrent().getArg().get("name").toString();
    }

    @Listen("onClick=#btnSave")
    public void onSaveClicked() throws Exception {
        A a = (A) jpaConnPanel.getAttribute("target");

        Map<String, String> properties = JpaUtil.getConnectionProperties(name);
        properties.clear();
        properties.put("name", name);

        if (txtDriver.getValue().trim().length() > 0) {
            properties.put("driver", txtDriver.getValue().trim());
        }
        if (txtUrl.getValue().trim().length() > 0) {
            properties.put("url", txtUrl.getValue().trim());
        }
        if (txtUser.getValue().trim().length() > 0) {
            properties.put("user", txtUser.getValue().trim());
        }
        if (txtPassword.getValue().trim().length() > 0) {
            properties.put("password", txtPassword.getValue().trim());
        }

        EventListener<Event> callback = cast(jpaConnPanel.getAttribute("callback"));
        callback.onEvent(new Event("onConfigChanged", a));
    }
}
