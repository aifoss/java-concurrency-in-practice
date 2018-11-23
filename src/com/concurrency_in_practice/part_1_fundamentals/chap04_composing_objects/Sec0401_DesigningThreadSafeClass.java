package com.concurrency_in_practice.part_1_fundamentals.chap04_composing_objects;

import com.concurrency_in_practice.common.Annotation.GuardedBy;
import com.concurrency_in_practice.common.Annotation.ThreadSafe;

/**
 * Created by sofia on 5/26/17.
 */

/**
 * 4.1. Designing a Thread-Safe Class
 *
 * Encapsulation makes it possible to determine that a class is thread-safe without having to examine the entire program.
 *
 * **************************************************************************************************************
 * The design process for a thread-safe class should include these 3 basic elements:
 * 1. Identify the variables that form the object's state.
 * 2. Identify the invariants that constrain the state variables.
 * 3. Establish a policy for managing concurrent access to the object's state.
 * **************************************************************************************************************
 *
 * An object's state starts with its fields.
 * If they are all of primitive type, the fields comprise the entire state.
 * The state of an object with n primitive fields is just the n-tuple of its field values.
 * If the object has fields that are references to other objects,
 * its state will encompass fields from the referenced objects as well.
 *
 * The synchronization policy defines how an object coordinates access to its state without violating its variants or post-conditions.
 * It specifies what combination of immutability, thread confinement, and locking is used to maintain thread safety,
 * and which variables are guarded by which locks.
 * To ensure that the class can be analyzed and maintained, document the synchronization policy.
 *
 * 4.1.1. Gathering Synchronization Requirements
 *
 * Constraints placed on states or state transitions by invariants or post-conditions
 * create additional synchronization or encapsulation requirements.
 * If certain states are invalid, then the underlying state variables must be encapsulated,
 * otherwise client code could put the object into an invalid state.
 * If an operation has invalid state transitions, it must be made atomic.
 * On the other hand, if the class does not impose any such constraints,
 * we may be able to relax encapsulation or serialization requirements
 * to obtain greater flexibility or better performance.
 *
 * A class can also have invariants that constrain multiple state variables.
 * Multivariable invariants create atomicity requirements:
 * related variables must be fetched or updated in a single atomic operation.
 * When multiple variables participate in an invariant,
 * the lock that guards them must be held for the duration of any operation that accesses the related variables.
 *
 * *****************************************************************************************************************************
 * You cannot ensure thread safety without understanding an object's invariants and post-conditions.
 * Constraints on the valid values or state transitions for state variables can create atomicity and encapsulation requirements.
 * *****************************************************************************************************************************
 *
 * 4.1.2. State-dependent Operations
 *
 * Some objects have methods with state-based preconditions.
 * Operations with state-based preconditions are called "state-dependent".
 *
 * In a single-threaded program, if a precondition does not hold, the operation has no choice but to fail.
 * But in a concurrent program, the precondition may become true later due to the action of another thread.
 * Concurrent programs add the possibility of waiting until the precondition becomes true,
 * and then proceeding with the operation.
 *
 * The built-in mechanisms for efficiently waiting for a condition to become true - wait and notify - are tightly bound to intrinsic locking,
 * and can be difficult to use correctly.
 * To create operations that wait for a precondition to become true before proceedings,
 * it is often easier to use existing library classes, such as blocking queues or semaphores,
 * to provide the desired state-dependent behavior.
 *
 * 4.1.3. State Ownership
 *
 * When defining which variables form an object's state, we want to consider only the data that object owns.
 * Ownership is not embodied explicitly in the language, but is instead an element of class design.
 *
 * For better or worse, garbage collection lets us avoid thinking carefully about ownership.
 *
 * In many cases, ownership and encapsulation go together - the object encapsulates the state it owns and owns the state it encapsulates.
 * It is the owner of a given state variable that gets to decide on the locking protocol used to maintain the integrity of that variable's state.
 *
 * Ownership implies control, but once you publish a reference to a mutable object, you no longer have exclusive control;
 * at best, you might have "shared ownership".
 *
 * A class usually does not own the objects passed to its methods or constructors,
 * unless the method is designed to explicitly transfer ownership of objects passed in.
 *
 * Collection classes often exhibit a form of "split ownership",
 * in which the collection owns the state of the collection infrastructure,
 * but client code owns the objects stored in the collection.
 */
public class Sec0401_DesigningThreadSafeClass {

    /**
     * Listing 4.1. Simple Thread-Safe Counter using the Java Monitor Pattern.
     */
    @ThreadSafe
    public final class Counter {
        @GuardedBy("this") private long value = 0;

        public synchronized long getValue() {
            return value;
        }

        public synchronized long increment() {
            if (value == Long.MAX_VALUE)
                throw new IllegalStateException("counter overflow");
            return ++value;
        }
    }



    public static void main(String[] args) {

    }

}
