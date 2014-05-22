package org.web4thejob.studio.http;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.sys.PageCtrl;

/**
 * Created by e36132 on 22/5/2014.
 */
public class StudioRichlet extends GenericRichlet {

    @Override
    public void service(Page page) throws Exception {
        String uri;

        switch (page.getRequestPath()) {
            case "/":
                uri = "~./designer.zul";
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
            default:
                uri = "~." + page.getRequestPath();
        }

        //Hackish but there is no other way to load the styles in the head section of the page,
        //which IMHO is a pretty legitimate request in the context of a Richlet.
        ((PageCtrl) page).addAfterHeadTags("<link type=\"text/css\" rel=\"stylesheet\" " +
                "href=\"/w4tjstudio-support/styles\"></link>\n");


        Executions.createComponents(uri, null, null);
    }


}
