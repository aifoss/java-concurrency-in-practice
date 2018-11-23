package com.concurrency_in_practice.part_1_fundamentals.chap04_composing_objects;

import com.concurrency_in_practice.common.Annotation.GuardedBy;
import com.concurrency_in_practice.common.Annotation.Immutable;
import com.concurrency_in_practice.common.Annotation.ThreadSafe;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sofia on 5/26/17.
 */

/**
 * 4.3. Delegating Thread Safety
 *
 * In some cases a composite made of thread-safe components is thread-safe (Listing 4.7 and 4.9),
 * and in others it is merely a good start (4.10).
 *
 * 4.3.1. Example: Vehicle Tracker Using Delegation
 *
 * Let's construct a version of the vehicle tracker that delegates to a thread-safe class.
 * We store the locations in a Map, so we start with a thread-safe Map implementation, ConcurrentHashMap.
 * we also store the location using an immutable Point class instead of MutablePoint.
 *
 * DelegatingVehicleTracker in Listing 4.7 does not use any explicit synchronization;
 * all access to state is managed by ConcurrentHashMap, and all the keys and values of the Map are immutable.
 * If an unchanging view of the map is required, getLocations could instead return a shallow copy, as shown in Listing 4.8.
 *
 * 4.3.2. Independent State Variables
 *
 * We can delegate thread safety to more than one underlying state variable as long as those underlying state variables are independent,
 * meaning that the composite class does not impose any invariants involving the multiple state variables.
 *
 * VisualComponent in Listing 4.9 delegates its thread safety obligations to two underlying thread-safe lists of listeners.
 * CopyOnWriteArrayList is a thread-safe implementation particularly suited for managing listener lists.
 *
 * 4.3.3. When Delegation Fails
 *
 * Most composite classes have invariants that relate their component state variables.
 *
 * NumberRange in Listing 4.10 is not thread-safe because, even though the underlying AtomicIntegers are thread-safe,
 * the state variables lower and upper are not independent.
 *
 * NumberRange could be made thread-safe by using locking to maintain its invariants,
 * such as guarding lower and upper with a common lock.
 * It must also avoid publishing lower and upper to prevent clients from subverting its invariants.
 *
 * If a class has compound actions, delegation alone is not a suitable approach for thread safety.
 * In these cases, the class must provide its own locking to ensure that compound actions are atomic,
 * unless the entire compound action can also be delegated to the underlying state variables.
 *
 * *****************************************************************************************************************************************
 * If a class is composed of multiple independent thread-safe state variables and has no operations that have any invalid state transitions,
 * then it can delegate thread safety to the underlying state variables.
 * *****************************************************************************************************************************************
 *
 * 4.3.4. Publishing Underlying State Variables
 *
 * When you delegate thread safety to an object's underlying state variables,
 * under what conditions can you publish those variables so that other classes can modify them as well?
 * The answer depends on what invariants your class imposes on those variables.
 *
 * ****************************************************************************************************
 * If a state variable is thread-safe, does not participate in any invariants that constrain its value,
 * and has no prohibited state transitions for any of its operations, then it can safely be published.
 * ****************************************************************************************************
 *
 * 4.3.5. Example: Vehicle Tracker that Publishes Its State
 *
 * Let's construct another version of the vehicle tracker that publishes its underlying mutable state.
 *
 * SafePoint in Listing 4.11 provides a getter that retrieves both the x and y values at once by returning a two-element array.
 * Using SafePoint, we can construct a vehicle tracker that publishes the underlying mutable state without undermining thread safety,
 * as shown in the PublishingVehicleTracker class in Listing 4.12.
 * PublishingVehicleTracker derives its thread safety from delegation to an underlying ConcurrentHashMap,
 * but this time the contents of the Map are thread-safe mutable points rather than immutable ones.
 */
public class Sec0403_DelegatingThreadSafety {

    /**
     * 4.3.1. Example: Vehicle Tracker Using Delegation
     */

    /**
     * Listing 4.6. Immutable Point class used by DelegatingVehicleTracker.
     */
    @Immutable
    public static class Point {
        public final int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Listing 4.7. Delegating Thread Safety to a ConcurrentHashMap.
     */
    @ThreadSafe
    public static class DelegatingVehicleTracker {
        private final ConcurrentHashMap<String, Point> locations;
        private final Map<String, Point> unmodifiableMap;

        public DelegatingVehicleTracker(Map<String, Point> points) {
            locations = new ConcurrentHashMap<>(points);
            unmodifiableMap = Collections.unmodifiableMap(locations);
        }

        public Map<String, Point> getLocations() {
            return unmodifiableMap;
        }

        /**
         * Listing 4.8. Returning a Static Copy of the Location Set instead of a "Live" One.
         */
        public Map<String, Point> getLocations2() {
            return Collections.unmodifiableMap(new HashMap<>(locations));   // Shallow copy
        }

        public Point getLocation(String id) {
            return locations.get(id);
        }

        public void setLocation(String id, int x, int y) {
            if (locations.replace(id, new Point(x, y)) == null) {
                throw new IllegalArgumentException("invalid vehicle name: " + id);
            }
        }
    }

    /**
     * 4.3.2. Independent State Variables
     */

    /**
     * Listing 4.9. Delegating Thread Safety to Multiple Underlying State Variables.
     */
    public static class VisualComponent {
        private final List<KeyListener> keyListeners = new CopyOnWriteArrayList<>();
        private final List<MouseListener> mouseListeners = new CopyOnWriteArrayList<>();

        public void addKeyListener(KeyListener listener) {
            keyListeners.add(listener);
        }

        public void addMouseListener(MouseListener listener) {
            mouseListeners.add(listener);
        }

        public void removeKeyListener(KeyListener listener) {
            keyListeners.remove(listener);
        }

        public void removeMouseListener(MouseListener listener) {
            mouseListeners.remove(listener);
        }
    }

    /**
     * 4.3.3. When Delegation Fails
     */

    /**
     * Listing 4.10. Number Range Class that does Not Sufficiently Protect Its Invariants. Don't Do this.
     */
    public static class NumberRange {
        // INVARIANT: lower <= upper
        private final AtomicInteger lower = new AtomicInteger(0);
        private final AtomicInteger upper = new AtomicInteger(0);

        public void setLower(int i) {
            // Warning -- unsafe check-then-act
            if (i > upper.get())
                throw new IllegalArgumentException("can't set lower to " + i + " > upper");
            lower.set(i);
        }

        public void setUpper(int i) {
            // Warning -- unsafe check-then-act
            if (i < lower.get())
                throw new IllegalArgumentException("can't set upper to " + i + " < lower");
            upper.set(i);
        }

        public boolean isInRange(int i) {
            return (i >= lower.get() && i <= upper.get());
        }
    }

    /**
     * 4.3.5. Example: Vehicle Tracker that Publishes Its State
     */

    /**
     * Listing 4.11. Thread-safe Mutable Point Class.
     */
    @ThreadSafe
    public static class SafePoint {
        @GuardedBy("this") private int x, y;

        public SafePoint(SafePoint p) {
            this(p.get());
        }

        private SafePoint(int[] a) {
            this(a[0], a[1]);
        }

        public SafePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public synchronized int[] get() {
            return new int[] { x, y };
        }

        public synchronized void set(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Listing 4.12. Vehicle Tracker that Safely Publishes Underlying State.
     */
    @ThreadSafe
    public static class PublishingVehicleTracker {
        private final Map<String, SafePoint> locations;
        private final Map<String, SafePoint> unmodifiableMap;

        public PublishingVehicleTracker(Map<String, SafePoint> locations) {
            this.locations = new ConcurrentHashMap<>(locations);
            this.unmodifiableMap = Collections.unmodifiableMap(this.locations);
        }

        public Map<String, SafePoint> getLocations() {
            return unmodifiableMap;
        }

        public SafePoint getLocation(String id) {
            return locations.get(id);
        }

        public void setLocation(String id, int x, int y) {
            if (!locations.containsKey(id))
                throw new IllegalArgumentException("invalid vehicle name: " + id);
            locations.get(id).set(x, y);
        }
    }



    public static void main(String[] args) {
        DelegatingVehicleTracker delegatingVehicleTracker = new DelegatingVehicleTracker(null);
        VisualComponent visualComponent = new VisualComponent();
        NumberRange numberRange = new NumberRange();
        SafePoint safePoint = new SafePoint(0, 0);
        PublishingVehicleTracker publishingVehicleTracker = new PublishingVehicleTracker(null);
    }

}
