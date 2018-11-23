package org.concurrency.part_1_fundamentals.chap03_sharing_objects;

import org.concurrency.Annotation.*;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * 3.1. Visibility
 *
 * In general, there is no guarantee that the reading thread will see a value written by another thread on a timely basis, or even at all.
 * In order to ensure visibility of memory writes across threads, you must use synchronization.
 *
 * NoVisibility in Listing 3.1 illustrates what can go wrong when threads share data without synchronization.
 * Because it does not use adequate synchronization, there is no guarantee that the values of ready and number written by the main thread
 * will be visible to the reader thread.
 * There is no guarantee that operations in one thread will be performed in the order given by the program,
 * as long as the reordering is not detectable from within that thread - even if the reordering is apparent to other threads.
 *
 * ************************************************************************************************************************
 * In the absence of synchronization, the compiler, processor, and runtime can do some downright weird things to the order
 * in which operations appear to execute. Attempts to reason about the order in which memory actions "must" happen
 * in insufficiently synchronized multithreaded programs will almost certainly be incorrect.
 * ************************************************************************************************************************
 *
 * Fortunately, there's an easy way to avoid these complex issues:
 * always use the proper synchronization whenever data is shared across threads.
 *
 * 3.1.1. Stale Data
 *
 * Unless synchronization is used every time a variable is accessed, it is possible to see a state value for that variable.
 * Worse, staleness is not all-or-nothing:
 * a thread can see an up-to-date value of one variable but a stale value of another variable that was written first.
 *
 * Stale values can cause serious safety or liveness failures.
 * Things can get even more complicated with stale values of object references.
 * Stale data can cause serious and confusing failures such as unexpected exceptions, corrupted data structures,
 * inaccurate computations, and infinite loops.
 *
 * MutableInteger in Listing 3.2 is not thread-safe because the value field is accessed from both get and set without synchronization.
 * Among other hazards, it is susceptible to stale values:
 * if one thread calls set, other threads calling get may or may not see that update.
 *
 * We can make MutableInteger thread-safe by synchronizing the getter and setter as shown in SynchronizedInteger in Listing 3.3.
 *
 * 3.1.2. Non-atomic 64-bit Operations
 *
 * When a thread reads a variable without synchronization, it may see a stale value,
 * but at least it sees a value that was actually placed by some thread rather than some random value.
 * This safety guarantee is called "out-of-thin-air safety".
 *
 * Out-of-thin-air safety applies to all variables, with one exception:
 * 64-bit numeric variables (double and long) that are not declared volatile.
 *
 * The Java Memory Model requires fetch and store operations to be atomic,
 * but for nonvolatile long and double variables, the JVM is permitted to treat a 64-bit read or write as two separate 32-bit operations.
 * If the reads and writes occur in different threads, it is therefore possible to read a nonvolatile long
 * and get back the high 32 bits of one value and the low 32 bits of another.
 *
 * Thus, even if you don't care about stale values, it is not safe to use shared mutable long and double variables in multithreaded programs
 * unless they are declared volatile or guarded by a lock.
 *
 * 3.1.3. Locking and Visibility
 *
 * Intrinsic locking can be used to guarantee that one thread sees the effects of another in a predictable manner.
 *
 * When thread A executes a synchronized block, and subsequently thread B enters a synchronized block guarded by the same lock,
 * the values of variables that were visible to A prior to releasing the lock are guaranteed to be visible to B upon acquiring the lock.
 * In other words, everything A did in prior to a synchronized block is visible to B when it executes a synchronized block guarded by the same lock.
 * Without synchronization, there is no such guarantee.
 *
 * We can now give the other reason for the rule requiring all threads to synchronize one the same lock when accessing a shared mutable variable
 * - to guarantee that values written by one thread are made visible to other threads.
 * Otherwise, if a thread reads a variable without holding the appropriate lock, it might see a stale value.
 *
 * ********************************************************************************
 * Locking is not just about mutual exclusion; it is also about memory visibility.
 * To ensure that all threads see the most up-to-date values of shared variables,
 * the reading and writing threads must synchronize on a common lock.
 * ********************************************************************************
 *
 * 3.1.4. Volatile Variables
 *
 * The Java language also provides an alternative, weaker form of synchronization, volatile variables,
 * to ensure that updates to a variable are propagated predictably to other threads.
 *
 * When a field is declared volatile, the compiler and runtime are put on notice
 * that this variable is shared and that operations on it should not be reordered with other memory operations.
 * Volatile variables are not cached in registers or in caches where they are hidden from other processors,
 * so a read of a volatile variable always returns the most recent write by any thread.
 * Yet accessing a volatile variable performs no locking and so cannot cause the executing thread to block,
 * making volatile variables a lighter-weight synchronization mechanism than synchronized.
 *
 * The visibility effects of volatile variables extend beyond the value of the volatile variable itself.
 *
 * When thread A writes to a volatile variable and subsequently thread B reads that same variable,
 * the values of all variables that were visible to A prior to writing to the volatile variable
 * becomes visible to B after reading the volatile variable.
 * So, from a memory visibility perspective, writing a volatile variable is like exiting a synchronized block
 * and reading a volatile variable is like entering a synchronized block.
 *
 * However, we do not recommend relying too heavily on volatile variables for visibility;
 * code that relies on volatile variables for visibility of arbitrary state is more fragile and harder to understand
 * than code that uses locking.
 *
 * ***********************************************************************************************************************
 * Use volatile variables only when they simplify implementing and verifying your synchronization policy;
 * avoid using volatile variables when verifying correctness would require subtle reasoning about visibility.
 * Good uses of volatile variables include ensuring the visibility of their own state, that of the object they refer to,
 * or indicating that an important lifecycle event (such as initialization or shutdown) has occurred.
 * ***********************************************************************************************************************
 *
 * Listing 3.4 illustrates a typical use of volatile variables: checking a status flag to determine when to exit a loop.
 *
 *      Listing 3.4. Counting Sheep.
 *
 *      volatile boolean asleep;
 *      ...
 *      while (!asleep)
 *          countSomeSheep();
 *
 * The most common use for volatile variables is as a completion, interruption, or status flag.
 * Volatile variables can be used for other kinds of state information, but more care is required when attempting this.
 *
 * *******************************************************************************************************
 * Locking can guarantee both visibility and atomicity; volatile variables can only guarantee visibility.
 * *******************************************************************************************************
 *
 * You can use volatile variables only when all the following criteria are met:
 *
 * - Write to the variable do not depend on its current value, or you can ensure that only a single thread ever updates the value
 * - The variable does not participate in invariants with other state variables
 * - Locking is not required for any other reason while the variable is being accessed
 */
public class Sec0301_Visibility {

    /**
     * Listing 3.1. Sharing Variables without Synchronization. Don't Do this.
     */
    @NotThreadSafe
    public static class NoVisibility {
        private static boolean ready;
        private static int number;

        private static class ReaderThread extends Thread {
            public void run() {
                while (!ready)
                    Thread.yield();
                System.out.println(number);
            }
        }

        public static void main(String[] args) {
            new ReaderThread().start();
            number = 42;
            ready = true;
        }
    }

    /**
     * 3.1.1. Stale Data
     */

    /**
     * Listing 3.2. Non-thread-safe Mutable Integer Holder.
     */
    @NotThreadSafe
    public static class MutableInteger {
        private int value;

        public int get() { return value; }
        public void set(int value) { this.value = value; }
    }

    /**
     * Listing 3.3. Thread-safe Mutable Integer Holder.
     */
    @ThreadSafe
    public static class SynchronizedInteger {
        @GuardedBy("this") private int value;

        public synchronized int get() { return value; }
        public synchronized void set(int value) { this.value = value; }
    }



    public static void main(String[] args) {
        MutableInteger mutable = new MutableInteger();
        SynchronizedInteger synchronzied = new SynchronizedInteger();
    }

}
