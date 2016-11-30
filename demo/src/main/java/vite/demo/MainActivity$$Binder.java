package vite.demo;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import vite.rxbus.BusBinder;

/**
 * Created by trs on 16-11-28.
 */

public final class MainActivity$$Binder<T extends MainActivity> implements BusBinder {

    private T target;
    private Subject mSubject;
    private Subscription mSubscription;
    private Scheduler mScheduler = AndroidSchedulers.mainThread();

    private CopyOnWriteArraySet<String> tags;
    private boolean isRelease = false;

    public MainActivity$$Binder(T target) {
        this.target = target;
        initTags();
        mSubject = new SerializedSubject(PublishSubject.create());
        mSubscription = mSubject.observeOn(mScheduler)
                .subscribe(new Action1() {
                    @Override
                    public void call(Object o) {
                        MainActivity$$Binder.this.target.test((Integer) o);
                    }
                });
    }

    private void initTags() {
        tags = new CopyOnWriteArraySet<>();
        tags.add("123");
    }

    @Override
    public Set<String> getTags() {
        return tags;
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
            if (mSubject == null || mSubscription == null || mSubscription.isUnsubscribed())
                return false;

            mSubject.onNext(value);
            return true;
        }
    }

    @Override
    public void release() {
        isRelease = true;
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
        mSubject = null;
    }
}
