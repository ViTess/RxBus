package vite.rxbus;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by trs on 17-3-21.
 */
final class WeakHashSet<T> extends HashSet {

    private ReferenceQueue mRefQueue = new ReferenceQueue();

    @Override
    public boolean add(Object o) {
        return super.add(new WeakNode(o, mRefQueue));
    }

    @Override
    public boolean remove(Object o) {
        boolean result = super.remove(new WeakNode(o));
        expungeStaleEntries();
        return result;
    }

    @Override
    public boolean contains(Object o) {
        return super.contains(new WeakNode(o));
    }

    public int size() {
        expungeStaleEntries();
        return super.size();
    }

    /**
     *
     */
    private void expungeStaleEntries() {
        for (WeakNode node; (node = (WeakNode) mRefQueue.poll()) != null; ) {
            synchronized (mRefQueue) {
                super.remove(node);
            }
        }
    }

    @Override
    public Iterator<T> iterator() {
        expungeStaleEntries();
        return new WeakHashSetIterator();
    }

    private class WeakHashSetIterator implements Iterator {

        Iterator iter;

        WeakHashSetIterator() {
            iter = WeakHashSet.super.iterator();
        }


        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public Object next() {
            return iter.next() == null ? null : ((WeakReference) iter.next()).get();
        }

        @Override
        public void remove() {
            iter.remove();
        }
    }


    private static class WeakNode extends WeakReference {

        private int hash;

        public WeakNode(Object ref) {
            super(ref);
            hash = ref.hashCode();
        }

        public WeakNode(Object o, ReferenceQueue q) {
            super(o, q);
            hash = o.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)//same reference
                return true;

            if (!(obj instanceof WeakNode))
                return false;

            //same hash code means same obj
            return this.hash == ((WeakNode) obj).hash;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
