package vite.rxbus;

import rx.Observable;

/**
 * Created by trs on 16-10-20.
 */
public class RxBus {

    private static final Bus bus = new RxBusImpl();

    public static void register(Object target) {
        bus.register(target);
    }

    public static void unregister(Object target) {
        bus.unregister(target);
    }

    public static void post(String tag) {
        bus.post(tag);
    }

    public static void post(String tag, Object value) {
        bus.post(tag, value);
    }

    public interface Bus {
        void register(Object target);

        void unregister(Object target);

        void post(String tag);

        void post(String tag, Object value);
    }
}
