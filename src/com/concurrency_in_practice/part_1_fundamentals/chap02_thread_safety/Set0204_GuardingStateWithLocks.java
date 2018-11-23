package com.concurrency_in_practice.part_1_fundamentals.chap02_thread_safety;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * 2.4. Guarding State with Locks
 *
 * Because locks enable serialized access to the code paths they guard,
 * we can use them to construct protocols guaranteeing access to shared state.
 * Following these protocols consistently can ensure state consistency.
 *
 * Compound actions on shared state, such as incrementing a hit counter (read-modify-write) or lazy initialization (check-then-act),
 * must be made atomic to avoid race conditions.
 * Holding a lock for the entire duration of a compound action can make that compound action atomic.
 * However, just wrapping the compound action with a synchronized block is not sufficient;
 * if synchronization is used to coordinate access to a variable, it is needed everywhere that variable is accessed.
 * Further, when using locks to coordinate access to a variable, the same lock must be used wherever that variable is accessed.
 *
 * It is a common mistake to assume that synchronization needs to be used only when writing to shared variables;
 * this is simply not true.
 *
 * ********************************************************************************
 * For each mutable state variable that may be accessed by more than one thread,
 * all accesses to that variable must be performed with the same lock held.
 * In this case, we say that the variable is guarded by that lock.
 * ********************************************************************************
 *
 * In SynchronizedFactorizer in Listing 2.6, lastNumber and lastFactors are guarded by the servlet object's intrinsic lock;
 * this is documented by the @GuardedBy annotation.
 *
 * There is no inherent relationship between an object's intrinsic lock and its state;
 * an object's fields need not be guarded by its intrinsic lock, though this is a perfectly valid locking convention.
 * Acquiring the lock associated with an object does not prevent other threads from accessing that object
 * - the only thing that acquiring a lock prevents any other thread from doing is acquiring that same lock.
 * The fact that every object has a built-in lock is just a convenience so that you needn't explicitly create lock objects.
 * It is up to you to construct locking protocols or synchronization policies that let you access shared state safely.
 *
 * ********************************************************************************
 * Every shared, mutable variable should be guarded by exactly one lock.
 * Make it clear to maintainers which lock that is.
 * ********************************************************************************
 *
 * A common locking convention is to encapsulate all mutable state within an object and to protect it from concurrent access
 * by synchronizing any code path that accesses mutable state using the object's intrinsic lock.
 * In such cases, all the variables in an object's state are guarded by the object's intrinsic lock.
 * However, neither the compiler nor the runtime enforces this (or any other) pattern of locking.
 *
 * When a variable is guarded by a lock - meaning that every access to that variable is performed with that lock held -
 * you've ensured that only one thread at a time can access that variable.
 * When a class has invariants that involve more than one state variable, there is an additional requirement:
 * each variable participating in the invariant must be guarded by the same lock.
 * This allows you to access or update them in a single atomic operation, preserving the invariant.
 * SynchronizedFactorizer demonstrates this rule:
 * both the cached number and the cached factors are guarded by the servlet object's intrinsic lock.
 *
 * **************************************************************************************************************
 * For every invariant that involves more than one variable, all the variables involved in that invariant
 * must be guarded by the same lock.
 * **************************************************************************************************************
 *
 * If synchronization is the cure for race conditions, why not just declare every method synchronized?
 * It turns out that such indiscriminate application of synchronization might be either too much or too little synchronization.
 *
 * While synchronized methods can make individual operations atomic, additional locking is required
 * - when multiple operations are combined into a compound action.
 * At the same time, synchronizing every method can lead to liveness or performance problems.
 */
public class Set0204_GuardingStateWithLocks {

    public static void main(String[] args) {

    }

}
