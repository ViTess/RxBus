package vite.rxbus.contract;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import vite.rxbus.RxThread;

/**
 * Created by trs on 16-11-14.
 */

public class ObvBuilder {
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

    public void create() {
        mSubject = new SerializedSubject(PublishSubject.create());
        mSubscription = mSubject.observeOn(RxThread.getScheduler(mRxThread))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (isParamEmpty)
                            invoke();
                        else
                            invoke(o);
                    }
                });
    }

    public void destory() {
        if (mSubject != null && mSubscription.isUnsubscribed())
            mSubject.onCompleted();

        if (mSubscription != null && mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();

        mSubject = null;
        mSubscription = null;
        mClassEntity = null;
        mRxThread = null;
        mMethod = null;
    }

    public void post(Object value) {
        mSubject.onNext(value);
    }

    private void invoke(Object value) {
        try {
            mMethod.invoke(mClassEntity, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void invoke() {
        try {
            mMethod.invoke(mClassEntity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
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
                '}';
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
