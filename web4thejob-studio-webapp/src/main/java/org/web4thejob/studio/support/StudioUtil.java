package org.web4thejob.studio.support;

import nu.xom.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;
import org.web4thejob.studio.controller.Controller;
import org.web4thejob.studio.controller.ControllerEnum;
import org.web4thejob.studio.controller.impl.CodeController;
import org.web4thejob.studio.dom.NodeFactory;
import org.zkoss.json.JSONValue;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.metainfo.ComponentDefinition;
import org.zkoss.zk.ui.metainfo.LanguageDefinition;
import org.zkoss.zk.ui.util.Clients;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.controller.ControllerEnum.CANVAS_CONTROLLER;
import static org.web4thejob.studio.controller.ControllerEnum.CODE_CONTROLLER;
import static org.zkoss.lang.Generics.cast;

/**
 * Created by Veniamin on 9/5/2014.
 */
public abstract class StudioUtil {
    public static final String ATTR_CANVAS_DESKTOP = "i-am-the-canvas-desktop";
    public static final String ATTR_PAIRED_DESKTOP = "paired-desktop-id";
    public static final String ATTR_STUDIO_CONTROLLERS = "studio-controllers";
    public static final String ATTR_CANVAS_UUID = "canvas-uuid";
    public static final String ATTR_CANVAS_FILE = "canvas-file";
    private static Map<Class<? extends Component>, Component> defaults = cast(Collections.synchronizedMap(new
            HashMap<>()));

    public static boolean isCanvasDesktop() {
        return Executions.getCurrent().getDesktop().hasAttribute(ATTR_CANVAS_DESKTOP);
    }

    /**
     * convenience method
     */
    public static Component getComponentByUuid(String uuid) {
        return Executions.getCurrent().getDesktop().getComponentByUuid(uuid);
    }

    /**
     * convenience method
     */
    public static Component getCanvasComponentByUuid(String uuid) {
        isTrue(!isCanvasDesktop(), "Call getComponentByUuid() directly");
        return getPairedDesktop().getComponentByUuid(uuid);
    }


    public static void showNotification(String clazz, String title, String message, boolean autoclose) {
        String m = message.replaceAll("'", "\"");
        Clients.evalJavaScript("top.w4tjStudioDesigner.alert('" + clazz + "','" + title +
                "','" + m + "'," + Boolean.valueOf(autoclose).toString() + ")");

    }

    public static void clearCanvasBusy(String uuid) {
        isTrue(!isCanvasDesktop(), "Call clearBusy directly");
        Clients.evalJavaScript("top.w4tjStudioDesigner.clearCanvasBusy(" + (uuid != null ? "'" + uuid + "'" : "") +
                ")");
    }

    public static void clearAlerts() {
        Clients.evalJavaScript("top.w4tjStudioDesigner.clearAlerts()");
    }


    public static void showError(Exception e) {
        showError(e, false);
    }

    public static void showError(Exception e, boolean autoclosable) {
        showNotification("danger", "Ooops!", (e.getMessage() != null ? e.getMessage() : e.toString()), autoclosable);
    }

    public static void showError(String message, boolean autoclosable) {
        showNotification("danger", "Ooops!", message, autoclosable);
    }


    /**
     * Restricted for read only use
     *
     * @return
     */
    public static Desktop getPairedDesktop() {
        return (Desktop) Executions.getCurrent().getDesktop().getAttribute(ATTR_PAIRED_DESKTOP);
    }


    public static TreeSet<Controller> getLocalControllers() {
        SortedMap<ControllerEnum, Controller> controllers = cast(Executions.getCurrent().getDesktop().getAttribute
                (ATTR_STUDIO_CONTROLLERS));
        notNull(controllers, "called to early");
        return new TreeSet<>(controllers.values());
    }

    private static <T extends Controller> T getController(ControllerEnum id) {
        Desktop desktop;
        if (id == CANVAS_CONTROLLER && isCanvasDesktop() || (id != CANVAS_CONTROLLER && !isCanvasDesktop())) {
            desktop = Executions.getCurrent().getDesktop();
        } else {
            desktop = getPairedDesktop();
        }

        SortedMap<ControllerEnum, Controller> controllers = cast(desktop.getAttribute(ATTR_STUDIO_CONTROLLERS));
        notNull(controllers, "called to early");
        return cast(controllers.get(id));
    }

    public static Document getCode() {
        return ((CodeController) getController(CODE_CONTROLLER)).getCode();
    }

    public static String getCanvasUuid() {
        isTrue(!isCanvasDesktop(), "No need for this, you are in the canvas");
        return (String) Executions.getCurrent().getDesktop().getAttribute(ATTR_CANVAS_UUID);
    }

    public static void traverseChildren(Component parent, Map<String, Object> params,
                                        ChildDelegate<Component> childDelegate) {
        if (params == null) params = new HashMap<>();
        childDelegate.onChild(parent, params);
        for (Component child : parent.getChildren()) {
            traverseChildren(child, new HashMap<>(params), childDelegate);
        }
    }


    public static void traverseChildren(Element parent, Map<String, Object> params,
                                        ChildDelegate<Element> childDelegate) {
        if (params == null) params = new HashMap<>();
        childDelegate.onChild(parent, params);
        for (int i = 0; i < parent.getChildElements().size(); i++) {
            traverseChildren(parent.getChildElements().get(i), new HashMap<>(params), childDelegate);
        }
    }

    public static Element getElementByUuid(final String uuid) {
        return (Element) (getCode().getRootElement().query("descendant-or-self::*[@uuid='" + uuid + "']").get(0));
    }

    public static boolean isDefaultValueForProperty(Component instance, String propertyName, String value,
                                                    boolean isBoolen) {
        if (!defaults.containsKey(instance.getClass())) {
            defaults.put(instance.getClass(), instance.getDefinition().newInstance(null, null));
        }

        try {
            Object defvalue = invokeGetter(defaults.get(instance.getClass()), propertyName, isBoolen);
            if (defvalue != null) {
                return value.equals(defvalue.toString());
            }
            return false;
        } catch (Exception e) {
            //do nothing
        }
        return false;

    }

    public static Object invokeGetter(Object instance, String property, boolean isBoolean) throws NoSuchMethodException,
            ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        MethodInvoker methodInvoker = new MethodInvoker();
        methodInvoker.setTargetClass(instance.getClass());
        methodInvoker.setTargetObject(instance);
        methodInvoker.setTargetMethod((isBoolean ? "is" : "get") + StringUtils.capitalize(property));
        methodInvoker.prepare();
        return methodInvoker.invoke();
    }

    public static boolean hasProperty(Class<?> clazz, String property, boolean isBoolean) {
        return ClassUtils.hasMethod(clazz, (isBoolean ? "is" : "get") + StringUtils.capitalize(property));
    }

    public static boolean isEligibleTypeForXml(Class<?> clazz) {
        return String.class.isAssignableFrom(clazz) || ClassUtils.isPrimitiveOrWrapper(clazz);
    }

    public static void cleanUUIDs(Element parent) {
        Nodes nodes = parent.query("descendant-or-self::*[@uuid]", XPathContext.makeNamespaceContext(parent));
        for (int i = 0; i < nodes.size(); i++) {
            Attribute uuid = ((Element) nodes.get(i)).getAttribute("uuid");
            ((Element) nodes.get(i)).removeAttribute(uuid);
        }
    }

    public static boolean isEventOrAttributeElement(Element element) {
        return isEventElement(element) || "custom-attributes".equals(element.getLocalName());
    }

    public static boolean isEventElement(Element element) {
        return "attribute".equals(element.getLocalName());
    }

    public static boolean isCodeElement(Element element) {
        String tagname = element.getLocalName();
        return "attribute".equals(tagname) || "script".equals(tagname) || "zscript".equals(tagname) || "style".equals
                (tagname) || "html".equals(tagname);
    }

    public static String getQueryParam(String query, String param) {
        if (query == null) return null;
        notNull(param);
        StringTokenizer tokenizer = new StringTokenizer(query, "&", false);
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken().trim();
            if (token.startsWith(param) && token.contains("=")) {
                return token.split("=")[1];
            }
        }
        return null;
    }

    public static String describeElement(Element element) {
        boolean script = element.getLocalName().equals("attribute");

        StringBuilder sb = new StringBuilder();

        String specialStyle = null;
        if (!script) {
            if (element.getAttributeValue("uuid") == null) {
                specialStyle = "w4tjstudio-element-skipped";
            } else if ("false".equals(element.getAttributeValue("visible"))) {
                specialStyle = "w4tjstudio-element-hidden";
            }
        } else {
            if (element.getParent() != null) {
                Element parent = (Element) element.getParent();
                if (parent.getAttributeValue("uuid") == null) {
                    specialStyle = "w4tjstudio-element-skipped";
                } else if ("false".equals(parent.getAttributeValue("visible"))) {
                    specialStyle = "w4tjstudio-element-hidden";
                }
            }
        }

        if (specialStyle != null) {
            sb.append("<span class=\"").append(specialStyle).append("\">");
        }


        sb.append(element.getLocalName()).append(" [");
        if (!script) {
            if (element.getAttributeValue("id") != null) {
                sb.append(element.getAttributeValue("id"));
            } else if (element.getAttributeValue("uuid") != null) {
                sb.append("#").append(element.getAttributeValue("uuid"));
            }
        } else {
            if (element.getAttributeValue("name") != null) {
                sb.append("<span style=\"font-family:monospace\">");
                sb.append(element.getAttributeValue("name")).append("@server");
                sb.append("</span>");
            } else if (element.getAttributeValue("name", getClientNamespace((org.web4thejob.studio.dom.Element) element)) != null) {
                sb.append("<span style=\"font-family:monospace\">");
                sb.append(element.getAttributeValue("name", getClientNamespace((org.web4thejob.studio.dom.Element) element))).append("@client");
                sb.append("</span>");
            }
        }
        sb.append("]");

        if (element.getAttributeValue("label") != null || element.getAttributeValue("value") != null) {
            sb.append(": ").append("<span style=\"color:").append((specialStyle != null ? "inherit" : "#428bca"))
                    .append(";font-style:italic;font-weight:bold;\">");

            String value;
            if (element.getAttributeValue("label") != null) {
                value = element.getAttributeValue("label");
            } else {
                value = element.getAttributeValue("value");
            }


            //EL expressions
            if (specialStyle == null) {
                String nval = "";
                String t1 = "${", t2 = "}";
                if (value.contains(t1)) {
                    int p1 = value.indexOf(t1);
                    int p2 = value.indexOf(t2);
                    if (p2 > p1) {
                        nval = "<code>" + value.substring(p1, p2 + 1) + "</code>";
                        if (p1 > 0) {
                            nval = value.substring(0, p1 - 1) + nval;
                        }
                        if (p2 < value.length() - 1) {
                            nval = nval + value.substring(p2 + 1);
                        }
                        value = nval;
                    }
                }
            }

            //Bind expressions
            if (specialStyle == null) {
                String nval = "";
                String t1 = null, t2 = null;
                for (int j = 1; j <= 3; j++) {
                    switch (j) {
                        case 1:
                            t1 = "@bind(";
                            t2 = ")";
                            break;
                        case 2:
                            t1 = "@load(";
                            t2 = ")";
                            break;
                        case 3:
                            t1 = "@save(";
                            t2 = ")";
                            break;
                    }
                    if (value.contains(t1)) {
                        int p1 = value.indexOf(t1);
                        int p2 = value.indexOf(t2);
                        if (p2 > p1) {
                            nval = "<span class=\"label label-success z-label mono\">" + value.substring(p1,
                                    p2 + 1) + "</span>";
                            if (p1 > 0) {
                                nval = value.substring(0, p1 - 1) + nval;
                            }
                            if (p2 < value.length() - 1) {
                                nval = nval + value.substring(p2 + 1);
                            }
                            value = nval;
                        }
                    }
                }
            }
/*
            if (value.length() > 30) {
                value = value.substring(0, 29) + "...";
            }
*/
            sb.append(value).append("</span>");

        }

        if (element.getAttributeValue("uuid") == null) {
            sb.append("</span");
        }

        return sb.toString();
    }

    public static ComponentDefinition getDefinitionByTag(String tag) {
        for (LanguageDefinition languageDefinition : LanguageDefinition.getByDeviceType("ajax")) {
            ComponentDefinition componentDefinition = languageDefinition.getComponentDefinitionIfAny(tag);
            if (componentDefinition != null) {
                return componentDefinition;
            }
        }
        return null;
    }

    public static Node getEventCodeNode(Element element, String eventName, boolean isServerSide) {
        Nodes nodes;
        if (isServerSide) {
            nodes = element.query("child::*[@name='" + eventName + "']");
        } else {
            String prefix = StudioUtil.getClientNamespacePrefix((org.web4thejob.studio.dom.Element) element);
            nodes = element.query("child::*[@" + prefix + ":name='" + eventName + "']", XPathContext.makeNamespaceContext(element));
        }
        if (nodes.size() != 1) return null;

        return nodes.get(0);
    }

    public static String getProcessingInstructions(String uri) throws IOException {
        uri = uri.replace("~.", "web");
        Resource resource = new ClassPathResource(uri);
        if (!resource.exists()) return null;

        return processProcessingInstructions(IOUtils.readLines(resource.getInputStream()));
    }

    public static String processProcessingInstructions(String[] lines) {
        StringBuilder sb = new StringBuilder();
        for (String s : lines) {
            String line = s.trim();
            if (line.startsWith("<?style") && line.endsWith("?>")) {
                sb.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"").append(extractValue(line, "href",
                        true)).append("\"/>\n");
            } else if (line.startsWith("<?script") && line.endsWith("?>")) {
                sb.append("<script type=\"text/javascript\" src=\"").append(extractValue(line, "src",
                        true)).append("\" charset=\"UTF-8\"></script>\n");
            } else if (line.startsWith("<zk")) {
                break;
            }
        }

        return sb.toString();
    }

    public static String processProcessingInstructions(List<String> lines) {
        return processProcessingInstructions(lines.toArray(new String[lines.size()]));
    }

    private static String extractValue(String s, String key, boolean isPath) {
        int start = s.indexOf(key);
        if (start < 0) return null;

        String value = "";
        start += key.length();
        for (int i = start; i < s.length(); i++) {
            String c = s.substring(i, i + 1);
            if (c.equals("=")) {
                value = s.substring(s.indexOf("\"", i + 1) + 1, s.indexOf("\"", i + 2));

                if (isPath && value.startsWith("/")/*absolute path*/) {
                    String cpath = Executions.getCurrent().getContextPath();
                    if (cpath != null && !cpath.equals("/")/*no root context*/) {
                        value = cpath + value;
                    }
                }

                break;
            }
        }
        return value;
    }


    public static File getCanvasFile() {
        Desktop desktop = (isCanvasDesktop() ? Executions.getCurrent().getDesktop() : getPairedDesktop());
        return new File((String) desktop.getAttribute(ATTR_CANVAS_FILE));
    }

    public static String beautifyXml2(Document document) {
        cleanWellKnownErrors(document);
        final Serializer serializer;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            serializer = new MultiplexSerializer(out);
            serializer.setIndent(2);
            serializer.write(document);
//            serializer.setMaxLength(120);
            serializer.flush();

            String zul = out.toString("UTF-8");
            zul = zul.replaceAll(" xmlns=\"\"", "");//xom bug?
            return zul;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void cleanWellKnownErrors(Document document) {
        XPathContext xpathContext = XPathContext.makeNamespaceContext(document.getRootElement());

        Nodes nodes = document.query("//zul:panelchildren[@vflex]", xpathContext);
        for (int i = 0; i < nodes.size(); i++) {
            Attribute a = ((Element) nodes.get(i)).getAttribute("vflex");
            a.detach();
        }
        nodes = document.query("//zul:treeitem[@label]", xpathContext);
        for (int i = 0; i < nodes.size(); i++) {
            Attribute a = ((Element) nodes.get(i)).getAttribute("label");
            a.detach();
        }
        nodes = document.query("//zul:treerow[@label]", xpathContext);
        for (int i = 0; i < nodes.size(); i++) {
            Attribute a = ((Element) nodes.get(i)).getAttribute("label");
            a.detach();
        }
        nodes = document.query("//zul:tabbox[@selectedIndex]", xpathContext);
        for (int i = 0; i < nodes.size(); i++) {
            Attribute a = ((Element) nodes.get(i)).getAttribute("selectedIndex");
            a.detach();
        }

    }

    public static File buildWorkingFile(Document document) {
        try {
            File f = File.createTempFile("w4tjstudio", "zul");
            f.deleteOnExit();
            FileUtils.writeStringToFile(f, CodeFormatter.formatXML(document), "UTF-8");
            return f;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendToDesigner(String eventName, Object data) {
        Clients.evalJavaScript("w4tjStudioCanvas.sendToDesigner('" + eventName + "'," +
                "" + JSONValue.toJSONString(data) + ")");
    }

    public static Document buildDocument(String content) {
        try {
            return new Builder(false, new NodeFactory()).build(content, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Document buildDocument(InputStream content) {
        try {
            return new Builder(false, new NodeFactory()).build(content, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getClientNamespacePrefix(org.web4thejob.studio.dom.Element element) {
        Map<String, String> ns = element.getNamespacePrefixesInScope();
        for (String prefix : ns.keySet()) {
            String uri = ns.get(prefix);
            if ("client".equals(uri) || LanguageDefinition.CLIENT_NAMESPACE.equals(uri)) {
                return prefix;
            }
        }
        return null;
    }

    public static String getClientNamespace(org.web4thejob.studio.dom.Element element) {
        Map<String, String> ns = element.getNamespacePrefixesInScope();
        return ns.get(getClientNamespacePrefix(element));
    }

}
