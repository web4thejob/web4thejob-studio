package org.web4thejob.studio.http;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.sys.PageCtrl;

import java.io.IOException;

/**
 * Created by e36132 on 22/5/2014.
 */
public class StudioRichlet extends GenericRichlet {

    private static String getProcessingInstructions(String uri) throws IOException {
        uri = uri.replace("~.", "web");
        Resource resource = new ClassPathResource(uri);
        if (!resource.exists()) return null;

        StringBuilder sb = new StringBuilder();
        for (String s : IOUtils.readLines(resource.getInputStream())) {
            String line = s.trim();
            if (line.startsWith("<?style") && line.endsWith("?>")) {
                sb.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"").append(extractValue(line, "href")).append("\"/>\n");
            } else if (line.startsWith("<?script") && line.endsWith("?>")) {
                sb.append("<script type=\"text/javascript\" src=\"").append(extractValue(line, "src")).append("\" charset=\"UTF-8\"></script>\n");
            } else if (line.startsWith("<zk") && line.endsWith(">")) {
                break;
            }
        }

        return sb.toString();
    }

    private static String extractValue(String s, String key) {
        int start = s.indexOf(key);
        if (start < 0) return null;

        String value = "";
        start += key.length();
        for (int i = start; i < s.length(); i++) {
            String c = s.substring(i, i + 1);
            if (c.equals("=")) {
                value = s.substring(s.indexOf("\"", i + 1) + 1, s.indexOf("\"", i + 2));
                break;
            }
        }
        return value;
    }

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
        String pi = getProcessingInstructions(uri);
        if (pi != null) {
            ((PageCtrl) page).addAfterHeadTags(pi);
        }

        Executions.createComponents(uri, null, null);
    }

}
