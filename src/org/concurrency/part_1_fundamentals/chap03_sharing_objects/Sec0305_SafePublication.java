package org.concurrency.part_1_fundamentals.chap03_sharing_objects;

/**
 * Created by sofia on 5/26/17.
 */

/**
 * 3.5. Safe Publication
 *
 * Simply storing a reference to an object into a public field, as in Listing 3.14, is not enough to publish that object safely.
 *
 * 3.5.1. Improper Publication: When Good Objects Go Bad
 *
 * If the Holder in Listing 3.15 is published using the unsafe publication idiom in Listing 3.14,
 * and a thread other than the publishing thread were to call assertSanity,
 * it could throw AssertionError.
 *
 * Because synchronization was not used to make the Holder visible to other threads,
 * we say the Holder was not properly published.
 *
 * Two things can go wrong with improperly published objects:
 * Other threads could see a stale value for the holder field, and thus see a null reference or other older value
 * even though a value has been placed in holder.
 * But far worse, other threads could see an up-to-date value for the holder reference,
 * but stale values for the state of the Holder.
 * To make things even less predictable,
 * a thread may see a stale value the first time it reads a field and then a more up-to-date value the next time,
 * which is why assertSanity can throw AssertionError.
 *
 * 3.5.2. Immutable objects and Initialization Safety
 *
 * Because immutable objects are so important,
 * the Java Memory Model offers a special guarantee of initialization safety for sharing immutable objects.
 *
 * That an object reference becomes visible to another thread does not necessarily mean
 * that the state of that object is visible to the consuming thread.
 * In order to guarantee a consistent view of the object's state, synchronization is needed.
 *
 * Immutable objects, on the other hand, can be safely accessed even when synchronization is not used to publish the object reference.
 *
 * **************************************************************************************
 * Immutable objects can be used safely by any thread without additional synchronization,
 * even when synchronization is not used to publish them.
 * **************************************************************************************
 *
 * This guarantee extends to the values of all final fields of properly constructed objects;
 * final fields can be safely accessed without additional synchronization.
 * However, if final fields refer to mutable objects,
 * synchronization is still required to access the state of the objects they refer to.
 *
 * 3.5.3. Safe Publication Idioms
 *
 * **********************************************************************************************************
 * To publish an object safely, both the reference to the object and the object's state must be made visible
 * to other threads at the same time.
 * A properly constructed object can be safely published by:
 * - Initializing an object reference from a static initializer
 * - Storing a reference to it into a volatile field or AtomicReference
 * - Storing a reference to it into a final field of a properly constructed object
 * - Storing a reference to it into a field that is properly guarded by a lock
 * **********************************************************************************************************
 *
 * The internal synchronization in thread-safe collections means that placing an object in a thread-safe collection
 * fulfils the last of these requirements.
 *
 * The thread-safe library collections offer the following safe publication guarantees:
 * - Placing a key or value in a Hashtable, synchronizedMap, or ConcurrentMap safely publishes it
 *   to any thread that retrieves it from the Map (whether directly or via an iterator).
 * - Placing an element in a Vector, CopyOnWriteArrayList, CopyOnWriteArraySet, synchronizedList, or synchronizedSet
 *   safely publishes it to any thread that retrieves it from the collection.
 * - Placing an element on a BlockingQueue or a ConcurrentLinkedQueue safely publishes it
 *   to any thread that retrieves it from the queue.
 *
 * Using a static initializer is often the easiest and safest way to publish objects that can be statically constructed:
 *
 *      public static Holder holder = new Holder(42);
 *
 * Static initializers are executed by the JVM at class initialization time;
 * because of internal synchronization in the JVM, this mechanism is guaranteed to safely publish any objects initialized in this way.
 *
 * 3.5.4. Effectively Immutable Objects
 *
 * Objects that are not technically immutable, but whose state will not be modified after publication, are called "effectively immutable".
 * Using effectively immutable objects can simplify development and improve performance by reducing the need for synchronization.
 *
 * *******************************************************************************************************************
 * Safely published effectively immutable objects can be used safely by any thread without additional synchronization.
 * *******************************************************************************************************************
 *
 * Suppose you want to maintain a Map storing the last login time of each user:
 *
 *      public Map<String, Date> lastLogin = Collections.synchronizedMap(new HashMap<>());
 *
 * If the Date values are not modified after they are placed in the Map,
 * then the synchronization in the synchronizedMap implementation is sufficient to publish the Date values safely.
 *
 * 3.5.5. Mutable Objects
 *
 * If an object may be modified after construction, safe publication ensures only the visibility of the as-published state.
 * Synchronization must be used not only to publish a mutable object,
 * but also every time the object is accessed to ensure visibility of subsequent modifications.
 * To share mutable objects safely, they must be safely published and be either thread-safe or guarded by a lock.
 *
 * ***********************************************************************************************
 * The publication requirements for an object depend on its mutability:
 * - Immutable objects can be published through any mechanism.
 * - Effectively immutable objects must be safely published.
 * - Mutable objects must be safely published and must be either thread-safe or guarded by a lock.
 * ***********************************************************************************************
 *
 * 3.5.6. Sharing Objects Safely
 *
 * Whenever you acquire a reference to an object, you should know what you are allowed to do with it.
 * Many concurrency errors stem from failing to understand these "rules of engagement" for a shared object.
 * When you publish an object, you should document how the object can be accessed.
 *
 * **************************************************************************************************************
 * The most useful policies for using and sharing objects in a concurrent program are:
 *
 * Thread-confined:
 * A thread-confined object is owned exclusively by and confined to one thread,
 * and can be modified by its owning thread.
 *
 * Shared read-only:
 * A shared read-only object can be accessed concurrently by multiple threads without additional synchronization,
 * but cannot be modified any thread.
 * Shared read-only objects include immutable and effectively immutable objects.
 *
 * Shared thread-safe:
 * A thread-safe object performs synchronization internally, so multiple threads can freely access it
 * through its public interface without further synchronization.
 *
 * Guarded:
 * A guarded object can be accessed only with a specific lock held.
 * Guarded objects include those that are encapsulated within other thread-safe objects
 * and published objects that are known to be guarded by a specific lock.
 * **************************************************************************************************************
 */
public class Sec0305_SafePublication {

    /**
     * Listing 3.14. Publishing an Object without Adequate Synchronization. Don't Do this.
     */
    // Unsafe publication
    public Holder holder;

    public void initialize() {
        holder = new Holder(42);
    }

    /**
     * Listing 3.15. Class at Risk of Failure if Not Properly Published.
     */
    public static class Holder {
        private int n;

        public Holder(int n) { this.n = n; }

        public void assertSanity() {
            if (n != n)
                throw new AssertionError("This statement is false.");
        }
    }



    public static void main(String[] args) {
        Holder holder = new Holder(0);
    }

}
