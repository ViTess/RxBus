package vite.rxbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import vite.rxbus.RxThread;

/**
 * Created by trs on 16-10-20.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {
    String[] tags() default {"__default__"};

    RxThread thread() default RxThread.MainThread;
}
