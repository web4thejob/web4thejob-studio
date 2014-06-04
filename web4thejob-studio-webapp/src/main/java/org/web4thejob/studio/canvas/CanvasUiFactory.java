package org.web4thejob.studio.canvas;

import org.w3c.dom.NodeList;
import org.web4thejob.studio.controller.impl.DesignerController;
import org.zkoss.idom.Document;
import org.zkoss.idom.input.SAXBuilder;
import org.zkoss.io.FileReader;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.http.SimpleUiFactory;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.sys.RequestInfo;

import javax.servlet.ServletRequest;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


/**
 * Created by e36132 on 29/5/2014.
 */
public class CanvasUiFactory extends SimpleUiFactory {
    private static final CanvasAuService canvasAuService = new CanvasAuService();
    private static final XPathExpression xPathExpression;

    static {
        try {
            xPathExpression = XPathFactory.newInstance().newXPath().compile("descendant-or-self::*[@uuid]");
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void cleanUUIDs(org.zkoss.idom.Document document) throws XPathExpressionException {
        NodeList nodeList = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
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
        if (file == null || path.startsWith("~")) {
            return super.getPageDefinition(ri, path);
        } else {
            try {
                //I use here the zk Document so that I will not have to parse twice the document
                //in order to remove the uuids and then feed it to the getPageDefinitionDirectly.
                //Alternatively I could do the uuid discarding wiht regex replace?!?
                Document document = new SAXBuilder(true, false, true).build(new FileReader(file, "UTF-8"));
                cleanUUIDs(document);
                return super.getPageDefinitionDirectly(ri, document, "zul");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
