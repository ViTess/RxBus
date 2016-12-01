package vite.rxbus;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by trs on 16-11-28.
 */
public interface BusBinder {
    void setBinders(Map<ParamKeeper, CopyOnWriteArraySet<SubjectKeeper>> map);

    void release();
}
