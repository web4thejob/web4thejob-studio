package org.web4thejob.studio.demo.test;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.web4thejob.studio.support.FileUtils;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by Veniamin on 21/10/2014.
 */
public class GroovyTest {

    @Test
    public void doSpring() throws IOException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");

        GroovyObject goo = (GroovyObject) ctx.getBean("country3");
        assertTrue(goo.toString().equals("Hello from New Zeland"));

        StringBuilder sb = new StringBuilder();
        sb.append("package org.web4thejob.studio.demo.test");
        sb.append("\n");
        sb.append("public class NewZealand {");
        sb.append("\n");
        sb.append("String toString() { return \"Hello from New Zeland!!!\" }");
        sb.append("\n");
        sb.append("}");
        FileUtils.writeStringToFile(new ClassPathResource("org/web4thejob/studio/demo/test/NewZeland.groovy").getFile(), sb.toString(), "UTF-8");

        goo = (GroovyObject) ctx.getBean("country3");
        assertTrue(goo.toString().equals("Hello from New Zeland!!!"));

    }

    @Test
    public void doNew() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        GroovyClassLoader loader = new GroovyClassLoader();
        Class<?> gooClass1, gooClass2;
        Object goo;
        StringBuilder script;

        script = new StringBuilder();
        script.append("package foo;");
        script.append("class Bar {");
        script.append("String toString() {return \"Hello\"};");
        script.append("}");
        gooClass1 = loader.parseClass(script.toString());
        //goo = gooClass1.newInstance();
        //assertTrue("Hello".equals(goo.toString()));
        //assertTrue("Hello".equals( Class.forName("foo.Bar",false,loader).newInstance().toString()));

        script = new StringBuilder();
        script.append("package foo;");
        script.append("class Bar {");
        script.append("String toString() {return \"Hello!!!\"};");
        script.append("}");
        gooClass2 = loader.parseClass(script.toString());
        goo = gooClass2.newInstance();
        assertTrue("Hello!!!".equals(goo.toString()));
        assertTrue("Hello!!!".equals(Class.forName("foo.Bar", false, loader).newInstance().toString()));

        goo = gooClass1.newInstance();
        assertTrue("Hello".equals(goo.toString()));
        goo = gooClass2.newInstance();
        assertTrue("Hello!!!".equals(goo.toString()));

        assertTrue(gooClass2.equals(Class.forName("foo.Bar", true, loader)));

    }
}
