package vite.rxbus;

import java.util.Objects;

/**
 * Created by trs on 17-6-7.
 */

final class StickyHolder<T, V> {
    public final T tag;
    public final V value;

    private final Class mValueType;

    public StickyHolder(T tag, V value) {
        this.tag = tag;
        this.value = value;
        mValueType = value.getClass();
    }

    @Override
    public int hashCode() {
        return (tag == null ? 0 : tag.hashCode()) ^ (mValueType == null ? 0 : mValueType.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StickyHolder))
            return false;

        StickyHolder<?, ?> holder = (StickyHolder<?, ?>) obj;
        return Objects.equals(holder.tag, tag) && Objects.equals(holder.mValueType, mValueType);
    }

    @Override
    public String toString() {
        return "StickyHolder{ Tag:" + String.valueOf(tag) +
                ", Value Type:" + String.valueOf(mValueType) +
                ", Value:" + value + "}";
    }
}
