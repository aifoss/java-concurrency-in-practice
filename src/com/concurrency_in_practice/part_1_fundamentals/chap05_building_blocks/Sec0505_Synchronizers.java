package com.concurrency_in_practice.part_1_fundamentals.chap05_building_blocks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;

/**
 * Created by sofia on 5/27/17.
 */

/**
 * 5.5. Synchronizers
 *
 * Blocking queues are unique among the collection classes:
 * not only do they act as containers for objects,
 * but they can also coordinate the control flow of producer and consumer threads
 * because take and put block until the queue enters the desired state (not empty or not full).
 *
 * A synchronizer is any object that coordinates the control flow of threads based on its state.
 * Blocking queues can act as synchronizers;
 * other types of synchronizers include semaphores, barriers, and latches.
 *
 * All synchronizers share certain structural properties:
 * they encapsulate state that determines whether threads arriving at the synchronizer should be allowed to pass or forced to wait,
 * provide methods to manipulate that state, and provide methods to wait efficiently for the synchronizer to enter the desired state.
 *
 * 5.5.1. Latches
 *
 * A latch is a synchronizer that can delay the progress of threads until it reaches its terminal state.
 * A latch acts as a gate:
 * until the latch reaches the terminal state, the gate is closed and no thread can pass,
 * and in the terminal state, the gate opens, allowing all threads to pass.
 * Once the latch reaches the terminal state, it cannot change state again, so it remains open forever.
 *
 * Latches can be used to ensure that certain activities do not proceed until other one-time activities complete, such as:
 *
 * - Ensuring that a computation does not proceed until resources it needs have been initialized.
 *
 *   A simple binary (two-state) latch could be used to indicate "Resource R has been initialized",
 *   and any activity that requires R would wait first on this latch.
 *
 * - Ensuring that a service does not start until other services on which it depends have started.
 *
 *   Each service would have an associated binary latch;
 *   starting service S would involve first waiting on the latches for other services on which S depends,
 *   and then releasing the S latch after startup completes so any services that depend on S can then proceed.
 *
 * - Waiting until all the parties involved in an activity, for instance, the players in a multi-player game, are ready to proceed.
 *
 *   In this case, the latch reaches the terminal state after all the players are ready.
 *
 * CountDownLatch is a flexible latch implementation that can be used in any of these situations;
 * it allows one or more threads to wait for a set of events to occur.
 * The latch state consists of a counter initialized to a positive number, representing the number of events to wait for.
 * The countDown method decrements the counter, indicating that an event has occurred,
 * and the await methods wait for the counter to reach zero, which happens when all the events have occurred.
 * If the counter is nonzero on entry, await blocks until the counter reaches zero,
 * the waiting thread is interrupted, or the wait times out.
 *
 * TestHarness in Listing 5.11 illustrates two common uses for latches.
 *
 * Why did we bother with the latches in TestHarness instead of just starting the threads immediately after they are created?
 * Presumably, we wanted to measure how long it takes to run a task n times concurrently.
 * Using a starting gate allows the master thread to release all the worker threads at once,
 * and the ending gate allows the master thread to wait for the last thread to finish rather than waiting sequentially for each thread to finish.
 *
 * 5.5.2. Future Task
 *
 * FutureTask also acts like a latch.
 * (FutureTask implements Future, which describes an abstract result-bearing computation.)
 * A computation represented by a FutureTask is implemented with a Callable, the result-bearing equivalent of Runnable,
 * and can be in one of 3 states: waiting to run, running, or completed.
 * Once a FutureTask enters the completed state, it stays in that state forever.
 *
 * The behavior of Future.get depends on the state of the task.
 * If it is completed, get returns the result immediately,
 * and otherwise blocks until the task transitions to the completed state and then returns the result or throws an exception.
 *
 * FutureTask conveys the result from the tread executing the computation to the thread(s) retrieving the result;
 * the specification of FutureTask guarantees that this transfer constitutes a safe publication of the result.
 *
 * FutureTask is used by the Executor framework to represent asynchronous tasks,
 * and can also be used to represent any potentially lengthy computation that can be started before the results are needed.
 *
 * Preloader in Listing 5.12 uses FutureTask to perform an expensive computation whose results are needed later;
 * by starting the computation early, you reduce the time you would have to wait later when you actually need the results.
 *
 * 5.5.3. Semaphores
 *
 * Counting semaphores are used to control the number of activities that can access a certain resource
 * or perform a given action at the same time.
 * Counting semaphores can be used to implement resource pools or to impose a bound on a collection.
 *
 * A Semaphore manages a set of virtual permits; the initial number of permits is passed to the Semaphore constructor.
 * Activities can acquire permits and release permits when they are done with them.
 * If no permit is available, acquire blocks until one is.
 * The release method returns a permit to the semaphore.
 *
 * A degenerate case of a counting semaphore is a binary semaphore, a Semaphore with an initial count of one.
 * A binary semaphore can be used as a mutex with non-reentrant locking semantics;
 * whoever holds the sole permit holds the mutex.
 *
 * Semaphores are useful for implementing resource pools such as database connection pools.
 * If you initialize a Semaphore to the pool size, acquire a permit before trying to fetch a resource from the pool,
 * and release the permit after putting a resource back in the pool,
 * acquire blocks until the pool becomes nonempty.
 *
 * You can use a Semaphore to turn any collection into a blocking bounded collection,
 * as illustrated by BoundedHashSet in Listing 5.14.
 * The semaphore is initialized to the desired maximum size of the collection.
 * The add operation acquires a permit before adding the item into the underlying collection.
 * If the underlying add operation does not actually add anything, it releases the permit immediately.
 * Similarly, a successful remove operation releases a permit, enabling more elements to be added.
 *
 * 5.5.4. Barriers
 *
 * Latches are single-use objects; once a latch enters the terminal state, it cannot be reset.
 *
 * Barriers are similar to latches in that they block a group of threads until some event has occurred.
 * The key difference is that, with a barrier, all the threads must come together at a barrier point at the same time in order to proceed.
 * Latches are for waiting for events; barriers are for waiting for other threads.
 *
 * CyclicBarrier allows a fixed number of parties to rendezvous repeatedly at a barrier point
 * and is useful in parallel iterative algorithms that break down a problem into a fixed number of independent subproblems.
 *
 * Threads call await when they reach the barrier point, and await blocks until all the threads have reached the barrier point.
 * If all threads meet at the barrier point, the barrier has been successfully passed,
 * in which case all threads are released and the barrier is reset so it can be used again.
 * If a call to await times out or a thread blocked in await is interrupted,
 * then the barrier is considered broken and all outstanding calls to await terminate with BrokenBarrierException.
 * If the barrier is successfully passed, await returns a unique arrival index for each thread,
 * which can be used to "elect" a leader that takes some special action in the next iteration.
 *
 * CyclicBarrier also lets you pass a barrier action to the constructor;
 * this is a Runnable that is executed (in one of the subtask threads)
 * when the barrier is successfully passed but before the blocked threads are released.
 *
 * Barriers are often used in simulations,
 * where the work to calculate one step can be done in parallel
 * but all the work associated with a given step must complete before advancing to the next step.
 *
 * For example, in n-body particle simulations, each step calculates an update to the position of each particle
 * based on the locations and other attributes of the other particles.
 * Waiting on a barrier between each update ensures that all updates for step k have completed before moving on to step k+1.
 *
 * CellularAutomata in Listing 5.15 demonstrates using a barrier to compute a cellular automata simulation,
 * such as Conway's Life game.
 *
 * When parallelizing a simulation, it is generally impractical to assign a separate thread to each element;
 * this would require too many threads, and the overhead of coordinating them would dwarf the computation.
 * Instead, it makes sense to partition the problem into a number of subparts,
 * let each thread solve a subpart, and then merge the results.
 *
 * CellularAutomata partitions the board into Ncpu parts, where Ncpu is the number of CPUs available,
 * and assigns each part to a thread.
 * At each step, the worker threads calculate new values for all the cells in their part of the board.
 * When all worker threads have reached the barrier, the barrier action commits the new values to the data model.
 * After the barrier action runs, the worker threads are released to compute the next step of the calculation.
 *
 * Another form of barrier is Exchanger, a two-party barrier in which the parties exchange data at the barrier point.
 * Exchanges are useful when the parties perform asymmetric activities,
 * for example, when one thread fills a buffer with data and the other thread consumes the data from the buffer;
 * these threads could use an Exchanger to meet and exchange a full buffer for an empty one.
 * When two threads exchange objects via an Exchanger,
 * the exchange constitutes a safe publication of both objects to the other party.
 *
 * The timing of the exchange depends on the responsiveness requirements of the application.
 * The simplest approach is that the filling task exchanges when the buffer is full,
 * and the emptying task exchanges when the buffer is empty;
 * this minimizes the number of exchanges but can delay processing of some data if the arrival rate of new data is unpredictable.
 * Another approach would be that the filler exchanges when the buffer is full,
 * but also when the buffer is partially filled and a certain amount of time has elapsed.
 */
public class Sec0505_Synchronizers {

    /**
     * 5.5.1. Latches
     */

    /**
     * Listing 5.11. Using CountDownLatch for Starting and Stopping Threads in Timing Tests.
     */
    public static class TestHarness {

        public long timeTasks(int nThreads, final Runnable task) throws InterruptedException {
            final CountDownLatch startGate = new CountDownLatch(1);
            final CountDownLatch endGate = new CountDownLatch(nThreads);

            for (int i = 0; i < nThreads; i++) {
                Thread t = new Thread() {
                    public void run() {
                        try {
                            // Ensures that no thread starts working until all threads are ready to start
                            startGate.await();
                            try {
                                task.run();
                            } finally {
                                // Allows the master thread to wait efficiently until the last of the worker threads has finished
                                endGate.countDown();
                            }
                        } catch (InterruptedException ignored) {

                        }
                    }
                };
                t.start();
            }

            long start = System.nanoTime();
            startGate.countDown();
            endGate.await();
            long end = System.nanoTime();
            return end-start;
        }
    }

    /**
     * 5.5.2. FutureTask
     */

    /**
     * Listing 5.12. Using FutureTask to Preload Data that is Needed Later.
     */
    public static class Preloader {

        private final FutureTask<ProductInfo> future =
                new FutureTask<ProductInfo>(new Callable<ProductInfo>() {
                    public ProductInfo call() throws DataLoadException {
                        return loadProductionInfo();
                    }
                });

        private final Thread thread = new Thread(future);

        public void start() { thread.start(); }

        public ProductInfo get() throws DataLoadException, InterruptedException {
            try {
                return future.get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof DataLoadException)
                    throw (DataLoadException) cause;
                else
                    throw launderThrowable(cause);
            }
        }

        private ProductInfo loadProductionInfo() {
            return null;
        }
    }

    /**
     * Listing 5.13. Coercing an Unchecked Throwable to a RuntimeException.
     */
    /**
     * If the Throwable is an Error, throw it; if it is a
     * RuntimeException return it, otherwise throw IllegalStateException
     */
    public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else
            throw new IllegalStateException("Not unchecked", t);
    }

    static class ProductInfo {

    }

    static class DataLoadException extends RuntimeException {

    }

    /**
     * 5.5.3. Semaphores
     */

    /**
     * Listing 5.14. Using Semaphore to Bound a Collection.
     */
    public static class BoundedHashSet<T> {

        private final Set<T> set;
        private final Semaphore sem;

        public BoundedHashSet(int bound) {
            this.set = Collections.synchronizedSet(new HashSet<>());
            this.sem = new Semaphore(bound);
        }

        public boolean add(T o) throws InterruptedException {
            sem.acquire();
            boolean wasAdded = false;

            try {
                wasAdded = set.add(o);
                return wasAdded;
            } finally {
                if (!wasAdded)
                    sem.release();
            }
        }

        public boolean remove(Object o) {
            boolean wasRemoved = set.remove(o);
            if (wasRemoved)
                sem.release();
            return wasRemoved;
        }
    }

    /**
     * 5.5.4. Barriers
     */

    /**
     * Listing 5.15. Coordinating Computation in a Cellular Automation with CyclicBarrier.0
     */
    public static class CellularAutomata {

        private final Board mainBoard;
        private final CyclicBarrier barrier;
        private final Worker[] workers;

        public CellularAutomata(Board board) {
            this.mainBoard = board;
            int count = Runtime.getRuntime().availableProcessors();
            this.barrier = new CyclicBarrier(count,
                    new Runnable() {
                        public void run() {
                            mainBoard.commitNewValues();
                        }
                    });
            this.workers = new Worker[count];
            for (int i = 0; i < count; i++)
                workers[i] = new Worker(mainBoard.getSubBoard(count, i));
        }

        private class Worker implements Runnable {
            private final Board board;

            public Worker(Board board) {
                this.board = board;
            }

            public void run() {
                while (!board.hasConverged()) {
                    for (int x = 0; x < board.getMaxX(); x++)
                        for (int y = 0; y < board.getMaxY(); y++)
                            board.setNewValue(x, y, computeValue(x, y));

                    try {
                        barrier.await();
                    } catch (InterruptedException ex) {
                        return;
                    } catch (BrokenBarrierException ex) {
                        return;
                    }
                }
            }

            private int computeValue(int x, int y) { return 0; }
        }

        private static class Board {
            public void commitNewValues() {}
            public Board getSubBoard(int x, int y) { return null; }
            public boolean hasConverged() { return true; }
            public int getMaxX() { return 0; }
            public int getMaxY() { return 0; }
            public void setNewValue(int x, int y, int val) {}
            public void waitForConvergence() {}
        }

        public void start() {
            for (int i = 0; i < workers.length; i++)
                new Thread(workers[i]).start();
            mainBoard.waitForConvergence();
        }
    }



    public static void main(String[] args) {
        TestHarness testHarness = new TestHarness();
        Preloader preloader = new Preloader();
        BoundedHashSet<Integer> boundedHashSet = new BoundedHashSet<>(10);
        CellularAutomata cellularAutomata = new CellularAutomata(null);
    }

}
