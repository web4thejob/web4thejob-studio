package org.web4thejob.studio.canvas;

import org.w3c.dom.NodeList;
import org.web4thejob.studio.controller.impl.DesignerController;
import org.web4thejob.studio.support.StudioUtil;
import org.zkoss.idom.Document;
import org.zkoss.idom.input.SAXBuilder;
import org.zkoss.io.FileReader;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.http.SimpleUiFactory;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.sys.RequestInfo;

import javax.servlet.ServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by e36132 on 29/5/2014.
 */
public class CanvasUiFactory extends SimpleUiFactory {
    private static CanvasAuService canvasAuService = new CanvasAuService();

    private static void cleanUUIDs(org.zkoss.idom.Document document) throws XPathExpressionException {
        String expression = "descendant-or-self::*[@uuid]";
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            ((org.zkoss.idom.Element) nodeList.item(i)).removeAttribute("uuid");
        }
    }

    @Override
    public Desktop newDesktop(RequestInfo ri, String updateURI, String path) {
        Desktop desktop = super.newDesktop(ri, updateURI, path);
        desktop.addListener(canvasAuService);
        return desktop;
    }

    @Override
    public PageDefinition getPageDefinition(RequestInfo ri, String path) {
        ServletRequest request = (ServletRequest) ri.getNativeRequest();
        String file = request.getParameter(DesignerController.PARAM_WORK_FILE);
        if (file == null) {
            return super.getPageDefinition(ri, path);
        } else {
            try {
                Document document = new SAXBuilder(true, false, true).build(new FileReader(file, "UTF-8"));
                cleanUUIDs(document);
                return super.getPageDefinitionDirectly(ri, document, "zul");
            } catch (Exception e) {
                Map<String, String> data = new HashMap<>();
                data.put("exception", (e.getMessage() != null ? e.getMessage() : e.toString()));
                StudioUtil.sendToDesigner("onCanvasFailed", data);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Page newPage(RequestInfo ri, PageDefinition pagedef, String path) {
        try {
            return super.newPage(ri, pagedef, path);
        } catch (Exception e) {
            Map<String, String> data = new HashMap<>();
            data.put("exception", (e.getMessage() != null ? e.getMessage() : e.toString()));
            StudioUtil.sendToDesigner("onCanvasFailed", data);
            throw new RuntimeException(e);
        }
    }
}
