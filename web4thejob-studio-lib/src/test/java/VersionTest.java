import junit.framework.Assert;
import org.junit.Test;
import org.web4thejob.studio.Version;

/**
 * Created by Veniamin Isaias on 5/8/2014.
 */
public class VersionTest {

    @Test
    public void getVersion() {
        Assert.assertNotSame("", Version.getVersion());
        System.out.println(Version.getVersion());
    }

}
