package vite.rxbus;

import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by trs on 16-11-14.
 */
class ObvBuilder {
    private Subject mSubject;
    private Subscription mSubscription;
    private Object mClassEntity;//类实例

    private MethodValue mMethodValue;

    private int hashCode;

    public ObvBuilder(Object classEntity, MethodValue methodValue) {
        this.mClassEntity = classEntity;
        this.mMethodValue = methodValue.clone();

        hashCode = mClassEntity.hashCode() + mMethodValue.hashCode();
    }

    public Object getClassEntity() {
        return mClassEntity;
    }

    public void create() {
        if (mSubject == null || (mSubscription != null && !mSubscription.isUnsubscribed())) {
            if (mSubscription != null && mSubscription.isUnsubscribed())
                mSubscription.unsubscribe();

            mSubject = new SerializedSubject(PublishSubject.create());
            mSubscription = mSubject.observeOn(mMethodValue.getScheduler())
                    .subscribe(new Action1() {
                        @Override
                        public void call(Object o) {
                            mMethodValue.invoke(mClassEntity, o);
                        }
                    });
        }
    }


    public void release() {
        if (mSubject != null && mSubscription.isUnsubscribed())
            mSubject.onCompleted();

        if (mSubscription != null && mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();

        mSubject = null;
        mSubscription = null;
        mClassEntity = null;
        mMethodValue = null;
    }

    public void post(Object value) {
        mSubject.onNext(value);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
