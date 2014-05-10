package org.web4thejob.studio.base;

import org.web4thejob.studio.Controller;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;

/**
 * Created by Veniamin on 10/5/2014.
 */
public class AbstractController extends SelectorComposer<Component> implements Controller {

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        register();
        init();
    }

    protected void init() {
        //override
    }

    private void register() {
//        Map<String,Controller> controllers= cast( Executions.getCurrent().getDesktop().getAttribute
// (ATTR_STUDIO_CONTROLLERS));
//        if (controllers==null){
//            controllers=new TreeMap<String, Controller>()
//        }
    }
}
