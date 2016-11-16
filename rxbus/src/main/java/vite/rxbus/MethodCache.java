package vite.rxbus;

import android.util.Log;
import android.util.LruCache;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by trs on 16-11-15.
 */

public class MethodCache {
    private static MethodCache instance;

    public static MethodCache getInstance() {
        if (instance == null)
            synchronized (MethodCache.class) {
                MethodCache temp = instance;
                if (temp == null) {
                    temp = new MethodCache();
                    instance = temp;
                }
            }
        return instance;
    }


    /**
     * 缓存128条记录，sizeOf为map.size()，每put一次size+=size，满128清除低频率使用的缓存
     */
    private static final int CacheSize = 128;

    private LruCache<Class, Map<MethodKey, ObvBuilder>> mCache;

    private MethodCache() {
        mCache = new LruCache<Class, Map<MethodKey, ObvBuilder>>(CacheSize) {
            @Override
            protected int sizeOf(Class key, Map<MethodKey, ObvBuilder> value) {
                Log.i("MethodCache", "sizeof:" + value.size());
                return value.size();
            }

            @Override
            protected void entryRemoved(boolean evicted, Class key,
                                        Map<MethodKey, ObvBuilder> oldValue,
                                        Map<MethodKey, ObvBuilder> newValue) {
                if (evicted && oldValue != null) {
                    Iterator iter = oldValue.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        ObvBuilder obvBuilder = (ObvBuilder) entry.getValue();
                        Log.i("MethodCache", "entryRemoved:" + obvBuilder.toString());
                        obvBuilder.destory();
                    }
                    oldValue.clear();
                    oldValue = null;
                }
            }
        };
    }

    public void addCache(Class classEntity, Map<MethodKey, ObvBuilder> value) {
        if (mCache.get(classEntity) == null)
            mCache.put(classEntity, value);
    }

    public Map<MethodKey, ObvBuilder> getCache(Class key) {
        return mCache.get(key);
    }

    public void clearCache(Class key) {
        mCache.remove(key);
    }
}
