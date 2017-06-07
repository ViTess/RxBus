package vite.rxbus;

import android.text.TextUtils;
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
    private static final Map<String, Set<BusFlowableProcessor>> PROCESSORS = new ConcurrentHashMap<>();
    /**
     * Integer -> hashCode
     */
    private static final Map<Integer, StickyHolder<String, Object>> STICKY_CACHE = new ConcurrentHashMap<>();

    protected static final DefaultObject DEFAULT_OBJECT = new DefaultObject();

    public static void register(Object entity) {
        BusProxy proxy = createProxy(entity);
        proxy.register(entity);
        proxy.mount(PROCESSORS);

        //TODO:maybe use thread to do this?
        if (STICKY_CACHE.size() > 0)
            for (StickyHolder<String, Object> sticky : STICKY_CACHE.values())
                proxy.post(sticky.tag, sticky.value);
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
    public static void post(Object value) {
        post(null, value);
    }

    /**
     * @param tag
     * @param value
     */
    public static void post(String tag, Object value) {
        if (TextUtils.isEmpty(tag))
            tag = Subscribe.DEFAULT;
        Set<BusFlowableProcessor> subjects = PROCESSORS.get(tag);
        if (subjects != null)
            for (BusFlowableProcessor p : subjects)
                if (!p.isDispose())
                    p.onNext(value == null ? DEFAULT_OBJECT : value);
                else
                    subjects.remove(p);
    }

    /**
     * @param value
     * @return sticky's key
     */
    public static int postSticky(Object value) {
        return postSticky(null, value);
    }

    /**
     * @param tag
     * @param value
     * @return tag and parameter of the key
     */
    public static int postSticky(String tag, Object value) {
        post(tag, value);
        final StickyHolder<String, Object> sticky = new StickyHolder<>(TextUtils.isEmpty(tag) ? Subscribe.DEFAULT : tag,
                value == null ? DEFAULT_OBJECT : value);
        final int hashCode = sticky.hashCode();
        STICKY_CACHE.put(hashCode, sticky);
        return hashCode;
    }

    /**
     * @param key tag and parameter of the key，you can get it form postSticky
     */
    public static void removeSticky(int key) {
        if (STICKY_CACHE.size() > 0)
            STICKY_CACHE.remove(key);
    }

    public static void removeAllSticky() {
        STICKY_CACHE.clear();
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

    private RxBus() {
    }

    public static String toStrings() {
        return "RxBus[Proxy = " + PROXY_CACHE + " , Processor = " + PROCESSORS + " , Sticky = " + STICKY_CACHE + "]";
    }
}
