package org.concurrency.part_1_fundamentals.chap02_thread_safety;

import org.concurrency.Annotation.NotThreadSafe;
import org.concurrency.Annotation.ThreadSafe;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * 2.2. Atomicity
 *
 * 2.2.1. Race Conditions
 *
 * A race condition occurs when the correctness of a computation depends on the relative timing or interleaving of multiple threads by the runtime.
 * The most common type of race condition is "check-then-act", where a potentially stale observation is used to make a decision on what to do next.
 *
 * 2.2.2. Example: Race Conditions in Lazy Initialization
 *
 * A common idiom that uses check-then-act is lazy initialization.
 *
 * 2.2.3. Compound Actions
 *
 * To avoid race conditions, there must be a way to prevent other threads from using a variable while we're in the middle of modifying it,
 * so we can ensure that other threads can observe or modify the state only before we start or after we finish, but non in the middle.
 *
 * **********************************************************************************************************************************
 * Operations A and B are atomic with respect to each other if,
 * from the perspective of a thread executing A,
 * when another thread executes B,
 * either all of B has executed or none of it has.
 * An atomic operation is one that is atomic with respect to all operations, including itself, that operate on the same state.
 * **********************************************************************************************************************************
 *
 * To ensure thread safety, check-then-act operations (like lazy initialization) and read-modify-write oeprations (like increment)
 * must always be atomic.
 *
 * We refer collectively to check-then-act and read-modify-write sequences as compound actions:
 * sequences of operations that must be executed atomically in order to remain thread-safe.
 *
 * The java.util.concurrent.atomic package contains atomic variable classes for effecting atomic state transitions on numbers and object references.
 *
 * When a single element of state is added to a stateless class,
 * the resulting class will be thread-safe if the state is entirely managed by a thread-safe object.
 *
 * **********************************************************************************************************************************
 * Where practical, use existing thread-safe objects, like AtomicLong, to manage your class's state.
 * It is simpler to reason about the possible states and state transitions for existing thread-safe objects
 * than it is for arbitrary state variables, and this makes it easier to maintain and verify thread safety.
 * **********************************************************************************************************************************
 */
public class Sec0202_Atomicity {

    /**
     * 2.2.1 Race Conditions
     */

    /**
     * Listing 2.2. Servlet that Counts Requests without the Necessary Synchronization. Don't Do this.
     */
    @NotThreadSafe
    public abstract class UnsafeCountingFactorizer implements Servlet {
        private long count = 0;

        public long getCount() { return count; }

        public void service(ServletRequest req, ServletResponse resp) {
            BigInteger i = extractFromRequest(req);
            BigInteger[] factors = factor(i);
            ++count;    // read-modify-write
            encodeIntoResponse(resp, factors);
        }

        private BigInteger extractFromRequest(ServletRequest req) {
            return null;
        }

        private BigInteger[] factor(BigInteger i) {
            return null;
        }

        private void encodeIntoResponse(ServletResponse resp, BigInteger[] factors) {

        }
    }

    /**
     * 2.2.2. Example: Race Conditions in Lazy Initialization
     */

    /**
     * Listing 2.3. Race Condition in Lazy Initialization. Don't Do this.
     */
    @NotThreadSafe
    public static class LazyInitRace {
        private ExpensiveObject instance = null;

        public ExpensiveObject getInstance() {
            if (instance == null) // check-then-act
                instance = new ExpensiveObject();
            return instance;
        }

        private class ExpensiveObject {

        }
    }

    /**
     * 2.2.3. Compound Actions
     */

    /**
     * Listing 2.4. Servlet that Counts Requests Using AtomicLong.
     */
    @ThreadSafe
    public abstract class CountingFactorizer implements Servlet {

        private final AtomicLong count = new AtomicLong(0);

        public long getCount() { return count.get(); }

        public void service(ServletRequest req, ServletResponse resp) {
            BigInteger i = extractFromRequest(req);
            BigInteger[] factors = factor(i);
            count.incrementAndGet();
            encodeIntoResponse(resp, factors);
        }

        private BigInteger extractFromRequest(ServletRequest req) {
            return null;
        }

        private BigInteger[] factor(BigInteger i) {
            return null;
        }

        private void encodeIntoResponse(ServletResponse resp, BigInteger[] factors) {

        }
    }



    public static void main(String[] args) {
        LazyInitRace lazy = new LazyInitRace();
    }

}
