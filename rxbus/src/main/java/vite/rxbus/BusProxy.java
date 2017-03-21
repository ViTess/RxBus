package vite.rxbus;

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
    protected final HashMap<String, Set<SubjectFucker>> SubjectMap = new HashMap<>();

    protected <V> void createMethod(String tag, Scheduler scheduler, final ProxyAction<T, V> proxyAction) {
        SubjectFucker fucker = new SubjectFucker();
        fucker.processor = BusProcessor.create();
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

        fucker.disposable = (Disposable) fucker.processor
                .filter(new Predicate<V>() {
                    @Override
                    public boolean test(V v) throws Exception {
                        return v != null;
                    }
                })
                .subscribeOn(scheduler)
                .subscribeWith(busSubscriber);
        Set<SubjectFucker> fuckers = SubjectMap.get(tag);
        if (fuckers == null) {
            fuckers = new CopyOnWriteArraySet<>();
            SubjectMap.put(tag, fuckers);
        }
        fuckers.add(fucker);
    }

    protected void post(String tag, Object value) {
        Set<SubjectFucker> fuckers = SubjectMap.get(tag);
        for (SubjectFucker f : fuckers) {
            if (!f.disposable.isDisposed())
                f.processor.onNext(value);
        }
    }

    protected void register(T entity) {
        Entitys.add(entity);
    }

    protected void unregister(T entity) {
        Entitys.remove(entity);
        if (Entitys.size() == 0) {
            Iterator iter = SubjectMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Set<SubjectFucker>> entry = (Map.Entry<String, Set<SubjectFucker>>) iter.next();
                Set<SubjectFucker> fuckers = entry.getValue();
                for (SubjectFucker fucker : fuckers)
                    fucker.disposable.dispose();
            }
        }
    }

    protected void mount(Map<String, Set<BusProxy.SubjectFucker>> map) {
        Iterator iter = SubjectMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Set<SubjectFucker>> entry = (Map.Entry<String, Set<SubjectFucker>>) iter.next();
            String tag = entry.getKey();
            Set<SubjectFucker> fuckers = entry.getValue();

            Set<SubjectFucker> allFuckers = map.get(tag);
            if (allFuckers == null) {
                allFuckers = new CopyOnWriteArraySet<>();
                map.put(tag, allFuckers);
            }
            allFuckers.addAll(fuckers);
        }
    }

    protected interface ProxyAction<T, V> {
        void toDo(T t, V v);
    }

    protected static final class SubjectFucker {
        BusProcessor processor;
        Disposable disposable;
    }
}
