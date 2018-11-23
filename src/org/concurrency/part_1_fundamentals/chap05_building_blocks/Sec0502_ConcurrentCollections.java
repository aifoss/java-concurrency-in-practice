package org.concurrency.part_1_fundamentals.chap05_building_blocks;

/**
 * Created by sofia on 5/27/17.
 */

import java.util.Map;

/**
 * 5.2. Concurrent Collections
 *
 * Synchronized collections achieve their thread safety by serializing all access to the collection's state.
 * The synchronized collections classes hold a lock for the duration of each operation.
 * The cost of this approach is poor concurrency;
 * when multiple threads contend fo the collection-wide lock, throughput suffers.
 *
 * The concurrent collections, on the other hand, are designed for concurrent access from multiple threads.
 *
 * ****************************************************************************************************************************
 * Replacing synchronized collections with concurrent collections can offer dramatic scalability improvements with little risk.
 * ****************************************************************************************************************************
 *
 * Java 5.0 adds ConcurrentHashMap, a replacement for synchronized hash-based Map implementations,
 * and CopyOnWriteArrayList, a replacement for synchronized List implementations for cases where traversal is the dominant operation.
 *
 * Java 5.0 also adds two new collection types, Queue and BlockingQueue.
 * BlockingQueue extends Queue to add blocking insertion and retrieval operations.
 * If the queue is empty, a retrieval blocks until an element is available,
 * and if the queue is full, an insertion blocks until there is space available.
 *
 * Java 6 adds ConcurrentSkipListMap and ConcurrentSkipListSet,
 * which are concurrent replacements for a synchronized SortedMap or SortedSet.
 *
 * 5.2.1. ConcurrentHashMap
 *
 * Instead of synchronizing every method on a common lock, restricting access to a single thread at a time, as in synchronizedMap,
 * ConcurrentHashMap uses a finer-grained locking mechanism called "lock striping" to allow a greater degree of shared access.
 * Arbitrarily many reading threads can access the map concurrently,
 * readers can access the map concurrently with writers,
 * and a limited number of writers can modify the map concurrently.
 * The result is far greater throughput under concurrent access, with little performance penalty for single-threaded access.
 *
 * ConcurrentHashMap, along with the other concurrent collections, further improve on the synchronized collection classes
 * by providing iterators that do not throw ConcurrentModificationException,
 * thus eliminating the need to lock the collection during iteration.
 *
 * The iterators returned by ConcurrentHashMap are weakly consistent instead of fail-fast.
 * A weakly consistent iterator can tolerate concurrent modification, traverse elements as they existed when the iterator was constructed,
 * and may (but is not guaranteed to) reflect modifications to the collection after the construction of the iterator.
 *
 * The one feature offered by the synchronized Map implementations but not by ConcurrentHashMap
 * is the ability to lock the map for exclusive access.
 *
 * Because it has so many advantages and so few disadvantages compared to Hashtable or synchronizedMap,
 * replacing synchronized Map implementations with ConcurrentHashMap in most cases results only in better scalability.
 * Only if your application needs to lock the map for exclusive access is ConcurrentHashMap not an appropriate drop-in replacement.
 *
 * 5.2.2. Additional Atomic Map Operations
 *
 * Since ConcurrentHashMap cannot be locked for exclusive access, we cannot use client-side locking to create new atomic operations.
 * Instead, a number of common compound operations, such as put-if-absent, remove-if-equal, and replace-if-equal,
 * are implemented as atomic operations, as shown in Listing 5.7.
 *
 * 5.2.3. CopyOnWriteArrayList
 *
 * CopyOnWriteArrayList/Set is a concurrent replacement for a synchronized List/Set that offers better concurrency in some common situations
 * and eliminates the need to lock or copy the collection during iteration.
 *
 * The copy-on-write collections derive their thread safety from the fact
 * that as long as an effectively immutable object is properly published, no further synchronization is required when accessing it.
 * They implement mutability by creating and republishing a new copy of the collection every time it is modified.
 *
 * Iterators for the copy-on-write collections retain a reference to the backing array that was current at the start of iteration,
 * and since this will never change, they need to synchronize only briefly to ensure visibility of the array contents.
 * As a result, multiple threads can iterate the collection without interference from one another or from threads wanting to modify the collection.
 * The iterators returned by the copy-on-write collections do not throw ConcurrentModificationException
 * and return the elements exactly as they were at the time the iterator was created, regardless of subsequent modifications.
 *
 * The copy-on-write collections are reasonable to use only when iteration is far more common than modification.
 * This criterion exactly describes many event-notification systems:
 * delivering a notification requires iterating the list of registered listeners and calling each one of them,
 * and in most cases registering or unregistering an event listener is far less common than receiving an event notification.
 */
public class Sec0502_ConcurrentCollections {

    /**
     * 5.2.2. Additional Atomic Map Operations
     */

    /**
     * Listing 5.7. ConcurrentHashMap Interface.
     */
    public interface ConcurrentHashMap<K, V> extends Map<K, V> {
        // Insert into map only if no value is mapped from K
        V putifAbsent(K key, V value);

        // Remove only if K is mapped to V
        // boolean remove(K key, V value);

        // Replace value only if K is mapped to oldValue
        boolean replace(K key, V oldValue, V newValue);

        // Replace value only if K is mapped to some value
        V replace(K key, V newValue);
    }



    public static void main(String[] args) {

    }

}
