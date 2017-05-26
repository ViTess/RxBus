package vite.rxbus;

import android.util.Log;
import android.util.LruCache;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by trs on 17-3-27.
 */

final class CacheUtil {
    /**
     * key -> class's name
     *
     * @return
     */
    public static final LruCache<String, Constructor<? extends BusProxy>> Create() {
        LruCache cache = new LruCache<String, Constructor<? extends BusProxy>>(16) {
            @Override
            protected Constructor<? extends BusProxy> create(String key) {
                return super.create(key);
            }

            @Override
            protected int sizeOf(String key, Constructor<? extends BusProxy> value) {
                return super.sizeOf(key, value);
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Constructor<? extends BusProxy> oldValue, Constructor<? extends BusProxy>
                    newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
        return cache;
    }

}
