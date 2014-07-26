import nu.xom.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.zkoss.util.resource.Locators;
import org.zkoss.zk.ui.metainfo.ComponentDefinition;

import java.io.IOException;
import java.util.*;

import static org.web4thejob.studio.support.StudioUtil.getDefinitionByTag;
import static org.web4thejob.studio.support.ZulXsdUtil.*;

/**
 * Created by e36132 on 27/3/2014.
 */
@RunWith(JUnit4.class)
public class XSDtoTags {


    @Test
    public void prepareTags() throws IOException, ParsingException {
        StringBuilder sb = new StringBuilder();
        sb.append("var tags={");
        sb.append("\n");

        Builder parser = new Builder(false);
        Document document = parser.build(Locators.getDefault().getResourceAsStream("org/web4thejob/studio/zul.xsd"), null);
        XPathContext ctx = new XPathContext("xs", "http://www.w3.org/2001/XMLSchema");

        Nodes topElements = document.getRootElement().query("xs:group//xs:element", ctx);
        sb.append("\t").append("\"!top\": [");
        for (int i = 0; i < topElements.size(); i++) {
            Element e = (Element) topElements.get(i);

            ComponentDefinition definition = getDefinitionByTag(e.getAttributeValue("ref"));
            if (definition != null) {
                sb.append("\"").append(e.getAttributeValue("ref")).append("\"").append((topElements.size() - 1 > i ? ", " : ""));
            }

        }
        sb.append("],").append("\n");

        topElements = document.getRootElement().query("xs:element", ctx);
        for (int i = 0; i < topElements.size(); i++) {
            Element topE = (Element) topElements.get(i);

            ComponentDefinition definition = getDefinitionByTag(topE.getAttributeValue("name"));
            if (definition == null) continue;

            SortedMap<String, SortedSet<Element>> desc = getWidgetDescription(topE.getAttributeValue("name"));

            //sort attributes alphabetically
            List<String> attributes = new ArrayList<>();
            for (SortedSet<Element> set : desc.values()) {
                for (Element e : set) {
                    attributes.add(e.getAttributeValue("name"));
                }
            }
            Collections.sort(attributes);

            if (!attributes.isEmpty()) {
                sb.append("\t").append(topE.getAttributeValue("name")).append(": {\n");
                sb.append("\t").append("\t").append("attrs").append(": {\n");
                for (String attr : attributes) {
//                    if (StudioUtil.bannedProperties.contains("[" + attr + "]")) continue;

                    sb.append("\t").append("\t").append("\t").append(attr).append(": ");

                    List<String> restrictions;
                    if (!"mold".equals(attr)) {
                        restrictions = getConstraintForAttributeType(getTypeOfAttribute(topE.getAttributeValue("name"), attr));
                    } else {
                        restrictions = new ArrayList<>();
                        restrictions.addAll(definition.getMoldNames());
                    }

                    if (!restrictions.isEmpty()) {
                        sb.append("[");
                        for (int j = 0; j < restrictions.size(); j++) {
                            String restriction = restrictions.get(j);
                            sb.append("\"").append(restriction).append("\"").append(restrictions.size() - 1 > j ? ", " : "");
                        }
                        sb.append("]");
                    } else {
                        sb.append("null");
                    }

                    sb.append(attr.equals(attributes.get(attributes.size() - 1)) ? "\n" : ",\n");
                }
                sb.append("\t").append("\t").append("},").append("\n");

                //eligible children
                SortedSet<String> children = getWidgetChildren(topE.getAttributeValue("name"));
                Iterator<String> iter = children.iterator();
                while (iter.hasNext()) {
                    if (getDefinitionByTag(iter.next()) == null) iter.remove();
                }

                if (!children.isEmpty()) {
                    sb.append("\t").append("\t").append("children").append(": [");
                    for (String child : children) {
                        sb.append("\"").append(child).append("\"").append(child.equals(children.last()) ? "" : ", ");
                    }
                    sb.append("]").append("\n");
                } else {
                    sb.append("\t").append("\t").append("children").append(": []").append("\n");
                }
                sb.append("\t").append("}").append((topElements.size() - 1 > i ? ", " : "")).append("\n");
            }

        }

        sb.append("\n").append("};");

        System.out.println(sb.toString());
    }

}
