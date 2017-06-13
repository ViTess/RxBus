package vite.rxbus;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import io.reactivex.Scheduler;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.operators.flowable.FlowableInternalHelper;

/**
 * Created by trs on 17-1-4.
 */
public class BusProxy<T> {

    protected interface IAction<T, V> {
        void toDo(T t, V v);
    }

    //    protected final Set<T> Entitys = new WeakHashSet<>();//avoid memory leak
    protected final HashMap<String, Set<BusFlowableProcessor<T>>> ProcessorMap = new HashMap<>();

    private WeakReference<T> entity;

    protected <V> void createMethod(String tag, Scheduler scheduler, final Class<V> clazz, final IAction<T, V> proxyAction) {
        BusFlowableProcessor p = BusProcessor.create().toSerialized();
        BusSubscriber<V> busSubscriber = new BusSubscriber<>(new Consumer<V>() {
            @Override
            public void accept(V v) throws Exception {
                //TODO:thread-safe Entitys
                if (entity != null && entity.get() != null)
                    proxyAction.toDo(entity.get(), v);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.e("RxBus", throwable.getMessage());
            }
        }, Functions.EMPTY_ACTION, FlowableInternalHelper.RequestMax.INSTANCE);

        //no need filter,because RxJava2 unsupport null
        p.filter(new Predicate() {
            @Override
            public boolean test(Object o) throws Exception {
                return clazz.isInstance(o);
            }
        }).observeOn(scheduler).subscribeWith(busSubscriber);

        Set<BusFlowableProcessor<T>> bPros = ProcessorMap.get(tag);
        if (bPros == null) {
            bPros = new HashSet<>();
            ProcessorMap.put(tag, bPros);
        }
        bPros.add(p);
    }

    protected void post(String tag, Object value) {
        Set<BusFlowableProcessor<T>> bPros = ProcessorMap.get(tag);
        if (bPros != null)
            for (BusFlowableProcessor p : bPros)
                p.onNext(value);
    }

    protected void register(T entity) {
//        Entitys.add(entity);
        this.entity = new WeakReference<T>(entity);
    }

    protected void unregister(Map<String, Set<BusFlowableProcessor>> map) {
        for (Iterator iter = ProcessorMap.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String, Set<BusFlowableProcessor>> entry = (Map.Entry<String, Set<BusFlowableProcessor>>) iter.next();
            String tag = entry.getKey();
            Set<BusFlowableProcessor> bPros = entry.getValue();

            Set<BusFlowableProcessor> allBPros = map.get(tag);
            allBPros.removeAll(bPros);
            if (allBPros.size() == 0)
                map.remove(tag);

            for (BusFlowableProcessor p : bPros)
                p.dispose();
        }
        ProcessorMap.clear();
        this.entity.clear();
        this.entity = null;
    }

    protected void mount(Map<String, Set<BusFlowableProcessor>> map) {
        for (Iterator iter = ProcessorMap.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String, Set<BusFlowableProcessor>> entry = (Map.Entry<String, Set<BusFlowableProcessor>>) iter.next();
            String tag = entry.getKey();
            Set<BusFlowableProcessor> bPros = entry.getValue();

            Set<BusFlowableProcessor> allBPros = map.get(tag);
            if (allBPros == null) {
                allBPros = new CopyOnWriteArraySet<>();
                map.put(tag, allBPros);
            }
            allBPros.addAll(bPros);
        }
    }
}
