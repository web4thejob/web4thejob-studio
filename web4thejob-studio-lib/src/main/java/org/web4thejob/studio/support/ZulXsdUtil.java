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

package org.web4thejob.studio.support;

import nu.xom.*;
import org.zkoss.util.resource.Locators;

import javax.xml.XMLConstants;
import java.util.*;

/**
 * Created by e36132 on 14/5/2014.
 */
public abstract class ZulXsdUtil {
    private static final ElementComparator ELEMENT_COMPARATOR = new ElementComparator();
    private static final SortedMap<String, SortedSet<Element>> emptyMap = new TreeMap<>();
    private static final SortedSet<String> emptySet = new TreeSet<>();
    private static final Document xsdDocument = new XsdDocumentBuilder().build();
    public static final String ZUL_NS = "http://www.zkoss.org/2005/zul";
    public static final XPathContext XPATH_CONTEXT_XS = new XPathContext("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI);

    public static SortedSet<String> getWidgetChildren(String widget) {
        SortedSet<String> result = new TreeSet<>();
        Element root = xsdDocument.getRootElement();

        Nodes nodes = root.query("xs:element[@name='" + widget + "']", XPATH_CONTEXT_XS);
        if (nodes.size() != 1) return emptySet;
        Element element = (Element) nodes.get(0);

        nodes = root.query("xs:complexType[@name='" + element.getAttributeValue("type") + "']//xs:element",
                XPATH_CONTEXT_XS);
        for (int i = 0; i < nodes.size(); i++) {
            result.add(((Element) nodes.get(i)).getAttributeValue("ref"));
        }

        nodes = root.query("xs:complexType[@name='" + element.getAttributeValue("type") + "']//xs:group",
                XPATH_CONTEXT_XS);
        for (int i = 0; i < nodes.size(); i++) {
            getWidgetChildren(((Element) nodes.get(i)).getAttributeValue("ref"), result);
        }

        return result;
    }

    private static void getWidgetChildren(String group, SortedSet<String> result) {
        Element root = xsdDocument.getRootElement();

        Nodes nodes = root.query("xs:group[@name='" + group + "']//xs:element", XPATH_CONTEXT_XS);
        for (int i = 0; i < nodes.size(); i++) {
            result.add(((Element) nodes.get(i)).getAttributeValue("ref"));
        }

        nodes = root.query("xs:group[@name='" + group + "']//xs:group", XPATH_CONTEXT_XS);
        for (int i = 0; i < nodes.size(); i++) {
            getWidgetChildren(((Element) nodes.get(i)).getAttributeValue("ref"), result);
        }
    }

    public static SortedMap<String, SortedSet<Element>> getWidgetDescription(String widget) {
        SortedMap<String, SortedSet<Element>> result = new TreeMap<>();
        Element root = xsdDocument.getRootElement();

        Nodes nodes = root.query("xs:element[@name='" + widget + "']", XPATH_CONTEXT_XS);
        if (nodes.size() != 1) return emptyMap;
        Element element = (Element) nodes.get(0);

        nodes = root.query("xs:complexType[@name='" + element.getAttributeValue("type") + "']", XPATH_CONTEXT_XS);
        if (nodes.size() != 1) return emptyMap;
        Element type = (Element) nodes.get(0);

        for (int i = 0; i < type.getChildElements().size(); i++) {
            Element attr = type.getChildElements().get(i);
            if (attr.getLocalName().equals("attribute")) {

                if (!result.containsKey(widget)) {
                    result.put(widget, new TreeSet<>(ELEMENT_COMPARATOR));
                }
                result.get(widget).add(attr);

            } else if (attr.getLocalName().equals("attributeGroup")) {
                populateAttributeGroups(attr.getAttributeValue("ref"), result);
            }

        }


        return result;
    }

    private static void populateAttributeGroups(String groupName, Map<String, SortedSet<Element>> result) {
        Element root = xsdDocument.getRootElement();

        Nodes nodes = root.query("xs:attributeGroup[@name='" + groupName + "']", XPATH_CONTEXT_XS);
        if (nodes.size() != 1) return;
        Element attrGroup = (Element) nodes.get(0);

        for (int i = 0; i < attrGroup.getChildElements().size(); i++) {
            Element attr = attrGroup.getChildElements().get(i);
            if (attr.getLocalName().equals("attribute")) {

//                if (isBannedProperty(attr.getAttributeValue("name"))) continue;

                if (!result.containsKey(groupName)) {
                    result.put(groupName, new TreeSet<>(ELEMENT_COMPARATOR));
                }
                result.get(groupName).add(attr);

            } else if (attr.getLocalName().equals("attributeGroup")) {
                populateAttributeGroups(attr.getAttributeValue("ref"), result);
            }
        }
    }

    public static List<String> getConstraintForAttributeType(String attributeType) {
        Element root = xsdDocument.getRootElement();

        Nodes nodes = root.query("xs:simpleType[@name='" + attributeType + "']//xs:restriction/xs:enumeration",
                XPATH_CONTEXT_XS);
        if (nodes.size() == 0) return Collections.emptyList();

        List<String> restrictions = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            restrictions.add(((Element) nodes.get(i)).getAttributeValue("value"));
        }

        return restrictions;
    }

    public static String getTypeOfAttribute(String element, String propertyName) {
        SortedMap<String, SortedSet<Element>> propsMap = getWidgetDescription(element);
        for (SortedSet<Element> group : propsMap.values()) {
            for (Element property : group) {
                if (property.getAttributeValue("name").equals(propertyName)) {
                    return property.getAttributeValue("type");
                }
            }
        }

        return null;
    }

    public static boolean isBaseGroupElement(Element element) {
        Element root = xsdDocument.getRootElement();
        Nodes nodes = root.query("xs:group[@name='baseGroup']//xs:element[@ref='" + element.getLocalName() + "']",
                XPATH_CONTEXT_XS);
        return nodes.size() == 1;
    }

    public static String getXPath(Element element) {
        if (element == null) return null;
        Node parent = element.getParent();
        if (parent == null || !(parent instanceof Element)) {
            return "/" + element.getQualifiedName();
        }
        return getXPath((Element) parent) + "/" + element.getQualifiedName() + "[" + getOccurenceOfChild((Element)
                parent, element) + "]";
    }

    private static int getOccurenceOfChild(Element parent, Element child) {
        int occurence = 0;
        for (int i = 0; i < parent.getChildElements().size(); i++) {
            if (parent.getChildElements().get(i).getQualifiedName().equals(child.getQualifiedName())) {
                occurence++;
            }
            if (parent.getChildElements().get(i).equals(child)) {
                return occurence;
            }
        }
        return 0;
    }

    private static class XsdDocumentBuilder {

        Document build() {
            try {
                return new Builder(false).build(Locators.getDefault().getResourceAsStream("org/web4thejob/studio/zul.xsd")
                        , null);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    private static class ElementComparator implements Comparator<Element> {

        @Override
        public int compare(Element o1, Element o2) {
            return o1.getAttributeValue("name").compareTo(o2.getAttributeValue("name"));
        }
    }
}


