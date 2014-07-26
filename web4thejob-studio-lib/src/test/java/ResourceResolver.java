import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Created by Veniamin on 26/7/2014.
 */
public class ResourceResolver {

    @Test
    public void getResources() throws IOException {

        Enumeration<URL> list = getClass().getClassLoader().getResources("META-INF/persistence.xml");
        while (list.hasMoreElements()) {
            URL url = list.nextElement();

            System.out.println(url.toString());
        }

    }

}
