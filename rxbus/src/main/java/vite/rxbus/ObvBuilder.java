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
    private Object mClassEntity;//类实例

    private Method mMethod;
    private RxThread mRxThread;
    private boolean isParamEmpty;//方法参数是否为空

    private int hashCode;

    public ObvBuilder(Object classEntity, Method method, RxThread rxThread, boolean isParamEmpty) {
        this.mClassEntity = classEntity;
        this.mMethod = method;
        this.mRxThread = rxThread;
        this.isParamEmpty = isParamEmpty;

        hashCode = mClassEntity.hashCode() + mMethod.hashCode() + mRxThread.hashCode();
    }

    public Object getClassEntity() {
        return mClassEntity;
    }

    public void create() {
        if (mSubject == null || (mSubscription != null && !mSubscription.isUnsubscribed())) {
            if (mSubscription != null && mSubscription.isUnsubscribed())
                mSubscription.unsubscribe();

            mSubject = new SerializedSubject(PublishSubject.create());
            mSubscription = mSubject.observeOn(RxThread.getScheduler(mRxThread))
                    .subscribe(new Action1() {
                        @Override
                        public void call(Object o) {
                            invoke(o);
                        }
                    });
        }
    }


    public void destory() {
        if (mSubject != null && mSubscription.isUnsubscribed())
            mSubject.onCompleted();

        if (mSubscription != null && mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();

        mSubject = null;
        mSubscription = null;
        mRxThread = null;
        mMethod = null;
    }

    public void post(Object value) {
        mSubject.onNext(value);
    }

    private void invoke(Object value) {
        try {
            if (isParamEmpty)
                mMethod.invoke(mClassEntity);
            else
                mMethod.invoke(mClassEntity, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ObvBuilder{" +
                "mSubject=" + mSubject +
                ", mSubscription=" + mSubscription +
                ", mClassEntity=" + mClassEntity +
                ", mMethod=" + mMethod +
                ", mRxThread=" + mRxThread +
                ", isParamEmpty=" + isParamEmpty +
                ", hashCode=" + hashCode +
                '}';
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
