package vite.rxbus.contract;

import java.lang.reflect.Method;

import vite.rxbus.RxThread;

/**
 * Created by trs on 16-11-14.
 */

public class MethodValue {
    private Method method;
    private RxThread rxThread;

    private int hashCode;

    public MethodValue(Method method, RxThread rxThread) {
        this.method = method;
        this.rxThread = rxThread;

        hashCode = method.hashCode() + rxThread.hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public Method getMethod() {
        return method;
    }

    public RxThread getRxThread() {
        return rxThread;
    }
}
