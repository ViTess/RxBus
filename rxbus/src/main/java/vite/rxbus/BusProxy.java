package vite.rxbus;

import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by trs on 17-1-4.
 */
public class BusProxy<T> {
    protected final HashSet<T> Entitys = new HashSet<>();
    protected final HashMap<String, Set<SubjectFucker>> SubjectMap = new HashMap<>();

    protected void createMethod(String tag, Scheduler scheduler, Func1 filter, final ProxyAction<T> proxyAction) {
        SubjectFucker fucker = new SubjectFucker();
        fucker.subject = PublishSubject.create();
        fucker.subscription = fucker.subject
                .filter(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        return o != null;
                    }
                })
                .filter(filter)
                .subscribeOn(scheduler)
                .subscribe(new Action1() {
                    @Override
                    public void call(Object o) {
                        for (T t : Entitys) {
                            proxyAction.toDo(t, o);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        //maybe cast error
                        Log.e("RxBus", throwable.getMessage());
                    }
                });
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
            if (!f.subscription.isUnsubscribed())
                f.subject.onNext(value);
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
                    fucker.subscription.unsubscribe();
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

    protected interface ProxyAction<T> {
        void toDo(T t, Object o);
    }

    protected static final class SubjectFucker {
        Subject subject;
        Subscription subscription;
    }
}
