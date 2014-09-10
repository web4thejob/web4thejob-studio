import org.apache.commons.codec.Charsets;
import org.junit.Test;
import org.web4thejob.studio.support.Base32;
import org.web4thejob.studio.support.FileUtils;

import java.io.*;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
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

    @Test
    public void MD5() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String val = "d:";
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] d = md.digest(val.getBytes("UTF-8"));

        //System.out.println(d);

        String v2 = Base32.encode(d);
        System.out.println(v2);


    }

    @Test
    public void mergePathTest() throws IOException {
        File fileProd = new File("d:\\Development\\web4thejob-studio\\web4thejob-studio-demo\\target\\web4thejob-studio-demo-1.0.3-SNAPSHOT\\a\\b\\empty.zul");
        String targetWebapp = "D:\\Development\\web4thejob-studio\\web4thejob-studio-demo\\target\\web4thejob-studio-demo-1.0.3-SNAPSHOT";
        String sourceWebapp = "d:\\Development\\web4thejob-studio\\web4thejob-studio-demo\\src\\main\\webapp";

        FileUtils.writeStringToFile(fileProd, new Date().toString(), "UTF-8");


        File fileProd2 = new File(sourceWebapp + fileProd.getAbsolutePath().substring(targetWebapp.length()));

        FileUtils.writeStringToFile(fileProd2, new Date().toString(), "UTF-8");


        //System.out.println(suffix);


    }
}
