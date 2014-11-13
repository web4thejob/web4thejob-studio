package org.web4thejob.studio.demo;

import groovy.lang.GroovyClassLoader;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Created by Veniamin on 24/10/2014.
 */
public class GroovyLoader extends SelectorComposer<Window> {
    @Wire
    private Textbox txtSource;

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        String src = (String) Executions.getCurrent().getDesktop().getWebApp().getAttribute("MyGroovyComposer");
        if (src == null) {
            StringBuilder builder = new StringBuilder();
            builder.append("package foo.bar").append("\n");
            builder.append("import org.zkoss.zk.ui.select.SelectorComposer").append("\n");
            builder.append("import org.zkoss.zk.ui.Component").append("\n");
            builder.append("import org.zkoss.zk.ui.select.annotation.Wire").append("\n");
            builder.append("import org.zkoss.zul.Label").append("\n");
            builder.append("\n");
            builder.append("class MyGroovyComposer extends SelectorComposer<Component> {").append("\n");
            builder.append("	@Wire").append("\n");
            builder.append("	Label lblHello").append("\n");
            builder.append("\n");
            builder.append("	public void doAfterCompose(Component comp) {").append("\n");
            builder.append("		super.doAfterCompose(comp);").append("\n");
            builder.append("		lblHello.setValue(this.class.name)").append("\n");
            builder.append("	}").append("\n");
            builder.append("}").append("\n");
            src = builder.toString();
        }

        txtSource.setValue(src);
        onCompile();
    }

    @Listen("onClick=#btnCompile")
    public synchronized void onCompile() {
        GroovyClassLoader loader = GroovyInitializer.GROOVY_LOADER;
        loader.parseClass(txtSource.getValue());
        Executions.getCurrent().getDesktop().getWebApp().setAttribute("MyGroovyComposer", txtSource.getValue());
    }
}
