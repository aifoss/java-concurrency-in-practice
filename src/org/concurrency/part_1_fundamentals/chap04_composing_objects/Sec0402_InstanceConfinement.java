package org.concurrency.part_1_fundamentals.chap04_composing_objects;

import org.concurrency.Annotation.GuardedBy;
import org.concurrency.Annotation.NotThreadSafe;
import org.concurrency.Annotation.ThreadSafe;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by sofia on 5/26/17.
 */

/**
 * 4.2. Instance Confinement
 *
 * If an object is not thread-safe, several techniques can still let it be used safely in a multithreaded program.
 * You can ensure that it is only accessed from a single thread (thread confinement),
 * or that all access to it is properly guarded by a lock.
 *
 * Encapsulation simplifies making classes thread-safe by promoting "instance confinement", often just called "confinement".
 *
 * When an object is encapsulated within another object,
 * all code paths that have access to the encapsulated object are known and can be therefore be analyzed more easily
 * than if that object were accessible to the entire program.
 *
 * Combining confinement with an appropriate locking discipline can ensure
 * that otherwise non-thread-safe objects are used in a thread-safe manner.
 *
 * *******************************************************************************************
 * Encapsulating data within an object confines access to the data to the object's methods,
 * making it easier to ensure that the data is always accessed with the appropriate lock held.
 * *******************************************************************************************
 *
 * Confined objects must not escape their intended scope.
 * An object may be confined to a class instance (such as a private class member),
 * a lexical scope (such as a local variable),
 * or a thread (such as an object that is passed from method to method within a thread, but not supposed to be shared across threads).
 *
 * PersonSet in Listing 4.2 illustrates how confinement and locking can work together to make a class thread-safe
 * even when its component state variables are not.
 *
 * Instance confinement is one of the easiest ways to build thread-safe classes.
 * It also allows flexibility in the choice of locking strategy.
 * Instance confinement also allows different states to be guarded by different locks.
 *
 * The basic collection classes such as ArrayList and HashMap are not thread-safe,
 * but the class library provides wrapper factory methods (Collections.synchronizedList and friends)
 * so they can be used safely in multithreaded environments.
 *
 * It is still possible to violate confinement by publishing a supposedly confined object.
 * If an object is intended to be confined to a specific scope, then letting it escape from that scope is a bug.
 * Confined objects can also escape by publishing other objects such as iterators or inner class instances
 * that may indirectly publish the confined objects.
 *
 * **********************************************************************************************************************
 * Confinement makes it easier to build thread-safe classes
 * because a class that confines its state can be analyzed for thread safety without having to examine the whole program.
 * **********************************************************************************************************************
 *
 * 4.2.1. The Java Monitor Pattern
 *
 * Following the principle of instance confinement to its logical conclusion leads you to the Java monitor pattern.
 * An object following the Java monitor pattern encapsulates all its mutable states and guards it with the object's own intrinsic lock.
 *
 * Counter in Listing 4.1 shows a typical example of this pattern.
 * It encapsulates one state variable, value, and all access to that state variable is through the methods of Counter, which are all synchronized.
 *
 * The Java monitor pattern is used by many library classes, such as Vector and Hashtable.
 * The primary advantage of the Java monitor pattern is its simplicity.
 *
 * The Java monitor pattern is merely a convention;
 * any lock object could be used to guard an object's state so long as it is used consistently.
 *
 * There are advantages to using a private lock instead of an object's intrinsic lock (or any other publicly accessible lock).
 * Making the lock object private encapsulates the lock so that client code cannot acquire it,
 * whereas a publicly accessible lock allows client code to participate in its synchronization policy.
 * Clients that improperly acquire another object's lock could cause liveness problems,
 * and verifying that a publicly accessible lock is properly used requires examining the entire program rather than a single class.
 *
 * 4.2.2. Example: Tracking Fleet Vehicles
 *
 * Listing 4.4 shows an implementation of the vehicle tracker using the Java monitor pattern
 * that uses MutablePoint in Listing 4.5 for representing the vehicle locations.
 *
 * Even though MutablePoint is not thread-safe, the tracker class is.
 * This implementation maintains thread safety in part by copying mutable data before returning it to the client.
 * This is usually not a performance issue, but could become one if the data is very large.
 * Another consequence of copying the data is that the contents of the returned collection do not change
 * even if those of the underlying connection change.
 * Whether this is good or bad depends on your requirements.
 */
public class Sec0402_InstanceConfinement {

    /**
     * Listing 4.2. Using Confinement to Ensure Thread Safety.
     */
    @ThreadSafe
    public static class PersonSet {
        @GuardedBy("this")
        private final Set<Person> mySet = new HashSet<>();

        public synchronized void addPerson(Person p) {
            mySet.add(p);
        }

        public synchronized boolean containsPerson(Person p) {
            return mySet.contains(p);
        }
    }

    class Person {

    }

    /**
     * 4.2.1. The Java Monitor Pattern
     */

    /**
     * Listing 4.3. Guarding State with a Private Lock.
     */
    public static class PrivateLock {
        private final Object myLock = new Object();
        @GuardedBy("myLock")
        Widget widget;

        void someMethod() {
            synchronized (myLock) {
                // Access or modify the state of widget
            }
        }
    }

    class Widget {

    }

    /**
     * 4.2.2. Example: Tracking Fleet Vehicles
     */

    /**
     * Listing 4.4. Monitor-based Vehicle Tracker Implementation.
     */
    @ThreadSafe
    public static class MonitorVehicleTracker {
        @GuardedBy("this")
        private final Map<String, MutablePoint> locations;

        public MonitorVehicleTracker(Map<String, MutablePoint> locations) {
            this.locations = deepCopy(locations);
        }

        public synchronized Map<String, MutablePoint> getLocations() {
            return deepCopy(locations);
        }

        public synchronized MutablePoint getLocation(String id) {
            MutablePoint loc = locations.get(id);
            return loc == null ? null : new MutablePoint(loc);
        }

        public synchronized void setLocations(String id, int x, int y) {
            MutablePoint loc = locations.get(id);
            if (loc == null)
                throw new IllegalArgumentException("No such ID: " + id);
            loc.x = x;
            loc.y = y;
        }

        private static Map<String, MutablePoint> deepCopy(Map<String, MutablePoint> m) {
            Map<String, MutablePoint> result = new HashMap<>();
            for (String id : m.keySet())
                result.put(id, new MutablePoint(m.get(id)));
            return Collections.unmodifiableMap(result);
        }
    }

    /**
     * Listing 4.5. Mutable Point Class Similar to java.awt.Point.
     */
    @NotThreadSafe
    public static class MutablePoint {
        public int x, y;

        public MutablePoint() {
            x = 0;
            y = 0;
        }

        public MutablePoint(MutablePoint p) {
            this.x = p.x;
            this.y = p.y;
        }
    }



    public static void main(String[] args) {
        PersonSet personSet = new PersonSet();
        PrivateLock privateLock = new PrivateLock();
        MonitorVehicleTracker monitorVehicleTracker = new MonitorVehicleTracker(null);
    }

}
