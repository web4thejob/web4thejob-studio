package org.web4thejob.studio.demo;

import groovy.lang.GroovyClassLoader;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.util.Initiator;

import java.util.Map;

public class GroovyInitializer implements Initiator {
    public static final GroovyClassLoader GROOVY_LOADER = new GroovyClassLoader();

    @Override
    public void doInit(Page page, Map<String, Object> args) throws Exception {
        Thread.currentThread().setContextClassLoader(GROOVY_LOADER);
    }
}
