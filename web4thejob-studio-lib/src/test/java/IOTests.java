import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.zkoss.io.Files;
import org.zkoss.util.resource.Locators;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOTests {

    @Test
    public void testFonts() throws IOException {

//        String font = "org/web4thejob/studio/support/js/font-awesome/fonts/fontawesome-webfont.woff";
        String font = "web/font/fontawesome-webfont.woff";

        InputStream is = Locators.getDefault().getResourceAsStream(font);

        byte[] raw = Files.readAll(is);
        System.out.println(raw.length);

        File f = new File("c:\\Documents and Settings\\e36132\\IdeaProjects\\web4thejob-studio\\web4thejob-studio-lib\\src\\main\\resources\\org\\web4thejob\\studio\\support\\js\\font-awesome\\fonts\\fontawesome-webfont.woff");
        System.out.println(f.length());

        FileInputStream fin = new FileInputStream(f);
        byte[] raw2 = IOUtils.toByteArray(fin);
        System.out.println(raw2.length);


    }
}
