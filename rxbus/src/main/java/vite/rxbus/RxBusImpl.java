package vite.rxbus;

import android.util.Log;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import rx.Observable;
import vite.rxbus.contract.MethodKey;
import vite.rxbus.contract.ObvBuilder;

/**
 * Created by trs on 16-11-14.
 */

public class RxBusImpl implements RxBus.Bus {

    private static final ConcurrentMap<MethodKey, Set<ObvBuilder>> MethodMap = new ConcurrentHashMap<>();

    /**
     * @param target 类实例
     */
    @Override
    public void register(Object target) {
        //先获取类中所有注解的方法和对应的key
        Log.v("RxBus register", "target:" + target.getClass().getName());
        MethodHelper.getMethodList(target, MethodMap);
        Iterator iter = MethodMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Log.v("RxBus register", "MethodKey:" + entry.getKey().toString());
            Set<ObvBuilder> sets = (Set<ObvBuilder>) entry.getValue();
            Iterator setIter = sets.iterator();
            while (setIter.hasNext()) {
                ObvBuilder obv = (ObvBuilder) setIter.next();
                obv.create();
                Log.v("RxBus register", "ObvBuilder:" + obv.toString());
            }
        }
        Log.v("RxBus register", "MethodMap size:" + MethodMap.size());
    }

    @Override
    public void unregister(Object target) {
        Log.v("RxBus", "unregister target:" + target.getClass().getName());
        MethodHelper.getMethodList(target, MethodMap);
        Iterator iter = MethodMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Set<ObvBuilder> sets = (Set<ObvBuilder>) entry.getValue();
            Iterator setIter = sets.iterator();
            while (setIter.hasNext()) {
                ObvBuilder obv = (ObvBuilder) setIter.next();
                obv.destory();
            }
            sets.clear();
            MethodMap.remove(entry.getKey());
        }
    }

    @Override
    public void post(String tag) {
        String t = tag;
        if (t == null)
            t = "__default__";
        MethodKey k = new MethodKey(t, Void.TYPE);
        Set<ObvBuilder> sets = MethodMap.get(k);
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
        MethodKey k = new MethodKey(t, value.getClass());
        Set<ObvBuilder> sets = MethodMap.get(k);
        if (sets != null) {
            Iterator setIter = sets.iterator();
            while (setIter.hasNext()) {
                ObvBuilder obv = (ObvBuilder) setIter.next();
                obv.post(value);
            }
        }
    }

    @Override
    public Observable toObservable(String tag, Object value) {
        return null;
    }
}
