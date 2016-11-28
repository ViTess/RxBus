package vite.rxbus;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.Filer;

/**
 * build the java class
 * Created by trs on 16-11-28.
 */
final class BindBuilder {
    private static final String CLASS_UNIFORM_MARK = "$$BusBinder";

    private String packagePath;//包名路径，如com.example
    private String targetClassName;//目标类名
    private Set<MethodValue> methods;

    private int hashCode;

    public BindBuilder(String packagePath, String targetClassName) {
        this.packagePath = packagePath;
        this.targetClassName = targetClassName;
        methods = new LinkedHashSet<>();

        hashCode = packagePath.hashCode() + targetClassName.hashCode();
    }

    public void addMethodValue(MethodValue methodValue) {
        methods.add(methodValue);
    }

    /**
     * Build class for javapoet
     *
     * @param filer
     */
    public void build(Filer filer) {

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        BindBuilder builder = (BindBuilder) obj;
        if (builder.packagePath == null || builder.targetClassName == null)
            return false;

        if (builder.packagePath.equals(packagePath) && builder.targetClassName.equals(targetClassName))
            return true;
        else
            return false;
    }

    /**
     * targer class 's method
     */
    private final class MethodValue {
        private String methodName;//方法名
        private Set<String> tags;//对应的tag
        private String threadType;//对应的Rx线程

        private int hashCode;

        public MethodValue(String methodName, String threadType) {
            this.methodName = methodName;
            this.threadType = threadType;
            tags = new LinkedHashSet<>();
            hashCode = methodName.hashCode() + threadType.hashCode();
        }

        public void addTag() {
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
