package vite.rxbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by trs on 16-10-20.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Subscribe {
    /**
     * Tag
     *
     * @return
     */
    String[] value() default {DEFAULT};

    /**
     * random strings
     */
    String DEFAULT = "2d51$5,0";
}
