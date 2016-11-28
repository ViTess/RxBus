package vite.rxbus;

/**
 * Created by trs on 16-11-28.
 */
final class ParamKey {
    private String tag;//tag
    private Class valueType;//传递值的class类型

    private int hashCode;

    /**
     * @param tag
     * @param valueType 方法传入参数的类型
     */
    public ParamKey(String tag, Class valueType) {
        this.tag = tag;
        this.valueType = getClassType(valueType);
        hashCode = this.tag.hashCode() + this.valueType.hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final ParamKey other = (ParamKey) obj;

        return tag.equals(other.tag) && valueType == other.valueType;
    }

    @Override
    public String toString() {
        return "ParamKey{" +
                "tag='" + tag + '\'' +
                ", valueType=" + valueType +
                ", hashCode=" + hashCode +
                '}';
    }

    private Class getClassType(Class clazz) {
        if (clazz == Boolean.class)
            return Boolean.TYPE;
        if (clazz == Character.class)
            return Character.TYPE;
        if (clazz == Byte.class)
            return Byte.TYPE;
        if (clazz == Short.class)
            return Short.TYPE;
        if (clazz == Integer.class)
            return Integer.TYPE;
        if (clazz == Long.class)
            return Long.TYPE;
        if (clazz == Float.class)
            return Float.TYPE;
        if (clazz == Double.class)
            return Float.TYPE;
        if (clazz == Void.class)
            return Float.TYPE;
        return clazz;
    }
}
