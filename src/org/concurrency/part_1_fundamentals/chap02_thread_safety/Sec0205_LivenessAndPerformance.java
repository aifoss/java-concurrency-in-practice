package org.concurrency.part_1_fundamentals.chap02_thread_safety;

import org.concurrency.Annotation.GuardedBy;
import org.concurrency.Annotation.ThreadSafe;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.math.BigInteger;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * 2.5. Liveness and Performance
 *
 * It is easy to improve the concurrency of the servlet while maintaining thread safety by narrowing the scope of the synchronized block.
 * It is reasonable to try to exclude from synchronized blocks long-running operations that do not affect shared state,
 * so that other treads are not prevented from accessing the shared state while the long-running operation is in progress.
 *
 * CachedFactorizer in Listing 2.8 restructures the servlet to use two separate synchronized blocks.
 * One guards the check-then-act sequence that tests whether we can just return the cached result,
 * and the other guards updating both the cached number and the cached factors.
 * The portions of code that are outside the synchronized blocks operate exclusively on local (stack-based) variables,
 * which are not shared across threads and therefore do not require synchronization.
 *
 * The restructuring of CachedFactorizer provides a balance between simplicity (synchronizing the entire method)
 * and concurrency (synchronizing the shortest possible code paths).
 * CachedFactorizer holds the lock when accessing state variables and for the duration of compound actions,
 * but releases it before executing the potentially long-running factorization operation.
 *
 * ****************************************************************************************************************************************
 * There is frequently a tension between simplicity and performance.
 * When implementing a synchronization policy, resist the temptation to prematurely sacrifice simplicity (potentially compromising safety)
 * for the sake of performance.
 * ****************************************************************************************************************************************
 *
 * Holding a lock for a long time, either because you are doing something compute-intensive
 * or because you execute a potentially blocking operation, introduces the risk of liveness or performance problems.
 *
 * ********************************************************************************************************************************
 * Avoid holding locks during lengthy computations or operations at risk of not completing quickly such as network or console I/O.
 * ********************************************************************************************************************************
 */
public class Sec0205_LivenessAndPerformance {

    /**
     * Listing 2.8. Servlet that Caches its Last Request and Result.
     */
    @ThreadSafe
    public abstract class CachedFactorizer implements Servlet {
        @GuardedBy("this") private BigInteger lastNumber;
        @GuardedBy("this") private BigInteger[] lastFactors;
        @GuardedBy("this") private long hits;
        @GuardedBy("this") private long cacheHits;

        public synchronized long getHits() { return hits; }
        public synchronized double getCacheHitRatio() {
            return (double) cacheHits / (double) hits;
        }

        public void service(ServletRequest req, ServletResponse resp) {
            BigInteger i = extractFromRequest(req);
            BigInteger[] factors = null;

            synchronized (this) {
                ++hits;

                if (i.equals(lastNumber)) {
                    ++cacheHits;
                    factors = lastFactors.clone();
                }
            }

            if (factors == null) {
                factors = factor(i);

                synchronized (this) {
                    lastNumber = i;
                    lastFactors = factors.clone();
                }
            }

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

    }

}
