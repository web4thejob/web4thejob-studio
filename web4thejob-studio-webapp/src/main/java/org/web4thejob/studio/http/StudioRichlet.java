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
        String uri;

        page.setTitle("Web4thejob Studio - " + Executions.getCurrent().getDesktop().getWebApp().getAppName());
        switch (page.getRequestPath()) {
            case "/":
                uri = "~./dashboard.zul";
                break;
            case "/dashboard":
                uri = "~./dashboard.zul";
                break;
            case "/designer":
                uri = "~./designer.zul";
                break;
            case "/about":
                uri = "~./about.zul";
                break;
            case "/discussion":
                uri = "~./discussion.zul";
                break;
            case "/exception":
                Execution execution = Executions.getCurrent();
                Exception e = (Exception) ((HttpServletRequest) execution.getNativeRequest()).getAttribute("javax.servlet.error.exception");
                page.setAttribute("javax.servlet.error.exception", e);
                page.setTitle("+++Error+++");
                return;
            default:
                uri = "~." + page.getRequestPath();
        }

        //Hackish but there is no other way to load the styles in the head section of the page,
        //which IMHO is a pretty legitimate request in the context of a Richlet.
        String pi = getProcessingInstructions(uri);
        if (pi != null) {
            ((PageCtrl) page).addAfterHeadTags(pi);
        }

        Executions.createComponents(uri, null, null);
    }

}
