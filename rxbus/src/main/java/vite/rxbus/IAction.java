package vite.rxbus;

/**
 * Created by trs on 17-3-21.
 */
public interface IAction<T, V> {
    void toDo(T t, V v);
}
