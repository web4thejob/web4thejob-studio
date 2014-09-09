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

import org.web4thejob.studio.support.CookieUtil;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import java.io.File;

public class SourceWebappController extends SelectorComposer<Window> {
    @Wire
    private Window win;
    @Wire
    private Textbox txtPath;

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
    }

    @Listen("onClick=#btnCancel;onCancel=#win")
    public void onCancel() {
        StudioUtil.clearAlerts();
        win.detach();
    }

    @Listen("onDoneClicked=#win")
    public void onDone(Event event) {
        StudioUtil.clearAlerts();
        String name = txtPath.getValue().trim();
        if (name.length() == 0) return;

        File path = new File(name);
        if (!path.exists() || !path.isDirectory()) {
            StudioUtil.showPopover(txtPath.getUuid(), "error", "<strong>This path does not exist.</strong> Please provide a valid path for your file system.", true);
            return;
        }

        String key = CookieUtil.comformCookieName(Executions.getCurrent().getDesktop().getWebApp().getRealPath("/"));
        CookieUtil.setCookie((javax.servlet.http.HttpServletResponse) Executions.getCurrent().getNativeResponse(), key, path.getAbsolutePath());

        Executions.getCurrent().sendRedirect(null);

        win.detach();
    }
}
