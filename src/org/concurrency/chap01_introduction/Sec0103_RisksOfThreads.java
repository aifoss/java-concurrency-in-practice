package org.concurrency.chap01_introduction;

import org.concurrency.java_concurrency_in_practice.Annotation.*;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * 1.3 Risks of Threads
 *
 * 1.3.1 Safety Hazards
 *
 * Thread safety can be unexpectedly subtle because, in the absence of sufficient synchronization,
 * the ordering of operations in multiple threads is unpredictable and sometimes surprising.
 *
 * Because threads share the same memory address space and run concurrently,
 * they can access or modify variables that other threads might be using.
 * This is a tremendous convenience, because it makes data sharing much easier than would other inter-thread communication mechanisms.
 * But it is also a significant risk: threads can be confused by having data change unexpectedly.
 * Allowing multiple threads to access and modify the same variables introduces an element of non-sequentiality
 * into an otherwise sequential programming model.
 *
 * For a multithreaded program's behavior to be predictable,
 * access to shared variables must be properly coordinated so that threads do not interfere with one another.
 *
 * 1.3.2 Liveness Hazards
 *
 * While safety means "nothing bad ever happens", liveness concerns the complementary goal that "something good eventually happens".
 * A liveness failure occurs when an activity gets into a state such that it is permanently unable to make forward progress.
 *
 * One form of liveness failure that can occur in sequential programs is an inadvertent infinite loop.
 * The use of threads introduces additional liveness risks.
 * For example, if thread A is waiting for a resource that thread B holds exclusively, and B never releases it, A will wait forever.
 *
 * 1.3.3 Performance Hazards
 *
 * While liveness means that something good eventually happens, eventually may not be good enough - we often want good things to happen quickly.
 *
 * Threads carry some degree of runtime overhead.
 * Context switches are more frequent in applications with many threads, and have significant costs:
 * saving and restoring execution context, loss of locality, and CPU time spent scheduling threads instead of running them.
 * When threads share data, they must use synchronization mechanisms that can inhibit compiler optimizations,
 * flush or invalidate memory caches, and create synchronization traffic on the shared memory bus.
 * All these factors introduce additional performance costs.
 */
public class Sec0103_RisksOfThreads {

    /**
     * 1.3.1 Safety Hazards
     */

    /**
     * Listing 1.1. Non-thread-safe Sequence Generator.
     * Race condition
     */
    @NotThreadSafe
    public static class UnsafeSequence {
        private int value;

        /** Returns a unique value. */
        public int getNext() {
            return value++;
        }
    }

    /**
     * Listing 1.2. Thread-safe Sequence Generator.
     */
    @ThreadSafe
    public static class Sequence {
        @GuardedBy("this")
        private int nextValue;

        public synchronized int getNext() {
            return nextValue++;
        }
    }



    public static void main(String[] args) {
        UnsafeSequence unsafe = new UnsafeSequence();
        Sequence safe = new Sequence();
    }

}
