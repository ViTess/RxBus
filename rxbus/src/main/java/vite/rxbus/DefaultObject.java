package vite.rxbus;

/**
 * it just replace Object to post
 * Created by trs on 17-5-25.
 */

public final class DefaultObject {

    private final int mSeed = 17;
    private final Object mObject;
    private final int mObjectHashCode;

    private int mHashCode = -1;

    public DefaultObject() {
        mObject = new Object();
        mObjectHashCode = mObject.hashCode();
    }

    @Override
    public int hashCode() {
        if (mHashCode == -1)
            mHashCode = 31 * mSeed + mObjectHashCode;
        return mHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DefaultObject))
            return false;
        if (obj == this)
            return true;
        else if (obj.hashCode() == this.hashCode())
            return true;
        else
            return false;
    }
}
