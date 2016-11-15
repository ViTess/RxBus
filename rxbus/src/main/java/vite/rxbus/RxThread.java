package vite.rxbus;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by trs on 16-10-20.
 */

public enum RxThread {
    MainThread,
    IO,
    NewThread,
    Computation,
    Trampoline,
    Immediate;

    public static Scheduler getScheduler(RxThread thread) {
        Scheduler scheduler;
        switch (thread) {
            case MainThread:
                scheduler = AndroidSchedulers.mainThread();
                break;
            case NewThread:
                scheduler = Schedulers.newThread();
                break;
            case IO:
                scheduler = Schedulers.io();
                break;
            case Computation:
                scheduler = Schedulers.computation();
                break;
            case Trampoline:
                scheduler = Schedulers.trampoline();
                break;
            case Immediate:
                scheduler = Schedulers.immediate();
                break;
            default:
                scheduler = AndroidSchedulers.mainThread();
                break;
        }
        return scheduler;
    }
}
