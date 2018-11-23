package com.concurrency_in_practice.part_1_fundamentals.chap03_sharing_objects;

import com.concurrency_in_practice.common.Annotation.Immutable;
import com.concurrency_in_practice.common.Annotation.ThreadSafe;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sofia on 5/26/17.
 */

/**
 * 3.4. Immutability
 *
 * Thd other end-run around the need to synchronize is to use immutable objects.
 *
 * An immutable object is one whose state cannot be changed after construction.
 * Immutable objects are inherently thread-safe.
 *
 * *****************************************
 * Immutable objects are always thread-safe.
 * *****************************************
 *
 * ****************************************************************************************
 * An object is immutable if:
 * - Its state cannot be modified after construction.
 * - All its fields are final.
 * - It is properly constructed (the "this" reference does not escape during construction).
 * ****************************************************************************************
 *
 * Immutable objects can still use mutable objects internally to manage their state,
 * as illustrated by ThreeStooges in Listing 3.11.
 *
 * There is a difference between an object being immutable and the reference to it being immutable.
 * Program state stored in immutable objects can still be updated by "replacing" immutable objects
 * with a new instance holding new state.
 *
 * 3.4.1. Final Fields
 *
 * The final keyword supports the construction of immutable objects.
 *
 * Final fields can't be modified (although the objects they refer to can be modified if they are mutable),
 * but they also have special semantics under the Java Memory Model.
 * It is the use of final fields that makes possible the guarantee of initialization safety
 * that lets immutable objects be freely accessed and shared without synchronization.
 *
 * Even if an object is mutable, making some fields final can still simplify reasoning about its state,
 * since limiting the mutability of an object restricts its set of possible states.
 * An object that is "mostly immutable" but has one or two mutable state variables is still simpler
 * than one that has many mutable variables.
 * Declaring fields final also documents to maintainers that these fields are not expected to change.
 *
 * *********************************************************************************************
 * Just as it is a good practice to make all fields private unless they need greater visibility,
 * it is a good practice to make all fields final unless they need to be mutable.
 * *********************************************************************************************
 *
 * 3.4.2. Example: Using Volatile to Publish Immutable Objects
 *
 * Immutable objects can sometimes provide a weak form of atomicity.
 *
 * Whenever a group of related items must be acted on atomically,
 * consider creating an immutable holder class for them,
 * such as OneValueCache in Listing 3.12.
 *
 * Race conditions in accessing or updating mutable related variables can be eliminated
 * by using an immutable object to hold all the variables.
 *
 * With a mutable holder object, you would have to use locking to ensure atomicity;
 * with an immutable one, once a thread acquires a reference to it, it need never worry about another thread modifying its state.
 *
 * VolatileCachedFactorizer in Listing 3.13 uses a OneValueCache to store the cached number and factors.
 * This combination of an immutable holder object for mutable state variables related by an invariant,
 * and a volatile reference used to ensure its timely visibility,
 * allows VolatileCachedFactorizer to be thread-safe even though it does no explicit locking.
 */
public class Sec0304_Immutability {

    /**
     * Listing 3.11. Immutable Class Built Out of Mutable Underlying Objects.
     */
    @Immutable
    public static final class ThreeStooges {
        private final Set<String> stooges = new HashSet<>();

        public ThreeStooges() {
            stooges.add("Moe");
            stooges.add("Larry");
            stooges.add("Curly");
        }

        public boolean isStooge(String name) {
            return stooges.contains(name);
        }
    }

    /**
     * 3.4.2. Example: Using Volatile to Publish Immutable Objects
     */

    /**
     * Listing 3.12. Immutable Holder for Caching a Number and its Factors.
     */
    @Immutable
    class OneValueCache {
        private final BigInteger lastNumber;
        private final BigInteger[] lastFactors;

        public OneValueCache(BigInteger i, BigInteger[] factors) {
            lastNumber = i;
            lastFactors = Arrays.copyOf(factors, factors.length);
        }

        public BigInteger[] getFactors(BigInteger i) {
            if (lastNumber == null || !lastNumber.equals(i))
                return null;
            else
                return Arrays.copyOf(lastFactors, lastFactors.length);
        }
    }

    /**
     * Listing 3.13. Caching the Last Result Using a Volatile Reference to an Immutable Holder Object.
     */
    @ThreadSafe
    public abstract class VolatileCachedFactorizer implements Servlet {
        private volatile OneValueCache cache = new OneValueCache(null, null);

        public void service(ServletRequest req, ServletResponse resp) {
            BigInteger i = extractFromRequest(req);
            BigInteger[] factors = cache.getFactors(i);

            if (factors == null) {
                factors = factor(i);
                cache = new OneValueCache(i, factors);
            }

            encoseIntoResponse(resp, factors);
        }

        private BigInteger extractFromRequest(ServletRequest req) {
            return null;
        }

        private BigInteger[] factor(BigInteger i) {
            return null;
        }

        private void encoseIntoResponse(ServletResponse resp, BigInteger[] factors) {

        }
    }



    public static void main(String[] args) {
        ThreeStooges threeStooges = new ThreeStooges();
    }

}
