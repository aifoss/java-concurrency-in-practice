package org.concurrency.part_1_fundamentals.chap03_sharing_objects;

import org.concurrency.Annotation.*;

import java.awt.*;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * 3.2. Publication and Escape
 *
 * Publishing an object means making it available to code outside of its current scope,
 * such as by storing a reference to it where other code can find it,
 * returning it from a non-private method, or passing it to a method in another class.
 * In many situations, we want to ensure that objects and their internals are not published.
 * In other situations, we do want to publish an object for general use,
 * but doing so in a thread-safe manner may require synchronization.
 * Publishing internal state variables can compromise encapsulation and make it more difficult to preserve invariants;
 * publishing objects before they are fully constructed can compromise thread safety.
 * An object that is published when it should not have been is said to have escaped.
 *
 * The most blatant form of publication is to store a reference in a public static field, where any class and thread could see it.
 *
 * Publishing one object may indirectly publish others.
 *
 * Returning a reference from a non-private method also publishes the returned object.
 *
 * Publishing an object also publishes any objects referenced by its non-private fields.
 * More generally, any object that is reachable from a published object by following some chain of non-private field references and method calls
 * has also been published.
 *
 * From the perspective of a class C, an alien method is one whose behavior is not fully specified by C.
 * This includes methods in other classes as well as overrideable methods (neither private nor final) in C itself.
 * Passing an object to an alien method must also be considered publishing that object.
 *
 * Once an object escapes, you have to assume that another class or thread may, maliciously or carelessly, misuse it.
 * This is a compelling reason to use encapsulation:
 * it makes it practical to analyze programs for correctness and harder to violate design constraints accidentally.
 *
 * A final mechanism by which an object or its internal state can be published is
 * by inner class instances containing a hidden reference to the enclosing instance.
 *
 * 3.2.1. Safe Construction Practices
 *
 * ThisEscape in Listing 3.7 illustrates an important special case of escape - when the "this" reference escapes during construction.
 * When the inner EventListener instance is published, so is the enclosing ThisEscape instance.
 * But an object is in a predictable, consistent state only after its constructor returns,
 * so publishing an object from within its constructor can publish an incompletely constructed object.
 * This is true even if the publication is the last statement in the constructor.
 * If the "this" reference escapes during construction, the object is considered not properly constructed.
 *
 * *****************************************************************
 * Do not allow the "this" reference to escape during construction.
 * *****************************************************************
 *
 * A common mistake that can let the "this" reference escape during construction is to start a thread from a constructor.
 * When an object creates a thread from its constructor, it almost always shares its "this" reference with the new thread,
 * either explicitly (by passing it to the constructor) or implicitly (because the Thread or Runnable is an inner class of the owning object).
 * The new thread might then be able to see the owning object before it is fully constructed.
 * There's nothing wrong with creating a thread in a constructor, but it is best not to start the thread immediately.
 * Instead, expose a "start" or "initialize" method that starts the owned thread.
 *
 * Calling an overrideable instance method (one that is neither private nor final) from the constructor
 * can also allow the "this" reference to escape.
 *
 * If you are tempted to register an event listener or start a thread from a constructor,
 * you can avoid the improper construction by using a private constructor and a public factory method,
 * as shown in SafeListener in Listing 3.8.
 */
public class Sec0302_PublicationAndEscape {

    /**
     * Listing 3.5. Publishing an Object
     */
    public static Set<Secret> knownSecrets;

    @NotThreadSafe
    public static void initialize() {
        knownSecrets = new HashSet<>();
    }

    static class Secret {

    }

    /**
     * Listing 3.6. Allowing Internal Mutable State to Escape. Don't Do this.
     */
    @NotThreadSafe
    public static class UnsafeStates {
        private String[] states = new String[] { "AK", "AL" };

        public String[] getStates() { return states; }
    }

    /**
     * Listing 3.7. Implicitly Allowing the "this" Reference to Escape. Don't Do this.
     */
    @NotThreadSafe
    public static class ThisEscape {
        public ThisEscape(EventSource source) {
            source.registerListener(new EventListener() {
                public void onEvent(Event e) {
                    doSomething(e);
                }
            });
        }

        private void doSomething(Event e) {}
    }

    static class EventSource {
        public void registerListener(EventListener e) {}
    }

    /**
     * 3.2.1. Safe Construction Practices
     */

    /**
     * Listing 3.8. Using a Factory Method to Prevent the "this" Reference from Escaping during Construction.
     */
    @ThreadSafe
    public static class SafeListener {
        private final EventListener listener;

        private SafeListener() {
            listener = new EventListener() {
                public void onEvent(Event e) {
                    doSomething(e);
                }
            };
        }

        private void doSomething(Event e) {}
    }

    public static SafeListener newInstance(EventSource source) {
        SafeListener safe = new SafeListener();
        source.registerListener(safe.listener);
        return safe;
    }



    public static void main(String[] args) {
        initialize();
        UnsafeStates unsafeStates = new UnsafeStates();
        ThisEscape escape = new ThisEscape(null);
        SafeListener safeListener = new SafeListener();
        safeListener = newInstance(null);
    }

}
