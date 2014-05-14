package org.web4thejob.studio.support;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import org.springframework.util.ClassUtils;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;
import org.web4thejob.studio.CodeController;
import org.web4thejob.studio.Controller;
import org.web4thejob.studio.ControllerEnum;
import org.zkoss.json.JSONValue;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static org.web4thejob.studio.ControllerEnum.CANVAS_CONTROLLER;
import static org.web4thejob.studio.ControllerEnum.CODE_CONTROLLER;
import static org.web4thejob.studio.support.ZulXsdUtil.XPATH_CONTEXT_ZUL;
import static org.zkoss.lang.Generics.cast;

/**
 * Created by Veniamin on 9/5/2014.
 */
public abstract class StudioUtil {
    public static final String ATTR_PAIRED_DESKTOP = "paired-desktop-id";
    public static final String ATTR_STUDIO_CONTROLLERS = "studio-controllers";
    public static final String ATTR_CANVAS_UUID = "canvas-uuid";
    private static Map<Class<? extends Component>, Component> defaults = cast(Collections.synchronizedMap(new HashMap
            ()));

    public static boolean isCanvasDesktop() {
        return Executions.getCurrent().getDesktop().getRequestPath().contains("canvas.zul");
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
        Clients.evalJavaScript("top.w4tjStudioDesigner.alert('" + clazz + "','" + title +
                "','" + message + "'," + Boolean.valueOf(autoclose).toString() + ")");

    }

    public static void clearCanvasBusy(String uuid) {
        isTrue(!isCanvasDesktop(), "Call clearBusy directly");
        Clients.evalJavaScript("top.w4tjStudioDesigner.clearCanvasBusy(" + (uuid != null ? "'" + uuid + "'" : "") +
                ")");
    }

    public static void showError(Exception e) {
        showError(e, false);
    }

    public static void showError(Exception e, boolean autoclosable) {
        showNotification("danger", "Ooops!", e.getMessage(), autoclosable);
    }

    public static void sendToDesigner(String eventName, Object data) {
        isTrue(isCanvasDesktop(), "Use it only from canvas desktop");
        Clients.evalJavaScript("w4tjStudioCanvas.sendToDesigner('" + eventName + "'," +
                "" + JSONValue.toJSONString(data) + ")");
    }

    public static void sendToCanvas(String eventName, Object data) {
        isTrue(!isCanvasDesktop(), "Use it only from designer desktop");
        Clients.evalJavaScript("w4tjStudioDesigner.sendToCanvas('" + eventName + "'," +
                "" + JSONValue.toJSONString(data) + ")");
    }

    /**
     * Restricted for read only use
     *
     * @return
     */
    public static Desktop getPairedDesktop() {
        return (Desktop) Executions.getCurrent().getDesktop().getAttribute(ATTR_PAIRED_DESKTOP);
    }

    private static <T extends Controller> T getController(ControllerEnum id) {
        Desktop desktop;
        if (!(isCanvasDesktop() && id == CANVAS_CONTROLLER)) {
            desktop = getPairedDesktop();
        } else {
            desktop = Executions.getCurrent().getDesktop();
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
        return ((Element) ((CodeController) getController(CODE_CONTROLLER)).getCode().getRootElement().query
                ("descendant-or-self::*[@uuid='" + uuid + "']").get(0));
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
        Nodes nodes = parent.query("descendant-or-self::*[@uuid]", XPATH_CONTEXT_ZUL);
        for (int i = 0; i < nodes.size(); i++) {
            Attribute uuid = ((Element) nodes.get(i)).getAttribute("uuid");
            ((Element) nodes.get(i)).removeAttribute(uuid);
        }
    }

    public static boolean isEventElement(String tag) {
        return "attribute".equals(tag) || "custom-attributes".equals(tag);
    }

}
