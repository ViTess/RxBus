package vite.rxbus;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by trs on 16-11-24.
 */
public class MethodTest {

    @Test
    public void testMethod() {
        long startTime1 = System.nanoTime();
        TestEntity testEntity = new TestEntity();
        int[] array = new int[1000000];
        for (int i = 0, len = array.length; i < len; i++) {
            array[i] = testEntity.getNum();
        }
        long result1 = System.nanoTime() - startTime1;

        long startTime2 = System.nanoTime();
        TestEntity testEntity2 = new TestEntity();
        Class clazz = TestEntity.class;
        Method method = null;
        try {
            method = clazz.getMethod("getNum");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        int[] array2 = new int[1000000];
        for (int i = 0, len = array2.length; i < len; i++) {
            try {
                array2[i] = (int) method.invoke(testEntity2);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        long result2 = System.nanoTime() - startTime2;

        System.out.println("result1=" + result1 + " , result2=" + result2);
    }

    public class TestEntity {
        private int mNum;
        private Random mRandom;

        public TestEntity() {
            mRandom = new Random();
        }

        public int getNum() {
            return mRandom.nextInt();
        }
    }
}
