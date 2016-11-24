package vite.rxbus;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

/**
 * Created by trs on 16-11-24.
 */
public class Method2Test {

    @Test
    public void testMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        long startTime1 = System.nanoTime();
        TestEntity testEntity = new TestEntity();
        int[] array = new int[1000000];
        for (int i = 0, len = array.length; i < len; i++) {
//            array[i] = testEntity.getNum();
            testEntity.getNum();
        }
        long result1 = System.nanoTime() - startTime1;


        long startTime2 = System.nanoTime();
        TestEntity testEntity2 = new TestEntity();
        Class clazz = TestEntity.class;
        Method method = null;
        method = clazz.getMethod("getNum");
        method.setAccessible(true);
        int[] array2 = new int[1000000];
        for (int i = 0, len = array2.length; i < len; i++) {
//            array2[i] = (int) method.invoke(testEntity2);
            method.invoke(testEntity2);
        }
        long result2 = System.nanoTime() - startTime2;

        System.out.println("result1=" + result1 + " , result2=" + result2);
    }

    public class TestEntity {
        private int mNum;

        public TestEntity() {
            mNum = 1024;
        }

        public int getNum() {
            return mNum;
        }
    }
}
