package org.web4thejob.studio.demo;

import groovy.lang.GroovyClassLoader;
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

    @Listen("onClick=#btnCompile")
    public void onCompile() {
        GroovyClassLoader loader = GroovyInitializer.GROOVY_LOADER;
        loader.parseClass(txtSource.getValue());
    }
}
