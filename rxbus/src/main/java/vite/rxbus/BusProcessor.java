package vite.rxbus;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.exceptions.MissingBackpressureException;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.internal.util.BackpressureHelper;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.processors.FlowableProcessor;

/**
 * rewrite PublishProcessor
 * <p>
 * Created by trs on 17-3-20.
 */
final class BusProcessor<T> extends FlowableProcessor<T> {
    /**
     * The terminated indicator for the subscribers array.
     */
    @SuppressWarnings("rawtypes")
    static final BusSubscription[] TERMINATED = new BusSubscription[0];
    /**
     * An empty subscribers array to avoid allocating it all the time.
     */
    @SuppressWarnings("rawtypes")
    static final BusSubscription[] EMPTY = new BusSubscription[0];

    /**
     * The array of currently subscribed subscribers.
     */
    final AtomicReference<BusSubscription<T>[]> subscribers;

    /**
     * The error, write before terminating and read after checking subscribers.
     */
    Throwable error;

    /**
     * Constructs a PublishProcessor.
     *
     * @param <T> the value type
     * @return the new PublishProcessor
     */
    public static <T> BusProcessor<T> create() {
        return new BusProcessor<T>();
    }

    /**
     * Constructs a PublishProcessor.
     *
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    BusProcessor() {
        subscribers = new AtomicReference<BusSubscription<T>[]>(EMPTY);
    }


    @Override
    public void subscribeActual(Subscriber<? super T> t) {
        BusSubscription<T> ps = new BusSubscription<T>(t, this);
        t.onSubscribe(ps);
        if (add(ps)) {
            // if cancellation happened while a successful add, the remove() didn't work
            // so we need to do it again
            if (ps.isCancelled()) {
                remove(ps);
            }
        } else {
            Throwable ex = error;
            if (ex != null) {
                t.onError(ex);
            } else {
                t.onComplete();
            }
        }
    }

    /**
     * Tries to add the given subscriber to the subscribers array atomically
     * or returns false if the subject has terminated.
     *
     * @param ps the subscriber to add
     * @return true if successful, false if the subject has terminated
     */
    boolean add(BusSubscription<T> ps) {
        for (; ; ) {
            BusSubscription<T>[] a = subscribers.get();
            if (a == TERMINATED) {
                return false;
            }

            int n = a.length;
            @SuppressWarnings("unchecked")
            BusSubscription<T>[] b = new BusSubscription[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = ps;

            if (subscribers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    /**
     * Atomically removes the given subscriber if it is subscribed to the subject.
     *
     * @param ps the subject to remove
     */
    @SuppressWarnings("unchecked")
    void remove(BusSubscription<T> ps) {
        for (; ; ) {
            BusSubscription<T>[] a = subscribers.get();
            if (a == TERMINATED || a == EMPTY) {
                return;
            }

            int n = a.length;
            int j = -1;
            for (int i = 0; i < n; i++) {
                if (a[i] == ps) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                return;
            }

            BusSubscription<T>[] b;

            if (n == 1) {
                b = EMPTY;
            } else {
                b = new BusSubscription[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }
            if (subscribers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (subscribers.get() == TERMINATED) {
            s.cancel();
            return;
        }
        // PublishSubject doesn't bother with request coordination.
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T t) {
        if (subscribers.get() == TERMINATED) {
            return;
        }
        if (t == null) {
            onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
            return;
        }
        for (BusSubscription<T> s : subscribers.get()) {
            s.onNext(t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onError(Throwable t) {
        if (subscribers.get() == TERMINATED) {
            RxJavaPlugins.onError(t);
            return;
        }
        if (t == null) {
            t = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        }
        error = t;

        for (BusSubscription<T> s : subscribers.getAndSet(TERMINATED)) {
            s.onError(t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onComplete() {
        if (subscribers.get() == TERMINATED) {
            return;
        }
        for (BusSubscription<T> s : subscribers.getAndSet(TERMINATED)) {
            s.onComplete();
        }
    }

    @Override
    public boolean hasSubscribers() {
        return subscribers.get().length != 0;
    }

    @Override
    public Throwable getThrowable() {
        if (subscribers.get() == TERMINATED) {
            return error;
        }
        return null;
    }

    @Override
    public boolean hasThrowable() {
        return subscribers.get() == TERMINATED && error != null;
    }

    @Override
    public boolean hasComplete() {
        return subscribers.get() == TERMINATED && error == null;
    }

    /**
     * Wraps the actual subscriber, tracks its requests and makes cancellation
     * to remove itself from the current subscribers array.
     *
     * @param <T> the value type
     */
    static final class BusSubscription<T> extends AtomicLong implements Subscription {

        private static final long serialVersionUID = 3268985049210991996L;
        /**
         * The actual subscriber.
         */
        final Subscriber<? super T> actual;
        /**
         * The subject state.
         */
        final BusProcessor<T> parent;

        /**
         * Constructs a PublishSubscriber, wraps the actual subscriber and the state.
         *
         * @param actual the actual subscriber
         * @param parent the parent PublishProcessor
         */
        BusSubscription(Subscriber<? super T> actual, BusProcessor<T> parent) {
            this.actual = actual;
            this.parent = parent;
        }

        public void onNext(T t) {
            long r = get();
            if (r == Long.MIN_VALUE) {
                return;
            }
            if (r != 0L) {
                actual.onNext(t);
                if (r != Long.MAX_VALUE) {
                    decrementAndGet();
                }
            } else {
                cancel();
                actual.onError(new MissingBackpressureException("Could not emit value due to lack of requests"));
            }
        }

        public void onError(Throwable t) {
            if (get() != Long.MIN_VALUE) {
                actual.onError(t);
            } else {
                RxJavaPlugins.onError(t);
            }
        }

        public void onComplete() {
            if (get() != Long.MIN_VALUE) {
                actual.onComplete();
            }
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.addCancel(this, n);
            }
        }

        @Override
        public void cancel() {
            if (getAndSet(Long.MIN_VALUE) != Long.MIN_VALUE) {
                parent.remove(this);
            }
        }

        public boolean isCancelled() {
            return get() == Long.MIN_VALUE;
        }
    }
}
