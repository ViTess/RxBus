package vite.rxbus;

/**
 * Created by trs on 16-11-28.
 */
public interface BusBinder {
    /**
     * @param value
     * @return true - success ; false - need release
     */
    boolean post(Object value);

    void release();
}
