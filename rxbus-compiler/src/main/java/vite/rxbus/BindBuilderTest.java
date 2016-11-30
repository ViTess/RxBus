package vite.rxbus;

import com.squareup.javapoet.ClassName;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Created by trs on 16-11-29.
 */
public class BindBuilderTest {

    @Test
    public void testCreateClass() {
        File file = new File("/home/trs/AndroidStudioProjects/RxBus/exampleTest");
        File DemoTestClass = new File(file.getAbsolutePath() + "/vite/demo/MainActivity$$BusBinder.java");
        BindBuilder builder = new BindBuilder(ClassName.get("vite.demo", "MainActivity"));
        builder.build(file);

        assertTrue(file.exists());
        assertTrue(DemoTestClass.exists());
    }
}
