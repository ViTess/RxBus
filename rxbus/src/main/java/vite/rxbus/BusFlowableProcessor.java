package vite.rxbus;

import org.reactivestreams.Processor;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.annotations.NonNull;

/**
 * rewrite to FlowableProcessor
 *
 * Represents a Subscriber and a Flowable (Publisher) at the same time, allowing
 * multicasting events from a single source to multiple child Subscribers.
 * <p>All methods except the onSubscribe, onNext, onError and onComplete are thread-safe.
 * Use {@link #toSerialized()} to make these methods thread-safe as well.
 *
 * @param <T> the item value type
 */
abstract class BusFlowableProcessor<T> extends Flowable<T> implements Processor<T, T>, FlowableSubscriber<T> {

    /**
     * Returns true if the subject has subscribers.
     * <p>The method is thread-safe.
     * @return true if the subject has subscribers
     */
    public abstract boolean hasSubscribers();

    /**
     * Returns true if the subject has reached a terminal state through an error event.
     * <p>The method is thread-safe.
     * @return true if the subject has reached a terminal state through an error event
     * @see #getThrowable()
     * @see #hasComplete()
     */
    public abstract boolean hasThrowable();

    /**
     * Returns true if the subject has reached a terminal state through a complete event.
     * <p>The method is thread-safe.
     * @return true if the subject has reached a terminal state through a complete event
     * @see #hasThrowable()
     */
    public abstract boolean hasComplete();

    /**
     * Returns the error that caused the Subject to terminate or null if the Subject
     * hasn't terminated yet.
     * <p>The method is thread-safe.
     * @return the error that caused the Subject to terminate or null if the Subject
     * hasn't terminated yet
     */
    public abstract Throwable getThrowable();

    /**
     * new add
     */
    public abstract void dispose();

    /**
     * new add
     */
    public abstract boolean isDispose();

    /**
     * Wraps this Subject and serializes the calls to the onSubscribe, onNext, onError and
     * onComplete methods, making them thread-safe.
     * <p>The method is thread-safe.
     * @return the wrapped and serialized subject
     */
    @NonNull
    public final BusFlowableProcessor<T> toSerialized() {
        if (this instanceof BusSerializedProcessor) {
            return this;
        }
        return new BusSerializedProcessor<T>(this);
    }
}
