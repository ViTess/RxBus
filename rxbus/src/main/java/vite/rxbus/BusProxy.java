package vite.rxbus;

import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.operators.flowable.FlowableInternalHelper;
import io.reactivex.processors.PublishProcessor;

/**
 * Created by trs on 17-1-4.
 */
public class BusProxy<T> {
    protected final Set<T> Entitys = new WeakHashSet<>();//avoid memory leak
    protected final HashMap<String, Set<BusProcessor<T>>> SubjectMap = new HashMap<>();

    protected <V> void createMethod(String tag, Scheduler scheduler, final IAction<T, V> proxyAction) {
        BusProcessor p = BusProcessor.create();
        BusSubscriber<V> busSubscriber = new BusSubscriber<>(new Consumer<V>() {
            @Override
            public void accept(V v) throws Exception {
                //TODO:thread-safe Entitys
                for (T t : Entitys) {
                    proxyAction.toDo(t, v);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.e("RxBus", throwable.getMessage());
            }
        }, Functions.EMPTY_ACTION, FlowableInternalHelper.RequestMax.INSTANCE);

        p.filter(new Predicate<V>() {
            @Override
            public boolean test(V v) throws Exception {
                return v != null;
            }
        })
                .subscribeOn(scheduler)
                .subscribeWith(busSubscriber);
        Set<BusProcessor<T>> bPros = SubjectMap.get(tag);
        if (bPros == null) {
            bPros = new HashSet<>();
            SubjectMap.put(tag, bPros);
        }
        bPros.add(p);
    }

    protected void post(String tag, Object value) {
        Set<BusProcessor<T>> bPros = SubjectMap.get(tag);
        for (BusProcessor p : bPros)
            p.onNext(value);
    }

    protected void register(T entity) {
        Entitys.add(entity);
    }

    protected void unregister(T entity) {
        Entitys.remove(entity);
        if (Entitys.size() == 0) {
            for (Iterator iter = SubjectMap.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry<String, Set<BusProcessor>> entry = (Map.Entry<String, Set<BusProcessor>>) iter.next();
                Set<BusProcessor> bPros = entry.getValue();
                for (BusProcessor p : bPros)
                    p.dispose();
            }
        }
    }

    protected void mount(Map<String, Set<BusProcessor>> map) {
        for (Iterator iter = SubjectMap.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String, Set<BusProcessor>> entry = (Map.Entry<String, Set<BusProcessor>>) iter.next();
            String tag = entry.getKey();
            Set<BusProcessor> bPros = entry.getValue();

            Set<BusProcessor> allBPros = map.get(tag);
            if (allBPros == null) {
                allBPros = new CopyOnWriteArraySet<>();
                map.put(tag, allBPros);
            }
            allBPros.addAll(bPros);
        }
    }
}
