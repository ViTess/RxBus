package vite.demo;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import vite.rxbus.BusProxy;

/**
 * Created by trs on 17-1-4.
 */
public final class MainActivity$$Proxy_test extends BusProxy<MainActivity> {
    public MainActivity$$Proxy_test() {
        createMethod("test1", AndroidSchedulers.mainThread()
                , new ProxyAction<MainActivity, Integer>() {
                    @Override
                    public void toDo(MainActivity mainActivity, Integer v) {
                        mainActivity.test(v);
                    }
                });
    }
}
