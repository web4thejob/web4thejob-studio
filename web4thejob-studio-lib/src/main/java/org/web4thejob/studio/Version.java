package org.web4thejob.studio;

import org.zkoss.util.resource.Locators;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Veniamin Isaias on 5/8/2014.
 */
public class Version {
    public static String getVersion() {

        try (InputStream input = Locators.getDefault().getResourceAsStream("org/web4thejob/studio/version.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty("application.version");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }


}
