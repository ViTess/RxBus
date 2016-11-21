package vite.rxbus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import vite.rxbus.annotation.RxThread;
import vite.rxbus.annotation.Subscribe;

/**
 * 根据注解获取对应的方法
 * Created by trs on 16-11-14.
 */
class MethodHelper {

    public static ArrayList<ParamKey> getMethodKeys(Object classEntity) {
        ArrayList<ParamKey> keyArray = new ArrayList<>();
        final Method[] methods = classEntity.getClass().getDeclaredMethods();
        for (Method method : methods) {
            //是否是被注解修饰的方法
            if (method.isAnnotationPresent(Subscribe.class)) {
                Class paramType = getMethodParamClass(method);
                Subscribe subsAnno = method.getAnnotation(Subscribe.class);
                String[] tags = subsAnno.tag();

                for (String tag : tags) {
                    keyArray.add(new ParamKey(tag, paramType));
                }
            }
        }
        return keyArray;
    }

    public static Class getMethodParamClass(Method m) {
        //获取方法的传入参数类型/
        Class[] paramTypes = m.getParameterTypes();
        if (paramTypes.length > 1) //方法的传入参数只允许1个，否则异常
            throw new IllegalArgumentException("parameter qty error");

        if (m.getModifiers() != Modifier.PUBLIC) //方法定义域必须为public
            throw new IllegalArgumentException("Modifier must be a 'public'");

        return paramTypes.length == 1 ? paramTypes[0] : Void.TYPE;
    }
}
