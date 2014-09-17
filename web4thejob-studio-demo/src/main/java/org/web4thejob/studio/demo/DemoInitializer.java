package org.web4thejob.studio.demo;

import org.zkoss.zk.ui.WebApp;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

import static org.zkoss.lang.Generics.cast;

public class DemoInitializer implements org.zkoss.zk.ui.util.WebAppInit {
    @Override
    public void init(WebApp webApp) throws Exception {
        String name = "MyJoblet";
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(name, null);

        //I don't have a Desktop yet, so I cant't use org.web4thejob.studio.support.JpaUtil.setEntityManagerFactory()
        Map<String, EntityManagerFactory> emfs = cast(webApp.getAttribute("w4tjstudio-emfs"));
        if (emfs == null) {
            emfs = new HashMap<>();
            webApp.setAttribute("w4tjstudio-emfs", emfs);
        }
        emfs.put(name, emf);
    }
}
