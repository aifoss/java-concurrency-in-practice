package com.concurrency_in_practice.part_1_fundamentals.chap05_building_blocks;

import com.concurrency_in_practice.common.Annotation.GuardedBy;
import com.concurrency_in_practice.common.Annotation.ThreadSafe;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static com.concurrency_in_practice.part_1_fundamentals.chap05_building_blocks.Sec0505_Synchronizers.launderThrowable;

/**
 * Created by sofia on 5/27/17.
 */

/**
 * 5.6. Building an Efficient, Scalable Result Cache
 *
 * A naive cache implementation is likely to turn a performance bottleneck into a scalability bottleneck,
 * even if it does improve single-threaded performance.
 *
 * In this section we develop an efficient and scalable result cache for a computationally expensive function.
 *
 * The Computable<A, V> interface in Listing 5.16 describes a function with input of type A and result of type V.
 * ExpensiveFunction, which implements Computable, takes a long time to compute its result;
 * we'd like to create a Computable wrapper that remembers the results of previous computations and encapsulates the caching process.
 * This technique is known as "memorization".
 *
 * Memorizer1 in Listing 5.16 shows a first attempt: using a HashMap to store the results of previous computations.
 * HashMap is not thread-safe, so to ensure that two threads do not access the HashMap at the same time,
 * Memorizer1 takes the conservative approach of synchronizing the entire compute method.
 * This ensures thread safety but has an obvious scalability problem: only one thread at a time can execute compute at all.
 *
 * Memorizer2 in Listing 5.17 improves on the awful concurrent behavior of Memorizer1 by replacing the HashMap with a ConcurrentHashMap.
 * Since ConcurrentHashMap is thread-safe, there is no need to synchronize when accessing the backing Map,
 * thus eliminating the serialization induced by synchronizing compute in Memorizer1.
 *
 * Memorizer2 certainly has better concurrent behavior than Memorizer1: multiple threads can actually use it concurrently.
 * But it still has some defects as a cache - there is a window of vulnerability in which two threads calling compute at the same time
 * could end up computing the same value.
 * In the case of memorization, this is merely inefficient
 * - the purpose of a cache is to prevent the same data from being calculated multiple times.
 * For a more general-purpose caching mechanism, it is far worse;
 * for an object cache that is supposed to provide once-and-only-once initialization, this vulnerability would also pose a safety risk.
 *
 * The problem with Memorizer2 is that if one thread starts an expensive computation,
 * other threads are not aware that the computation is in progress and so may start the same computation.
 *
 * We'd like to somehow represent the notion that "thread X is currently computing f(27)",
 * so that, if another thread arrives looking for f(27), it knows that the most efficient way to find it is to head over to thread X's house,
 * hang out there until X is finished, and then ask "Hey, what did you get for f(27)?".
 *
 * We've already seen a class that does almost exactly this: FutureTask.
 * FutureTask represents a computational process that may or may not already have completed.
 * FutureTask.get returns the result of the computation immediately if it is available;
 * otherwise it blocks until the result has been computed and then returns it.
 *
 * Memorizer3 in Listing 5.18 redefines the backing Map for the value cache as a ConcurrentHashMap<A, Future<V>>
 * instead of a ConcurrentHashMap<A, V>.
 *
 * Memorizer3 first checks to see if the appropriate calculation has been started (as opposed to finished, as in Memorizer2).
 * If not, it creates a FutureTask, register it in the Map, and starts the computation;
 * otherwise it waits for the result of the existing computation.
 * The result might be available immediately or might be in the process of being computed
 * - but this is transparent to the caller of Future.get.
 *
 * The Memorizer3 implementation is almost perfect:
 * it exhibits very good concurrency, the result is returned efficiently if it is already known,
 * and if the computation is in progress by another thread, newly arriving threads wait patiently for the result.
 *
 * It has only one defect: there is still a small window of vulnerability in which two threads might compute the same value.
 * Because the if block in compute is still a non-atomic check-then-act sequence,
 * it is possible for two threads to call compute with the same value at roughly the same time,
 * both see that the cache does not contain the desired value, and both start the computation.
 *
 * Memorizer3 is vulnerable to this problem because a compound action (put-if-absent) is performed on the backing map
 * that cannot be made atomic using locking.
 * Memorizer in Listing 5.19 takes advantage of the atomic putIfAbsent method of ConcurrentMap,
 * closing the window of vulnerability in Memorizer3.
 *
 * Caching a Future instead of a value creates the possibility of cache pollution:
 * if a computation is cancelled or fails, future attempts to compute the result will also indcate cancellation or failure.
 * To avoid this, Memorizer removes the Future from the cache if it detects that the computation was cancelled;
 * it might also be desirable to remove the Future upon detecting a RuntimException if the computation might succeed on a future attempt.
 *
 * Memorizer also does not address cache expiration, but this could be accomplished by using a subclass of FutureTask
 * that associates an expiration time with each result and periodically scanning the cache for expired entries.
 * (Similarly, it does not address cache eviction, where old entries are removed to make room for new ones
 * so that the cache does not consume too much memory.)
 *
 * With our concurrent cache implementation complete, we can now add real caching to the factorizing servlet from Chapter 2.
 * Factorizer in Listing 5.20 uses Memorizer to cache previously computed values efficiently and scalably.
 */
public class Sec0506_BuildingResultCache {

    /**
     * Listing 5.16. Initial Cache Attempt Using HashMap and Synchronization.
     */
    public interface Computable<A, V> {
        V compute(A arg) throws InterruptedException;
    }

    public static class ExpensiveFunction implements Computable<String, BigInteger> {
        public BigInteger compute(String arg) {
            // after deep thought...
            return new BigInteger(arg);
        }
    }

    public static class Memorizer1<A, V> implements Computable<A, V> {

        @GuardedBy("this")
        private final Map<A, V> cache = new HashMap<>();
        private final Computable<A, V> c;

        public Memorizer1(Computable<A, V> c) {
            this.c = c;
        }

        public synchronized V compute(A arg) throws InterruptedException {
            V result = cache.get(arg);
            if (result == null) {
                result = c.compute(arg);
                cache.put(arg, result);
            }
            return result;
        }
    }

    /**
     * Listing 5.17. Replacing HashMap with ConcurrentHashMap.
     */
    public static class Memorizer2<A, V> implements Computable<A, V> {

        private final Map<A, V> cache = new ConcurrentHashMap<>();
        private final Computable<A, V> c;

        public Memorizer2(Computable<A, V> c) {
            this.c = c;
        }

        public V compute(A arg) throws InterruptedException {
            V result = cache.get(arg);
            if (result == null) {
                result = c.compute(arg);
                cache.put(arg, result);
            }
            return result;
        }
    }

    /**
     * Listing 5.18. Memorizing Wrapper Using FutureTask.
     */
    public static class Memorizer3<A, V> implements Computable<A, V> {

        private final Map<A, Future<V>> cache = new ConcurrentHashMap<>();
        private final Computable<A, V> c;

        public Memorizer3(Computable<A, V> c) {
            this.c = c;
        }

        public V compute(final A arg) throws InterruptedException {
            Future<V> f = cache.get(arg);

            if (f == null) {
                Callable<V> eval = new Callable<V>() {
                    public V call() throws InterruptedException {
                        return c.compute(arg);
                    }
                };
                FutureTask<V> ft = new FutureTask<>(eval);
                f = ft;
                cache.put(arg, ft);
                ft.run(); // call to c.compute happens here
            }

            try {
                return f.get();
            } catch (ExecutionException e) {
                throw launderThrowable(e.getCause());
            }
        }
    }

    /**
     * Listing 5.19. Final Implementation of Memorizer.
     */
    public static class Memorizer<A, V> implements Computable<A, V> {

        private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<>();
        private final Computable<A, V> c;

        public Memorizer(Computable<A, V> c) {
            this.c = c;
        }

        public V compute(final A arg) throws InterruptedException {
            while (true) {
                Future<V> f = cache.get(arg);

                if (f == null) {
                    Callable<V> eval = new Callable<V>() {
                        public V call() throws InterruptedException {
                            return c.compute(arg);
                        }
                    };

                    FutureTask<V> ft = new FutureTask<>(eval);
                    f = cache.putIfAbsent(arg, ft);

                    if (f == null) {
                        f = ft;
                        ft.run();
                    }
                }

                try {
                    return f.get();
                } catch (CancellationException e) {
                    cache.remove(arg, f);
                } catch (ExecutionException e) {
                    throw launderThrowable(e.getCause());
                }
            }
        }
    }

    /**
     * Listing 5.20. Factorizing Servlet that Caches Results Using Memorizer.
     */
    @ThreadSafe
    public abstract static class Factorizer implements Servlet {

        private final Computable<BigInteger, BigInteger[]> c = new Computable<BigInteger, BigInteger[]>() {
            public BigInteger[] compute(BigInteger arg) {
                return factor(arg);
            }
        };

        private final Computable<BigInteger, BigInteger[]> cache =
                new Memorizer<>(c);

        public void service(ServletRequest req, ServletResponse resp) {
            try {
                BigInteger i = extractFromRequest(req);
                encodeIntoResponse(resp, cache.compute(i));
            } catch (InterruptedException e) {
                encodeError(resp, "factorization interrupted");
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

        private void encodeError(ServletResponse resp, String errorMessage) {

        }
    }




    public static void main(String[] args) {
        ExpensiveFunction expensiveFunction = new ExpensiveFunction();
        Memorizer1<String, Integer> memorizer1 = new Memorizer1<>(null);
        Memorizer2<String, Integer> memorizer2 = new Memorizer2<>(null);
        Memorizer3<String, Integer> memorizer3 = new Memorizer3<>(null);
        Memorizer<String, Integer> memorizer = new Memorizer<>(null);
    }

}
