package vite.demo;

import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.Subject;
import vite.rxbus.BusBinder;

/**
 * Created by trs on 16-11-28.
 */

public class MainActivity$$Binder<T extends MainActivity> implements BusBinder {

    private T target;
    private Subject mSubject;
    private Subscription mSubscription;
    private Scheduler mScheduler = AndroidSchedulers.mainThread();

    private boolean isRelease = false;

    public MainActivity$$Binder(T target) {
        this.target = target;
    }

    @Override
    public boolean post(Object value) {
        if (isRelease) {
            if (mSubscription != null && !mSubscription.isUnsubscribed()) {
                mSubscription.unsubscribe();
                mSubscription = null;
            }
            return false;
        } else {
            if (mSubscription == null || mSubscription.isUnsubscribed())
                return false;
            return true;
        }
    }

    @Override
    public void release() {

    }
}
