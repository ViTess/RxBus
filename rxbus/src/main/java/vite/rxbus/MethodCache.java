package vite.rxbus;

import android.util.Log;
import android.util.LruCache;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by trs on 16-11-15.
 */

class MethodCache {
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
     * 缓存32条记录，sizeOf为map.size()，每put一次size+=size，满32清除低频率使用的缓存
     * <p>
     * 缓存可适当根据自己需求调整
     */
    private static final int CacheSize = 32;

    private LruCache<String, Set<MethodValue>> mCache;

    private MethodCache() {
        mCache = new LruCache<String, Set<MethodValue>>(CacheSize) {
            @Override
            protected int sizeOf(String key, Set<MethodValue> value) {
                Log.i("MethodCache", "sizeof:" + value.size());
                return value.size();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key,
                                        Set<MethodValue> oldValue,
                                        Set<MethodValue> newValue) {
                if (evicted && oldValue != null) {
                    Iterator iter = oldValue.iterator();
                    while (iter.hasNext()) {
                        MethodValue methodValue = (MethodValue) iter.next();
                        methodValue.release();
                        Log.i("MethodCache", "entryRemoved:" + methodValue.toString());
                    }
                    oldValue.clear();
                    oldValue = null;
                }
            }
        };
    }

    public void addCache(String className, Set<MethodValue> value) {
        if (mCache.get(className) == null)
            mCache.put(className, value);
    }

    public Set<MethodValue> getCache(String className) {
        return mCache.get(className);
    }

    public void clearCache(String className) {
        mCache.remove(className);
    }
}
