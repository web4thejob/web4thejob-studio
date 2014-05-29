package org.web4thejob.studio.canvas;

import org.web4thejob.studio.controller.impl.DesignerController;
import org.zkoss.io.FileReader;
import org.zkoss.zk.ui.http.SimpleUiFactory;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.sys.RequestInfo;

import javax.servlet.ServletRequest;
import java.io.IOException;


/**
 * Created by e36132 on 29/5/2014.
 */
public class CanvasUiFactory extends SimpleUiFactory {


    @Override
    public PageDefinition getPageDefinition(RequestInfo ri, String path) {
        ServletRequest request = (ServletRequest) ri.getNativeRequest();
        String file = request.getParameter(DesignerController.PARAM_WORK_FILE);
        if (file == null) {
            return super.getPageDefinition(ri, path);
        } else {
            try {
                return super.getPageDefinitionDirectly(ri, new FileReader(file, "UTF-8"), "zul");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
