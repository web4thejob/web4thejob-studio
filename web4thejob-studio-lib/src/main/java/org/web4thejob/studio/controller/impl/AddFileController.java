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

import org.web4thejob.studio.support.Assert;
import org.web4thejob.studio.support.FileUtils;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class AddFileController extends SelectorComposer<Component> {
    File parentDir;
    @Wire
    private Window win;
    @Wire
    private Textbox txtFilename;
    private DashboardController dashboardController;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        StudioUtil.clearAlerts();

        String location = (String) Executions.getCurrent().getArg().get("location");
        Assert.assertTrue(location != null, "Attribute location is missing");
        parentDir = new File(location);
        Assert.assertTrue(parentDir.exists(), "Invalid parent dir: " + location);

        dashboardController = (DashboardController) Executions.getCurrent().getArg().get("dashboardController");
        Assert.assertTrue(dashboardController != null, "Attribute dashboardController is missing");
    }

    @Listen("onClick=#btnCancel;onCancel=#win")
    public void onCancel() {
        StudioUtil.clearAlerts();
        win.detach();
    }

    @Listen("onDoneClicked=#win")
    public void onDone(Event event) {
        StudioUtil.clearAlerts();
        boolean isFile = (boolean) ((Map) event.getData()).get("file");
        String name = txtFilename.getValue().trim();
        if (name.length() == 0) {
            return;
        }

        File file;
        try {
            if (isFile) {
                file = new File(parentDir.getAbsolutePath() + System.getProperty("file.separator") + name + ".zul");
                if (file.exists()) {
                    StudioUtil.showPopover(txtFilename.getUuid(), "error", "File already exists.", true);
                    return;
                }

                FileUtils.writeStringToFile(file, "<zk/>", "UTF-8");
            } else {
                file = new File(parentDir.getAbsolutePath() + System.getProperty("file.separator") + name);
                if (file.exists()) {
                    StudioUtil.showPopover(txtFilename.getUuid(), "error", "Directory already exists.", true);
                    return;
                }

                file.mkdir();

            }

            win.detach();
            dashboardController.buildTree(file.getAbsolutePath());
        } catch (IOException e) {
            StudioUtil.showError(e);
        }
    }

}
