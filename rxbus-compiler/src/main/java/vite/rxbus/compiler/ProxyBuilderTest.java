package vite.rxbus.compiler;

import com.squareup.javapoet.ClassName;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Created by trs on 17-1-5.
 */
public class ProxyBuilderTest {
    @Test
    public void testCreateClass() {
        File file = new File("/home/trs/AndroidStudioProjects/RxBus/exampleTest");
        File DemoTestClass = new File(file.getAbsolutePath() + "/vite/demo/MainActivity$$Proxy.java");
        ProxyBuilder builder = new ProxyBuilder(ClassName.get("vite.demo", "MainActivity"));
        builder.build(file);

        assertTrue(file.exists());
        assertTrue(DemoTestClass.exists());
    }
}
