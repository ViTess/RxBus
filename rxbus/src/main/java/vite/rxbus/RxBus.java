package vite.rxbus;

import android.support.annotation.NonNull;
import android.util.LruCache;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by trs on 16-10-20.
 */
public final class RxBus {

    private static final LruCache<String, Constructor<? extends BusProxy>> CONSTRUCTOR_CACHE = CacheUtil.Create();
    private static final Map<Class, BusProxy> PROXY_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Set<BusProcessor>> SUBJECTS = new ConcurrentHashMap<>();

    protected static final DefaultObject DEFAULT_OBJECT = new DefaultObject();

    public static void register(Object entity) {
        BusProxy proxy = createProxy(entity);
        proxy.register(entity);
        proxy.mount(SUBJECTS);
    }

    public static void unregister(Object entity) {
        Class entityClass = entity.getClass();
        BusProxy proxy = getProxy4Class(entityClass);
        if (proxy != null)
            proxy.unregister(entity);
    }

    /**
     * post to default tag('NULL' tag)
     *
     * @param value not tag
     */
    public static void post(@NonNull Object value) {
        post(Subscribe.DEFAULT, value);
    }

//    public static void post(@NonNull String tag) {
//        post(tag, DEFAULT_OBJECT);
//    }
//
//    public static void post() {
//        post(Subscribe.DEFAULT, DEFAULT_OBJECT);
//    }

    /**
     * @param tag
     * @param value
     */
    public static void post(@NonNull String tag, Object value) {
        Set<BusProcessor> subjects = SUBJECTS.get(tag);
        if (subjects != null)
            for (BusProcessor p : subjects)
                if (!p.isDispose()) {
                    p.onNext(value == null ? DEFAULT_OBJECT : value);
                } else
                    subjects.remove(p);
    }

    private static BusProxy createProxy(Object entity) {
        Class entityClass = entity.getClass();
        BusProxy proxy = getProxy4Class(entityClass);
        if (proxy == null) {
            Constructor<? extends BusProxy> constructor = getConstructor4Class(entityClass);
            try {
                proxy = constructor.newInstance();
                PROXY_CACHE.put(entityClass, proxy);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return proxy;
    }

    private static BusProxy getProxy4Class(Class c) {
        return PROXY_CACHE.get(c);
    }

    private static Constructor<? extends BusProxy> getConstructor4Class(Class c) {
        final String canonicalName = c.getCanonicalName();
        Constructor<? extends BusProxy> constructor = CONSTRUCTOR_CACHE.get(canonicalName);
        if (constructor == null) {
            String targetName = c.getName();
            try {
                Class targetProxy = Class.forName(targetName + "$$Proxy");
                constructor = targetProxy.getConstructor();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            CONSTRUCTOR_CACHE.put(canonicalName, constructor);
        }
        return constructor;
    }
}
