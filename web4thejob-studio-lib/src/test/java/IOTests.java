import org.apache.commons.codec.Charsets;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IOTests {
    private String JS_PATH_PREFFIX = "org/web4thejob/studio/support/js/";

    @Test
    public void testFonts() throws IOException {
        java.nio.file.Files.readAllLines(Paths.get(""), Charsets.UTF_8);


    }

    public List<String> readAllLines(InputStream is) {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        List<String> list = new ArrayList<>();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(new Error());
        }
        return list;
    }

}
