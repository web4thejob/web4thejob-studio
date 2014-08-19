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

package org.web4thejob.studio.http;

import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.sys.PageCtrl;

import javax.servlet.http.HttpServletRequest;

import static org.web4thejob.studio.support.StudioUtil.getProcessingInstructions;

/**
 * Created by e36132 on 22/5/2014.
 */
public class StudioRichlet extends GenericRichlet {

    @Override
    public void service(Page page) throws Exception {
        String uri, title = null;

        switch (page.getRequestPath()) {
            case "/":
                uri = "~./dashboard.zul";
                title = "Dashboard";
                break;
            case "/logo":
                uri = "~./logo.zul";
                title = "Logo";
                break;
            case "/dashboard":
                uri = "~./dashboard.zul";
                title = "Dashboard";
                break;
            case "/designer":
                uri = "~./designer.zul";
                String z = Executions.getCurrent().getParameter("z");
                if (z != null) {
                    String[] temp = z.split("/");
                    title = temp[temp.length - 1];
                }
                break;
            case "/about":
                uri = "~./about.zul";
                title = "About";
                break;
            case "/discussion":
                uri = "~./discussion.zul";
                title = "Discussion";
                break;
            case "/exception":
                Execution execution = Executions.getCurrent();
                Throwable e = (Throwable) ((HttpServletRequest) execution.getNativeRequest()).getAttribute("javax.servlet.error.exception");
                page.setAttribute("javax.servlet.error.exception", e);
                page.setTitle("+++Error+++");
                return;
            default:
                uri = "~." + page.getRequestPath();
                title = "";
        }

        page.setTitle("Web4thejob Studio - " + title);


        //Hackish but there is no other way to load the styles in the head section of the page,
        //which IMHO is a pretty legitimate request in the context of a Richlet.
        String pi = getProcessingInstructions(uri);
        if (pi != null) {
            ((PageCtrl) page).addAfterHeadTags(pi);
        }

        Executions.createComponents(uri, null, null);
    }

}
