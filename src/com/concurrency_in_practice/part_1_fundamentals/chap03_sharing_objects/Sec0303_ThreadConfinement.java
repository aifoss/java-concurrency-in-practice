package com.concurrency_in_practice.part_1_fundamentals.chap03_sharing_objects;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * 3.3. Thread Confinement
 *
 * Accessing shared, mutable data requires using synchronization; one way to avoid this requirement is to not share.
 * If data is only accessed from a single thread, no synchronization is needed.
 * This technique, thread confinement, is one of the simplest ways to achieve thread safety.
 * When an object is confined to a thread, such usage is automatically thread-safe even if the confined object itself is not.
 *
 * Swing uses thread confinement extensively.
 * Another common application of thread confinement is the use of pooled JDBC Connection objects.
 *
 * Just as the language has no mechanism for enforcing that a variable is guarded by a lock,
 * it has no means of confining an object to a thread.
 * Thread confinement is an element of your program's design that must be enforced by its implementation.
 *
 * 3.3.1 Ad-hoc Thread Confinement
 *
 * Ad-hoc thread confinement describes when the responsibility for maintaining thread confinement falls entirely on the implementation.
 *
 * Ad-hoc thread confinement can be fragile because none of the language features, such as visibility modifiers or local variables,
 * helps confine the object to the target thread.
 *
 * The decision to use thread confinement is often a consequence of the decision to implement a particular subsystem,
 * such as the GUI, as a single-thread subsystem.
 * Single-thread subsystems can sometimes offer a simplicity benefit that outweighs the fragility of ad-hoc thread confinement.
 *
 * A special case of thread confinement applies to volatile variables.
 * It is safe to perform read-modify-write operations on shared volatile variables
 * as long as you ensure that the volatile variable is only written from a single thread.
 * In this case, you are confining the modification to a single thread to prevent race conditions,
 * and the visibility guarantees for volatile variables ensure that other threads see the most up-to-date value.
 *
 * Because of its fragility, ad-hoc thread confinement should be used sparingly;
 * if possible, use one of the stronger forms of thread confinement (stack confinement or ThreadLocal) instead.
 *
 * 3.3.2. Stack Confinement
 *
 * Stack confinement is a special case of thread confinement in which an object can only be reached through local variables.
 *
 * Just as encapsulation can make it easier to preserve invariants, local variables can make it easier to confine objects to a thread.
 * Local variables are intrinsically confined to the executing thread;
 * they exist on the executing thread's stack, which is not accessible to other threads.
 *
 * Stack confinement (also called within-thread or thread-local usage, but not to be confused with the ThreadLocal library class)
 * is simpler to maintain and less fragile than ad-hoc thread confinement.
 *
 * For primitively typed local variables, you cannot violate stack confinement even if you tried.
 * There is no way to obtain a reference to a primitive variable,
 * so the language semantics ensures that primitive local variables are always stack-confined.
 *
 * Maintaining stack confinement for object references requires a little more assistance from the programmer
 * to ensure that the referent does not escape.
 *
 * Using a non-thread-safe object in a within-thread context is still thread-safe.
 * However, be careful: the design requirement that the object be confined to the executing thread,
 * or the awareness that the confined object is not thread-safe,
 * often exists only in the head of the developer when the code is written.
 * If the assumption of within-thread usage is not clearly documented, future maintainers might mistakenly allow the objects to escape.
 *
 * 3.3.3. ThreadLocal
 *
 * A more formal means of maintaining thread confinement is ThreadLocal,
 * which allows you to associate a per-thread value with a value-holding object.
 * ThreadLocal provides get and set accessor methods that maintain a separate copy of the value for each thread that uses it,
 * so a get returns the most recent value passed to set from the currently executing thread.
 *
 * Thread-local variables are often used to prevent sharing in designs based on mutable Singletons or global variables.
 * For example, a single-threaded application might maintain a global database connection that is initialized at startup
 * to avoid having to pass a Connection to every method.
 * By using a ThreadLocal to store the JDBC connection, each thread will have its own connection.
 *
 * This technique can also be used when a frequently used operation requires a temporary object such as a buffer
 * and wants to avoid reallocating the temporary object on each invocation.
 *
 * When a thread calls ThreadLocal.get for the first time, initialValue is consulted to provide the initial value for that thread.
 * Conceptually, you can think of a ThreadLocal<T> as holding a Map<Thread, T> that stores the thread-specific values,
 * though this is not how it is actually implemented.
 * The thread-specific values are stored in the Thread object itself;
 * when the thread terminates, the thread-specific values can be garbage collected.
 *
 * If you are porting a single-thread application to a multithreaded environment,
 * you can preserve thread safety by converting shared global variables into ThreadLocals,
 * if the semantics of the shared globals permits this;
 * an application-wide cache would not be as useful if it were turned into a number of thread-local caches.
 *
 * ThreadLocal is widely used in implementing application frameworks.
 *
 * It is easy to abuse ThreadLocal by treating its thread confinement property as a license to use global variables
 * or as a means of creating "hidden" method arguments.
 * Like global variables, thread-local variables can detract from reusability and introduce hidden couplings among classes,
 * and should therefore be used with care.
 */
public class Sec0303_ThreadConfinement {

    /**
     * 3.3.2. Stack Confinement
     */

    /**
     * Listing 3.9. Thread Confinement of Local Primitives and Reference Variables.
     */
    static Ark ark = new Ark();

    public static int loadTheArk(Collection<Animal> candidates) {
        SortedSet<Animal> animals;
        int numPairs = 0;
        Animal candidate = null;

        // animals confined to method, don't let them escape!
        animals = new TreeSet<>(new SpeciesGenderComparator());
        animals.addAll(candidates);

        for (Animal a : animals) {
            if (candidate == null || !candidate.isPotentialMate(a))
                candidate = a;
            else {
                ark.load(new AnimalPair(candidate, a));
                ++numPairs;
                candidate = null;
            }
        }

        return numPairs;
    }

    static class Animal {
        public boolean isPotentialMate(Animal other) {
            return false;
        }
    }

    static class AnimalPair {
        public AnimalPair(Animal a, Animal b) {

        }
    }

    static class SpeciesGenderComparator implements Comparator<Animal> {
        @Override
        public int compare(Animal a1, Animal a2) {
            return 0;
        }
    }

    static class Ark {
        public void load(AnimalPair pair) {}
    }

    /**
     * 3.3.3. ThreadLocal
     */

    /**
     * Listing 3.10. Using ThreadLocal to Ensure Thread Confinement.
     */
    private static ThreadLocal<Connection> connectionHolder = new ThreadLocal<Connection>() {
        public Connection initialValue() {
            try {
                return DriverManager.getConnection(DB_URL);
            } catch (SQLException e) {

            }
            return null;
        }
    };

    public static Connection getConnection() {
        return connectionHolder.get();
    }

    private static final String DB_URL = "...";



    public static void main(String[] args) {
        loadTheArk(null);
        getConnection();
    }

}
