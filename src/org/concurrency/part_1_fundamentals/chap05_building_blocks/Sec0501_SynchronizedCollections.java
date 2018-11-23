package org.concurrency.part_1_fundamentals.chap05_building_blocks;

import org.concurrency.java_concurrency_in_practice.Annotation.GuardedBy;
import org.concurrency.java_concurrency_in_practice.Annotation.NotThreadSafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

/**
 * Created by sofia on 5/27/17.
 */

/**
 * 5.1. Synchronized Collections
 *
 * The synchronized collection classes achieve thread safety by encapsulating their state and synchronizing every public method
 * so that only one thread at a time can access the collection state.
 *
 * 5.1.1. Problems with Synchronized Collections
 *
 * The synchronized collections are thread-safe, but you may sometimes need to use additional client-side locking to guard compound actions.
 * Common compound actions on collections include iteration, navigation, and conditional operations such as put-if-absent.
 * With a synchronized collection, these compound actions are still technically thread-safe even without client-side locking,
 * but they not behave as you might expect when other threads can concurrently modify the collection.
 *
 * Listing 5.1 shows two methods that operate on a Vector, getLast and deleteLast, both of which are check-then-act sequences.
 *
 * Because the synchronized collections commit to a synchronization policy that supports client-side locking,
 * it is possible to create new operations that are atomic with respect to other collection operations
 * as long as we know which lock to use.
 * The synchronized collection classes guard each method with the lock on the synchronized collection object itself.
 *
 * By acquiring the collection lock we can make getLast and deleteLast atomic,
 * ensuring that the size of the Vector does not change between calling size and get, as shown in Listing 5.2.
 *
 * The risk that the size of the list might change between a call to size and the corresponding call to get is also present
 * when we iterate through the elements of a Vector, as show in Listing 5.3.
 *
 * The problem of unreliable iteration can again be addressed by client-side locking, at some additional cost to scalability.
 * By holding the Vector lock for the duration of iteration, as shown in Listing 5.4,
 * we prevent other threads from modifying the Vector while we are iterating it.
 * Unfortunately, we also prevent other threads from accessing it at all during this time, impairing concurrency.
 *
 * 5.1.2. Iterators and ConcurrentModificationException
 *
 * The standard way to iterate a Collection is with an Iterator,
 * but using iterators does not obviate the need to lock the collection during iteration if other threads can concurrently modify it.
 *
 * The iterators returned by the synchronized collections are not designed to deal with concurrent modification,
 * and they are fail-fast - meaning that if they detect that the collection has changed since iteration began,
 * they throw the unchecked ConcurrentModificationException.
 *
 * These fail-fast iterators are designed to catch concurrency errors on a "good-faith-effort" basis
 * and thus act only as early-warning indicators for concurrency problems.
 *
 * Listing 5.5 illustrates iterating a collection with the for-each loop syntax.
 * The way to prevent ConcurrentModificationException is to hold the collection lock for the duration of the iteration.
 *
 * There are several reasons, however, why locking a collection during iteration may be undesirable.
 * Other threads that need to access the collection will block until the iteration is complete;
 * if the collection is large or the task performed for each element is lengthy, they could wait a long time.
 * Also, if the collection is locked, as in Listing 5.4, doSomething is being called with a lock held, which is a risk actor for deadlock.
 * Even in the absence of starvation or deadlock risk, locking collections for significant periods of time hurts application scalability.
 * The longer a lock is held, the more likely it is to be contended,
 * and if many threads are blocked waiting for a lock throughout and CPU utilization can suffer.
 *
 * An alternative to locking the collection during iteration is to choose the collection and iterate the copy instead.
 * Since the clone is thread-confined, no other thread can modify it during iteration,
 * eliminating the possibility of ConcurrentModificationException.
 * (The collection still must be locked during the clone operation itself.)
 *
 * Cloning the collection has an obvious performance cost;
 * whether this is a favorable tradeoff depends on many factors including the size of the collection,
 * how much work is done for each element, the relative frequency of iteration compared to other collection operations,
 * and responsiveness and throughput requirements.
 *
 * 5.1.3. Hidden Iterators
 *
 * You have to remember to use locking everywhere a shared collection might be iterated.
 * This is trickier than it sounds, as iterators are sometimes hidden, as in HiddenIterator in Listing 5.6.
 *
 * HiddenIterator is not thread-safe;
 * the HiddenIterator lock should be acquired before using set in the println call,
 * but debugging and logging code commonly neglect to do this.
 *
 * The real lesson here is that the greater the distance between the state and the synchronization that guards it,
 * the more likely that someone will forget to use proper synchronization when accessing that state.
 *
 * If HiddenIterator wrapped the HashSet with a synchronizedSet, encapsulating the synchronization,
 * this sort of error would not occur.
 *
 * ****************************************************************************************
 * Just as encapsulating an object's state makes it easier to preserve its invariants,
 * encapsulating its synchronization makes it easier to enforce its synchronization policy.
 * ****************************************************************************************
 *
 * Iteration is also indirectly invoked by the collection's hashCode and equals methods,
 * which may be called if the collection is used as an element or key of another collection.
 * Similarly, the containsAll, removeAll, and retainAll methods, as well as the constructors that take collections as arguments,
 * also iterate the collection.
 * All of these indirect uses of iteration can cause ConcurrentModificationException.
 */
public class Sec0501_SynchronizedCollections {

    /**
     * 5.1.1. Problems with Synchronized Collections
     */

    /**
     * Listing 5.1. Compound Actions on a Vector that may Produce Confusing Results.
     */
    public static Object getLast(Vector list) {
        int lastIndex = list.size()-1;
        return list.get(lastIndex);
    }

    public static void deleteLast(Vector list) {
        int lastIndex = list.size()-1;
        list.remove(lastIndex);
    }

    /**
     * Listing 5.2. Compound Actions on Vector Using Client-side Locking.
     */
    public static Object getLast2(Vector list) {
        synchronized (list) {
            int lastIndex = list.size()-1;
            return list.get(lastIndex);
        }
    }

    public static void deleteLast2(Vector list) {
        synchronized (list) {
            int lastIndex = list.size()-1;
            list.remove(lastIndex);
        }
    }

    /**
     * 5.1.3. Hidden Iterators
     */

    /**
     * Listing 5.6. Iteration Hidden within String Concatenation. Don't Do this.
     */
    @NotThreadSafe
    public static class HiddenIterator {
        @GuardedBy("this")
        private final Set<Integer> set = new HashSet<>();

        public synchronized void add(Integer i) {
            set.add(i);
        }

        public synchronized void remove(Integer i) {
            set.remove(i);
        }

        public void addTenThings() {
            Random r = new Random();
            for (int i = 0; i < 10; i++)
                add(r.nextInt());
            System.out.println("DEBUG: added ten elements to " + set); // Hidden iteration
        }
    }



    public static void main(String[] args) {
        /**
         * 5.1.1. Problems with Synchronized Collections
         */

        /**
         * Listing 5.3. Iteration that may Throw ArrayIndexOutOfBoundsException.
         */
        Vector vector = new Vector();
        for (int i = 0; i < vector.size(); i++)
            doSomething(vector.get(i));

        /**
         * Listing 5.4. Iteration with Client-side Locking.
         */
        synchronized (vector) {
            for (int i = 0; i < vector.size(); i++)
                doSomething(vector.get(i));
        }

        /**
         * 5.1.2. Iterators and ConcurrentModificationException
         */

        /**
         * Listing 5.5. Iterating a List with an Iterator.
         */
        List<Widget> widgetList = Collections.synchronizedList(new ArrayList<>());

        // May throw ConcurrentModificationException
        for (Widget w : widgetList)
            doSomething(w);

        /**
         * 5.1.3. Hidden Iterators
         */

        HiddenIterator hiddenIterator = new HiddenIterator();
    }

    private static void doSomething(Object o) {}

    static class Widget {

    }

}
