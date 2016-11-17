package vite.rxbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import vite.rxbus.annotation.RxThread;

/**
 * Created by trs on 16-11-14.
 */
class ObvBuilder {
    private Subject mSubject;
    private Subscription mSubscription;
    //    private Object mClassEntity;//类实例
    private Set<Object> classEntitySets;

    private Method mMethod;
    private RxThread mRxThread;
    private boolean isParamEmpty;//方法参数是否为空

    private int hashCode;

    public ObvBuilder(Method method, RxThread rxThread, boolean isParamEmpty) {
        this.mMethod = method;
        this.mRxThread = rxThread;
        this.isParamEmpty = isParamEmpty;

        classEntitySets = new HashSet<>();

        hashCode = mMethod.hashCode() + mRxThread.hashCode();
    }

    public void addEntity(Object classEntity) {
        classEntitySets.add(classEntity);
    }

    public boolean removeEntity(Object classEntity) {
        return classEntitySets.remove(classEntity);
    }

    public void create() {
        if (mSubject == null || (mSubscription != null && !mSubscription.isUnsubscribed())) {
            if (mSubscription != null && mSubscription.isUnsubscribed())
                mSubscription.unsubscribe();
            mSubject = new SerializedSubject(PublishSubject.create());
            mSubscription = mSubject.observeOn(RxThread.getScheduler(mRxThread))
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {
                            Iterator iter = classEntitySets.iterator();
                            while (iter.hasNext()) {
                                Object classEntity = iter.next();
                                if (isParamEmpty)
                                    invoke(classEntity);
                                else
                                    invoke(classEntity, o);
                            }
                        }
                    });
        }
    }

    public void destory() {
        if (mSubject != null && mSubscription.isUnsubscribed())
            mSubject.onCompleted();

        if (mSubscription != null && mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();

        classEntitySets.clear();
        classEntitySets = null;
        mSubject = null;
        mSubscription = null;
        mRxThread = null;
        mMethod = null;
    }

    public void post(Object value) {
        mSubject.onNext(value);
    }

    private void invoke(Object classEntity, Object value) {
        try {
            mMethod.invoke(classEntity, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void invoke(Object classEntity) {
        try {
            mMethod.invoke(classEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ObvBuilder{" +
                "mSubject=" + mSubject +
                ", mSubscription=" + mSubscription +
                ", mMethod=" + mMethod +
                ", mRxThread=" + mRxThread +
                '}';
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
