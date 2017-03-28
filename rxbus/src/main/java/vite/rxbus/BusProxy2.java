package vite.rxbus;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by trs on 17-3-21.
 */
public abstract class BusProxy2<T> {

    /**
     * tag ->
     */
    protected final Map<String , BusProcessor> map = new HashMap<>();

    protected static <T, V> void createSingle(String tag, V v, final Set<T> entitys, Scheduler scheduler, final IAction<T, V>
            proxyAction) {
        Single.just(v)
                .subscribeOn(scheduler)
                .subscribe(new SingleObserver<V>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(V value) {
                        for (T t : entitys)
                            proxyAction.toDo(t, value);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    protected static <T, V> void createProcessor(String tag, V v, Set<T> entitys, Scheduler scheduler, final IAction<T, V> proxyAction) {
    }

    protected final Set<T> Entitys = new WeakHashSet<>();//avoid memory leak

    void register(T entity) {
        Entitys.add(entity);
    }

    void unregister(T entity) {
        Entitys.remove(entity);
    }

    protected abstract void onPost(String tag, Object v);

    protected abstract void onContinuePost(String Tag, Object v);
}
