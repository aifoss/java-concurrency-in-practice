package org.concurrency.part_1_fundamentals.chap02_thread_safety;

import org.concurrency.Annotation.*;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * 2.3. Locking
 *
 * When multiple variables participate in an invariant, they are not independent:
 * the value of one constrains the allowed value(s) of the others.
 * Thus, when updating one, you must update the others in the same atomic operation.
 *
 * ****************************************************************************************************
 * To preserve state consistency, update related state variables in a single atomic operation.
 * ****************************************************************************************************
 *
 * 2.3.1. Intrinsic Locks
 *
 * Java provides a built-in locking mechanism for enforcing atomicity: the synchronized block.
 *
 * A synchronized block has two parts: a reference to an object that will serve as the lock, and a block of code to be guarded by that lock.
 *
 *      synchronized (lock) {
 *          // Access or modify shared state guarded by lock
 *      }
 *
 * Every Java objects can implicitly act as a lock for purposes of synchronization;
 * these built-in locks are called "intrinsic locks" or "monitor locks".
 *
 * The lock is automatically acquired by the executing thread before entering a synchronized block
 * and automatically released when control exits the synchronized block,
 * whether by the normal control path or by throwing an exception out of the block.
 * The only way to acquire an intrinsic lock is to enter a synchronized block or method guarded by that lock.
 *
 * Intrinsic locks in Java act as mutexes (or mutual exclusion locks), which means at most one thread may own the lock.
 * When thread A attempts to acquire a lock held by thread B, A must wait, or block, until B releases it.
 * If B never releases the lock, A waits forever.
 *
 * 2.3.2. Reentrancy
 *
 * When a thread requests a lock that is already held by another thread, the requesting thread blocks.
 * But because intrinsic locks are reentrant, if a thread tries to acquire a lock that it already holds, the request succeeds.
 * Reentrancy means that locks are acquired on a per-thread rather than per-invocation basis.
 * Reentrancy is implemented by associating with each lock an acquisition count and an owning thread.
 * When the count is zero, the lock is considered unheld.
 * When a thread acquires a previously unheld lock, the JVM records the owner and sets the acquisition count to one.
 * If that same thread acquires the lock again, the count is incremented,
 * and when the owning thread exits the synchronized block, the count is decremented.
 * When the count reaches zero, the lock is released.
 *
 * Reentrancy facilitates encapsulation of locking behavior, and thus simplifies the development of object-oriented concurrent code.
 * Without reentrant locks, the code in Listing 2.7, in which a subclass overrides a synchronized method and then calls the superclass method,
 * would deadlock.
 */
public class Sec0203_Locking {

    /**
     * 2.3.1. Intrinsic Locks
     */

    /**
     * Listing 2.5. Servlet that Attempts to Cache its Last Result without Adequate Atomicity. Don't Do this.
     */
    @NotThreadSafe
    public abstract class UnsafeCachingFactorizer implements Servlet {

        private final AtomicReference<BigInteger> lastNumber = new AtomicReference<>();
        private final AtomicReference<BigInteger[]> lastFactors = new AtomicReference<>();

        public void service(ServletRequest req, ServletResponse resp) {
            BigInteger i = extractFromRequest(req);

            if (i.equals(lastNumber.get())) {
                encodeIntoResponse(resp, lastFactors.get());
            } else {
                BigInteger[] factors = factor(i);
                lastNumber.set(i);
                lastFactors.set(factors);
                encodeIntoResponse(resp, factors);
            }
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
     * Listing 2.6. Servlet that Caches Last Result, But with Unacceptable Poor Concurrency. Don't Do this.
     */
    @ThreadSafe
    public abstract class SynchronizedFactorizer implements Servlet {
        @GuardedBy("this")
        private BigInteger lastNumber;
        @GuardedBy("this")
        private BigInteger[] lastFactors;

        public synchronized void service(ServletRequest req, ServletResponse resp) {
            BigInteger i = extractFromRequest(req);

            if (i.equals(lastNumber))
                encodeIntoResponse(resp, lastFactors);
            else {
                BigInteger[] factors = factor(i);
                lastNumber = i;
                lastFactors = factors;
                encodeIntoResponse(resp, factors);
            }
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
     * 2.3.2. Reentrancy
     */

    /**
     * Listing 2.7. Code that would Deadlock if Intrinsic Locks were Not Reentrant.
     */
    public static class Widget {
        public synchronized void doSomething() {

        }
    }

    public static class LoggingWidget extends Widget {
        public synchronized void doSomething() {
            System.out.println(toString() + ": calling doSomething");
            super.doSomething();
        }
    }



    public static void main(String[] args) {
        LoggingWidget widget = new LoggingWidget();
    }

}
