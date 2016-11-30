package vite.rxbus;

import java.util.Map;
import java.util.Set;

/**
 * Created by trs on 16-11-28.
 */
public interface BusBinder {
    void setBinders(Map<ParamKeeper, Set<SubjectKeeper>> map);

    void release();
}
