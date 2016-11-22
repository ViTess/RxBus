package vite.rxbus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import vite.rxbus.annotation.RxComputation;
import vite.rxbus.annotation.RxIO;
import vite.rxbus.annotation.RxImmediate;
import vite.rxbus.annotation.RxMainThread;
import vite.rxbus.annotation.RxNewThread;
import vite.rxbus.annotation.RxTrampoline;
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
                String[] tags = subsAnno.value();

                for (String tag : tags) {
                    keyArray.add(new ParamKey(tag, paramType));
                }
            }
        }
        return keyArray;
    }

    public static Scheduler getScheduler(Method method) {
        if (method.isAnnotationPresent(RxMainThread.class))
            return AndroidSchedulers.mainThread();
        if (method.isAnnotationPresent(RxIO.class))
            return Schedulers.io();
        if (method.isAnnotationPresent(RxComputation.class))
            return Schedulers.computation();
        if (method.isAnnotationPresent(RxNewThread.class))
            return Schedulers.newThread();
        if (method.isAnnotationPresent(RxTrampoline.class))
            return Schedulers.trampoline();
        if (method.isAnnotationPresent(RxImmediate.class))
            return Schedulers.immediate();
        return Schedulers.immediate();
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
