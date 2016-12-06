package vite.rxbus;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import vite.rxbus.thread.RxComputation;
import vite.rxbus.thread.RxIO;
import vite.rxbus.thread.RxImmediate;
import vite.rxbus.thread.RxMainThread;
import vite.rxbus.thread.RxNewThread;
import vite.rxbus.thread.RxTrampoline;

/**
 * Created by trs on 16-10-20.
 */
public final class RxBus {

//    private static final Bus bus = new RxBusImpl();
//
//    public static void register(Object target) {
//        bus.register(target);
//    }
//
//    public static void unregister(Object target) {
//        bus.unregister(target);
//    }
//
//    public static void post(String tag) {
//        bus.post(tag);
//    }
//
//    public static void post(String tag, Object value) {
//        bus.post(tag, value);
//    }

    public static final Scheduler getScheduler(Class clazz) {
        if (RxMainThread.class.equals(clazz))
            return AndroidSchedulers.mainThread();
        if (RxIO.class.equals(clazz))
            return Schedulers.io();
        if (RxComputation.class.equals(clazz))
            return Schedulers.computation();
        if (RxNewThread.class.equals(clazz))
            return Schedulers.newThread();
        if (RxTrampoline.class.equals(clazz))
            return Schedulers.trampoline();
        if (RxImmediate.class.equals(clazz))
            return Schedulers.immediate();

        return AndroidSchedulers.mainThread();
    }

    public static <T> void post(String tag , T value){
    }

    public interface Bus {
        void register(Object target);

        void unregister(Object target);

        void post(String tag);

        void post(String tag, Object value);
    }
}
