package vite.rxbus;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import rx.Scheduler;
import vite.rxbus.annotation.Subscribe;

import static vite.rxbus.MethodHelper.getMethodKeys;
import static vite.rxbus.MethodHelper.getMethodParamClass;
import static vite.rxbus.MethodHelper.getScheduler;

/**
 * Created by trs on 16-11-14.
 */

class RxBusImpl implements RxBus.Bus {

    /**
     * 记录所有已注册的
     */
    private static final ConcurrentMap<ParamKey, Set<ObvBuilder>> MethodMap = new ConcurrentHashMap<>();

    /**
     * @param target 类实例
     */
    @Override
    public void register(Object target) {
        Log.v("RxBus register", "target:" + target.getClass().getName());

        //获取类里所有的方法
        final Class clazz = target.getClass();
        final String className = clazz.getName();
        Set<MethodValue> methodValueCache = MethodCache.getInstance().getCache(className);
        if (methodValueCache == null || methodValueCache.isEmpty()) {
            Log.v("RxBus register", className + " hasn't cache");
            //缓存为空时
            if (methodValueCache == null)
                methodValueCache = new HashSet<>();

            final Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                //是否是被注解修饰的方法
                if (method.isAnnotationPresent(Subscribe.class)) {
                    Class paramType = getMethodParamClass(method);

                    boolean isParamEmpty = (paramType == Void.TYPE);
                    //获取方法上的注解中的tag和thread
                    Subscribe subsAnno = method.getAnnotation(Subscribe.class);
                    Scheduler scheduler = getScheduler(method);
                    String[] tags = subsAnno.value();

                    MethodValue methodValue = new MethodValue(method, scheduler, isParamEmpty);
                    HashSet<ParamKey> paramKeys = new HashSet<>();
                    for (String tag : tags) {
                        ParamKey key = new ParamKey(tag, paramType);
                        Set<ObvBuilder> methodSets = MethodMap.get(key);
                        if (methodSets == null) {
                            methodSets = new CopyOnWriteArraySet<>();
                            MethodMap.putIfAbsent(key, methodSets);
                        }
                        ObvBuilder value = new ObvBuilder(target, methodValue);
                        value.create();
                        methodSets.add(value);

                        paramKeys.add(key);
                    }
                    methodValue.setParamKeys(paramKeys);
                    methodValueCache.add(methodValue);
                }
            }
            MethodCache.getInstance().addCache(className, methodValueCache);
        } else {
            //缓存不为空时
            Log.v("RxBus register", className + " has cache");
            Iterator iter = methodValueCache.iterator();
            while (iter.hasNext()) {
                MethodValue methodValue = (MethodValue) iter.next();
                Iterator paramIter = methodValue.getParamKeys().iterator();
                while (paramIter.hasNext()) {
                    ParamKey paramKey = (ParamKey) paramIter.next();
                    Set<ObvBuilder> methodSets = MethodMap.get(paramKey);
                    if (methodSets == null) {
                        methodSets = new CopyOnWriteArraySet<>();
                        MethodMap.putIfAbsent(paramKey, methodSets);
                    }
                    ObvBuilder value = new ObvBuilder(target, methodValue);
                    value.create();
                    methodSets.add(value);
                }
            }
        }
    }

    @Override
    public void unregister(Object target) {
        Log.v("RxBus", "unregister target:" + target.getClass().getName());
        final Class clazz = target.getClass();
        final String className = clazz.getName();
        Set<MethodValue> methodValueCache = MethodCache.getInstance().getCache(className);
        if (methodValueCache == null || methodValueCache.isEmpty()) {
            Log.v("RxBus unregister", className + " hasn't cache");
            ArrayList<ParamKey> keyArray = getMethodKeys(target);
            for (ParamKey key : keyArray) {
                Set<ObvBuilder> sets = MethodMap.get(key);
                Iterator iter = sets.iterator();
                while (iter.hasNext()) {
                    ObvBuilder value = (ObvBuilder) iter.next();
                    if (value.getClassEntity().equals(target)) {
                        value.release();
                        sets.remove(value);
                    }
                }
            }
        } else {
            Log.v("RxBus unregister", className + " has cache");
            Iterator iter = methodValueCache.iterator();
            while (iter.hasNext()) {
                MethodValue methodValue = (MethodValue) iter.next();
                Log.v("RxBus unregister", "methodValue:" + methodValue);
                Set<ParamKey> paramKeys = methodValue.getParamKeys();
                Log.v("RxBus unregister", "paramKeys:" + paramKeys);
                Iterator keySets = paramKeys.iterator();
                while (keySets.hasNext()) {
                    ParamKey key = (ParamKey) keySets.next();
                    Set<ObvBuilder> sets = MethodMap.get(key);
                    Log.v("RxBus unregister", "sets:" + sets);
                    Iterator obvSets = sets.iterator();
                    while (obvSets.hasNext()) {
                        ObvBuilder value = (ObvBuilder) obvSets.next();
                        if (value.getClassEntity().equals(target)) {
                            value.release();
                            sets.remove(value);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void post(String tag) {
        String t = tag;
        if (t == null)
            t = "__default__";
        ParamKey k = new ParamKey(t, Void.TYPE);
        Set<ObvBuilder> sets = MethodMap.get(k);
        Log.v("RxBus post", "sets:" + sets);
        if (sets != null) {
            Iterator setIter = sets.iterator();
            while (setIter.hasNext()) {
                ObvBuilder obv = (ObvBuilder) setIter.next();
                obv.post(null);
            }
        }
    }

    @Override
    public void post(String tag, Object value) {
        String t = tag;
        if (t == null)
            t = "__default__";
        Class clazz = value == null ? Void.TYPE : value.getClass();
        ParamKey k = new ParamKey(t, clazz);
        Set<ObvBuilder> sets = MethodMap.get(k);
        Log.v("RxBus post", "sets:" + sets);
        if (sets != null) {
            Iterator setIter = sets.iterator();
            while (setIter.hasNext()) {
                ObvBuilder obv = (ObvBuilder) setIter.next();
                obv.post(value);
            }
        }
    }

}
