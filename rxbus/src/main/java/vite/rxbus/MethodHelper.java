package vite.rxbus;

import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import vite.rxbus.annotation.RxThread;
import vite.rxbus.annotation.Subscribe;

/**
 * 根据注解获取对应的方法
 * Created by trs on 16-11-14.
 */
class MethodHelper {

    public static ArrayList<MethodKey> getMethodKeys(Object classEntity) {
        ArrayList<MethodKey> keyArray = new ArrayList<>();
        final Method[] methods = classEntity.getClass().getDeclaredMethods();
        for (Method method : methods) {
            //是否是被注解修饰的方法
            if (method.isAnnotationPresent(Subscribe.class)) {
                Class paramType = getMethodParamClass(method);
                Subscribe subsAnno = method.getAnnotation(Subscribe.class);
                String[] tags = subsAnno.tag();

                for (String tag : tags) {
                    keyArray.add(new MethodKey(tag, paramType));
                }
            }
        }
        return keyArray;
    }

    public static void getMethodList(Object classEntity, Map<MethodKey, Set<ObvBuilder>> map) {
        //获取类里所有的方法
        final Class clazz = classEntity.getClass();
        final Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            //是否是被注解修饰的方法
            if (method.isAnnotationPresent(Subscribe.class)) {
                Class paramType = getMethodParamClass(method);
                boolean isParamEmpty = (paramType == Void.TYPE);
                //获取方法上的注解中的tag和thread
                Subscribe subsAnno = method.getAnnotation(Subscribe.class);
                RxThread thread = subsAnno.thread();
                String[] tags = subsAnno.tag();

                for (String tag : tags) {
                    MethodKey key = new MethodKey(tag, paramType);
                    Set<ObvBuilder> methodSets = map.get(key);
                    if (methodSets == null) {
                        methodSets = new HashSet<>();
                        map.put(key, methodSets);
                    }
                    ObvBuilder value = new ObvBuilder(classEntity, method, thread, isParamEmpty);
                    methodSets.add(value);
                }
            }
        }
    }

    private static Class getMethodParamClass(Method m) {
        //获取方法的传入参数类型/
        Class[] paramTypes = m.getParameterTypes();
        if (paramTypes.length > 1) //方法的传入参数只允许1个，否则异常
            throw new IllegalArgumentException("parameter qty error");

        if (m.getModifiers() != Modifier.PUBLIC) //方法定义域必须为public
            throw new IllegalArgumentException("Modifier must be a 'public'");

        return paramTypes.length == 1 ? paramTypes[0] : Void.TYPE;
    }
}
