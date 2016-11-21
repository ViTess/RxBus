package vite.rxbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import rx.Scheduler;
import vite.rxbus.annotation.RxThread;

/**
 * 与class一一对应
 * Created by trs on 16-11-18.
 */
class MethodValue implements Cloneable {
    private Method mMethod;
    private RxThread mRxThread;
    private boolean isParamEmpty;

    private HashSet<ParamKey> keySets;

    private int hashCode;

    public MethodValue(Method mMethod, RxThread mRxThread, boolean isParamEmpty) {
        this.mMethod = mMethod;
        this.mRxThread = mRxThread;
        this.isParamEmpty = isParamEmpty;

        hashCode = mMethod.hashCode() + mRxThread.hashCode();
    }

    public Method getMethod() {
        return mMethod;
    }

    public Scheduler getRxThread() {
        return RxThread.getScheduler(mRxThread);
    }

    public boolean isParamEmpty() {
        return isParamEmpty;
    }

    public void setParamKeys(HashSet<ParamKey> sets) {
        this.keySets = sets;
    }

    public HashSet<ParamKey> getParamKeys() {
        return keySets;
    }

    public void invoke(Object classEntity, Object value) {
        try {
            if (isParamEmpty)
                mMethod.invoke(classEntity);
            else
                mMethod.invoke(classEntity, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public MethodValue clone() {
        try {
            return (MethodValue) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void release() {
        mMethod = null;
        mRxThread = null;
        hashCode = 0;
        if (keySets != null) {
            keySets.clear();
            keySets = null;
        }
    }

    @Override
    public String toString() {
        return "MethodValue{" +
                "mMethod=" + mMethod +
                ", mRxThread=" + mRxThread +
                ", isParamEmpty=" + isParamEmpty +
                ", hashCode=" + hashCode +
                '}';
    }
}
