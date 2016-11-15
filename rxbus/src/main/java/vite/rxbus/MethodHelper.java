package vite.rxbus;

import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import vite.rxbus.annotation.Subscribe;
import vite.rxbus.contract.MethodKey;
import vite.rxbus.contract.ObvBuilder;

/**
 * 根据注解获取对应的方法
 * Created by trs on 16-11-14.
 */
public class MethodHelper {
    public static void getMethodList(Object classEntity, Map<MethodKey, Set<ObvBuilder>> map) {
        //获取类里所有的方法
        final Method[] methods = classEntity.getClass().getDeclaredMethods();
        for (Method method : methods) {
            //是否是被注解修饰的方法
            if (method.isAnnotationPresent(Subscribe.class)) {
                Log.v("RxBus MethodHelper", "method:" + method.getName() + " isAnnotationPresent");
                Class paramType = getMethodParamClass(method);
                boolean isParamEmpty = (paramType == Void.TYPE);
                //获取方法上的注解中的tag和thread
                Subscribe subsAnno = method.getAnnotation(Subscribe.class);
                RxThread thread = subsAnno.thread();
                String[] tags = subsAnno.tags();

                for (String tag : tags) {
                    MethodKey key = new MethodKey(tag, paramType);
                    Set<ObvBuilder> methodSets = map.get(key);
                    if (methodSets == null) {
                        methodSets = new HashSet<>();
                        map.put(key, methodSets);
                    }
                    methodSets.add(new ObvBuilder(classEntity, method, thread, isParamEmpty));
                }
            }
        }
    }

    private static Class getMethodParamClass(Method m) {
        //获取方法的传入参数类型
        Class[] paramTypes = m.getParameterTypes();
        if (paramTypes.length > 1) //方法的传入参数只允许1个，否则异常
            throw new IllegalArgumentException("parameter qty error");

        if (m.getModifiers() != Modifier.PUBLIC) //方法定义域必须为public
            throw new IllegalArgumentException("Modifier must be a 'public'");

        return paramTypes.length == 1 ? paramTypes[0] : Void.TYPE;
    }
}
