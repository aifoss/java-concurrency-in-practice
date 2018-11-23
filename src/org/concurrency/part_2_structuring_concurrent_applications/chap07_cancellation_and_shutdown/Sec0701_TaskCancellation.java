package org.concurrency.part_2_structuring_concurrent_applications.chap07_cancellation_and_shutdown;

import org.concurrency.Annotation.GuardedBy;
import org.concurrency.Annotation.ThreadSafe;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by sofia on 7/6/17.
 */

/**
 * 7.1 Task Cancellation
 *
 * There are a number of reasons why you might want to cancel an activity:
 * (a) User-requested cancellation.
 * (b) Time-limited activities.
 * (c) Application events.
 * (d) Errors.
 * (e) Shutdown.
 *
 * There is no safe way to preemptively stop a thread in Java, and therefore no safe way to preemptively stop a task.
 * There are only cooperative mechanisms, by which the task and the code requesting cancellation follow an agreed-upon protocol.
 *
 * One such cooperative mechanism is setting a "cancellation requested" flag that the task checks periodically;
 * if it finds the flag set, the task terminates early.
 * PrimeGenerator in Listing 7.1., which enumerates prime numbers until it is cancelled, illustrates this technique.
 *
 * Listing 7.2 shows a sample use of this class that lets the prime generator run for one second before cancelling it.
 *
 * A task that wants to be cancellable must have a cancellation policy that specifies the "how", "when", and "what" of cancellation
 * - how other code can request cancellation, when the task checks whether cancellation has been requested,
 * and what actions the task takes in response to a cancellation request.
 *
 * PrimeGenerator uses a simple cancellation policy:
 * client code requests cancellation by calling cancel,
 * PrimeGenerator checks for cancellation once per prime found and exists when it detects cancellation has been requested.
 *
 * 7.1.1. Interruption
 *
 * If a task that uses this approach calls a blocking method such as BlockingQueue.put, we could have a serious problem
 * - the task might never check the cancellation flag and therefore might never terminate.
 *
 * BrokenPrimeProducer in Listing 7.3 illustrates this problem.
 *
 * Certain blocking library methods support interruption.
 * Thread interruption is a cooperative mechanism for a thread to signal another thread that it should,
 * at its convenience and if it feels like it, stop what it is doing and do something else.
 *
 * *****************************************************************************************************************************
 * There is nothing in the API or language specification that ties interruption to any specific cancellation semantics,
 * but in practice, using interruption for anything but cancellation is fragile and difficult to sustain in larger applications.
 * *****************************************************************************************************************************
 *
 * Each thread has a boolean interrupted status; interrupting a thread sets its interrupted status to true.
 * Thread contains methods for interrupting a thread and querying the interrupted status of a thread, as shown in Listing 7.4.
 *
 * Listing 7.4. Interruption Methods in Thread.
 * public class Thread {
 *     public void interrupt() { ... }
 *     public boolean isInterrupted() { ... }
 *     public static boolean interrupted() { ... }
 *     ...
 * }
 *
 * The interrupt method interrupts the target thread, and isInterrupted returns the interrupted status of the target thread.
 * The interrupted method clears the interrupted status of the current thread and returns its previous value;
 * this is the only way to clear the interrupted status.
 *
 * Booking library methods like Thread.sleep and Object.wait try to detect when a thread has been interrupted and return early.
 * They respond to interruption by clearing the interrupted status and throwing InterruptedException,
 * indicating that the blocking operation completed early due to interruption.
 * The JVM makes no guarantees on how quickly a blocking method will detect interruption,
 * but in practice this happens reasonably quickly.
 *
 * If a thread is interrupted when it is not blocked, its interrupted status is set,
 * and it is up to the activity being cancelled to poll the interrupted status to detect interruption.
 * In this way interruption is "sticky" if it doesn't trigger an InterruptedException,
 * evidence of interruption persists until someone deliberately clears the interrupted status.
 *
 * *****************************************************************************************************************************
 * Calling interrupt does not necessarily stop the target thread from doing what it is doing;
 * it merely delivers the message that interruption has been requested.
 * *****************************************************************************************************************************
 *
 * Interruption does not actually interrupt a running thread; it just requests that the thread interrupts itself
 * at the next convenience opportunity. (These opportunities are called cancellation points.)
 *
 * If you code your tasks to be responsive to interruption, you can use interruption as your cancellation mechanism
 * and take advantage of the interruption support provided by many library classes.
 *
 * *****************************************************************************************************************************
 * Interruption is usually the most sensible way to implement cancellation.
 * *****************************************************************************************************************************
 *
 * BrokenPrimeProducer can be easily fixed by using interruption instead of a boolean flag to request cancellation,
 * as shown in Listing 7.5.
 *
 * 7.1.2. Interruption Policies
 *
 *
 */
public class Sec0701_TaskCancellation {

    /**
     * Listing 7.1. Using a Volatile Field to Hold Cancellation State.
     */
    @ThreadSafe
    public static class PrimeGenerator implements Runnable {
        @GuardedBy("this")
        private final List<BigInteger> primes = new ArrayList<>();
        private volatile boolean cancelled;

        public void run() {
            BigInteger p = BigInteger.ONE;
            while (!cancelled) {
                p = p.nextProbablePrime();
                synchronized (this) {
                    primes.add(p);
                }
            }
        }

        public void cancel() {
            cancelled = true;
        }

        public synchronized List<BigInteger> get() {
            return new ArrayList<>(primes);
        }
    }

    /**
     * Listing 7.2. Generating a Second's Worth of Prime Numbers.
     */
    public static List<BigInteger> aSecondOfPrimes() throws InterruptedException {
        PrimeGenerator generator = new PrimeGenerator();

        new Thread(generator).start();

        try {
            TimeUnit.SECONDS.sleep(1);
        } finally {
            generator.cancel();
        }

        return generator.get();
    }

    /**
     * Listing 7.3. Unreliable Cancellation that can Leave Producers Stuck in a Blocking Operation.
     * Don't Do This.
     */
    public static class BrokenPrimeProducer extends Thread {
        private final BlockingQueue<BigInteger> queue;
        private volatile boolean cancelled = false;

        public BrokenPrimeProducer(BlockingQueue<BigInteger> queue) {
            this.queue = queue;
        }

        public void run() {
            try {
                BigInteger p = BigInteger.ONE;
                while (!cancelled) {
                    queue.put(p = p.nextProbablePrime());
                }
            } catch (InterruptedException e) {

            }
        }

        public void cancel() {
            cancelled = true;
        }
    }

    public static void consumePrimes() throws InterruptedException {
        BlockingQueue<BigInteger> primes = new ArrayBlockingQueue<>(10);
        BrokenPrimeProducer producer = new BrokenPrimeProducer(primes);
        producer.start();

        try {
            while (needMorePrimes()) {
                consume(primes.take());
            }
        } finally {
            producer.cancel();
        }
    }

    public static boolean needMorePrimes() {
        return true;
    }

    public static void consume(BigInteger prime) {

    }

    /**
     * Listing 7.5. Using Interruption for Cancellation.
     */
    public static class PrimeProducer extends Thread {
        private final BlockingQueue<BigInteger> queue;

        public PrimeProducer(BlockingQueue<BigInteger> queue) {
            this.queue = queue;
        }

        public void run() {
            try {
                BigInteger p = BigInteger.ONE;
                while (!Thread.currentThread().isInterrupted()) {
                    queue.put(p = p.nextProbablePrime());
                }
            } catch (InterruptedException e) {
                /* Allow thread to exit */
            }
        }

        public void cancel() {
            interrupt();
        }
    }


    public static void main(String[] args) {

    }

}
