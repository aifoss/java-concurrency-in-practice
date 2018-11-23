package org.concurrency.part_1_fundamentals.chap04_composing_objects;

import org.concurrency.Annotation.NotThreadSafe;
import org.concurrency.Annotation.ThreadSafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Created by sofia on 5/27/17.
 */

/**
 * 4.4. Adding Functionality to Existing Thread-Safe Classes
 *
 * Sometimes a thread-safe class that supports all of the operations we want already exists,
 * but often the best we can find is a class that supports almost all the operations we want,
 * and then we need to add a new operation to it without undermining its thread safety.
 *
 * The safest way to add a new atomic operation is to modify the original class to support the desired operations,
 * but this is not always possible because you may not have access to the source code or may not be free to modify it.
 *
 * Another approach is to extend the class, assuming it was designed for extension.
 * BetterVector in Listing 4.13 extends Vector to add a putIfAbsent method.
 *
 * Extension is more fragile than adding code directly to a class,
 * because the implementation of the synchronization policy is now distributed over multiple, separately maintained source files.
 *
 * 4.4.1. Client-side Locking
 *
 * A third strategy is to extend the functionality of the class without extending the class itself
 * by placing extension code in a "helper" class.
 *
 * Listing 4.14 shows a failed attempt to create a helper class with an atomic put-if-absent operation for operating on a thread-safe List.
 * The problem is that it synchronizes on the wrong lock.
 * Whatever lock the List uses to guard its state, it sure isn't the lock on the ListHelper.
 * This means that putIfAbsent is not atomic relative to other operations on the List.
 *
 * To make this approach work, we have to use the same lock that the List uses by using client-side locking or external locking.
 * Client-side locking entails guarding client code that uses some object X with the lock X uses to guard its own state.
 *
 * Listing 4.15 shows a putIfAbsent operation on a thread-safe List that correctly uses client-side locking.
 *
 * If extending a class to add another atomic operation is fragile because it distributes the locking code for a class over multiple classes,
 * client-side locking is even more fragile because it entails putting locking code for class C into classes that are totally unrelated to C.
 *
 * Client-side locking has a lot in common with class extension
 * - they both couple the behavior of the derived class to the implementation of the base class.
 * Just as extension violates encapsulation of implementation,
 * client-side locking violates encapsulation of synchronization policy.
 *
 * 4.4.2. Composition
 *
 * There is a less fragile alternative for adding an atomic operation to an existing class: composition.
 *
 * ImprovedList in Listing 4.16 implements the List operations by delegating them to an underlying List instance,
 * and adds an atomic putIfAbsent method.
 *
 * Improved List uses an additional level of locking using its own intrinsic lock.
 * While the extra layer of synchronization may add some small performance penalty,
 * the implementation in ImprovedList is less fragile than attempting to mimic the locking strategy of another object.
 * In effect, we've used the Java monitor pattern to encapsulate an existing List,
 * and this is guaranteed to provide thread safety so long as our class holds the only outstanding reference to the underlying list.
 */
public class Sec0404_AddingFunctionality {

    /**
     * Listing 4.13. Extending Vector to have a Put-if-absent Method.
     */
    @ThreadSafe
    public static class BetterVector<E> extends Vector<E> {
        public synchronized boolean putIfAbsent(E x) {
            boolean absent = !contains(x);
            if (absent)
                add(x);
            return absent;
        }
    }

    /**
     * 4.4.1. Client-side Locking
     */

    /**
     * Listing 4.14. Non-thread-safe Attempt to Implement Put-if-absent. Don't Do this.
     */
    @NotThreadSafe
    public static class ListHelper<E> {
        public List<E> list = Collections.synchronizedList(new ArrayList<>());

        public synchronized boolean putIfAbsent(E x) {
            boolean absent = !list.contains(x);
            if (absent)
                list.add(x);
            return absent;
        }
    }

    /**
     * Listing 4.15. Implementing Put-if-absent with Client-side Locking.
     */
    @ThreadSafe
    public static class ListHelper2<E> {
        public List<E> list = Collections.synchronizedList(new ArrayList<>());

        public boolean putIfAbsent(E x) {
            synchronized (list) {
                boolean absent = !list.contains(x);
                if (absent)
                    list.add(x);
                return absent;
            }
        }
    }

    /**
     * 4.4.2. Composition
     */

    /**
     * Listing 4.16. Implementing Put-if-absent Using Composition.
     */
    @ThreadSafe
    public abstract static class ImprovedList<T> implements List<T> {
        private final List<T> list;

        public ImprovedList(List<T> list) {
            this.list = list;
        }

        public synchronized boolean putIfAbsent(T x) {
            boolean contains = list.contains(x);
            if (!contains)
                list.add(x);
            return !contains;
        }

        public synchronized void clear() {
            list.clear();
        }
    }

    public abstract static class ImprovedListExtended<T> extends ImprovedList<T> {
        public ImprovedListExtended(List<T> list) {
            super(list);
        }
    }



    public static void main(String[] args) {
        BetterVector<Integer> betterVector = new BetterVector<>();
        ListHelper<Integer> listHelper = new ListHelper<>();
        ListHelper2<Integer> listHelper2 = new ListHelper2<>();

    }

}
