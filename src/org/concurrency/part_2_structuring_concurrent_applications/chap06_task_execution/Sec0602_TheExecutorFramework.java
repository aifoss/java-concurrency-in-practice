package org.concurrency.part_2_structuring_concurrent_applications.chap06_task_execution;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * Created by sofia on 5/28/17.
 */

/**
 * 6.2. The Executor Framework
 *
 * In Chapter 5, we saw how to use bounded queues to prevent an overloaded application from running out of memory.
 * Thread pools offer the same benefit for thread management,
 * and java.util.concurrent provides a flexible thread pool implementation as part of the Executor framework.
 * The primary abstraction for task execution in the Java class libraries is not Thread, but Executor.
 *
 *      Listing 6.3. Executor Interface.
 *
 *      public interface Executor {
 *          void execute(Runnable command);
 *      }
 *
 * Executor is based on the producer-consumer pattern,
 * where activities that submit tasks are the producers and the threads that execute tasks are the consumers.
 *
 * 6.2.1. Example: Web Server Using Executor
 *
 * TaskExecutionWebServer in Listing 6.4 replaces the hard-coded thread creation with an Executor.
 *
 * Executor configuration is generally a one-time event and can easily be exposed for deployment-time configuration,
 * whereas task submission code tends to be strewn throughout the program and harder to expose.
 *
 * We can easily modify TaskExecutionWebServer to behave like ThreadPerTaskWebServer
 * by substituting an Executor that creates a new thread for each request.
 * Writing such an Executor is trivial, as shown in ThreadPerTaskExecutor in Listing 6.5.
 *
 * Similarly, it is also easy to write an Executor that would make TaskExecutionWebServer behave like the single-threaded version,
 * executing each task synchronously before returning from execute, as shown in WithinThreadExecutor in Listing 6.6.
 *
 * 6.2.2. Execution Policies
 *
 * The value of decoupling submission from execution is that it lets you easily specify,
 * and subsequently change without great difficulty, the execution policy for a given class of tasks.
 *
 * An execution policy specifies that "what, where, when, and how" of task execution, including:
 *
 * - In what thread will tasks be executed?
 * - In what order should tasks be executed (FIFO, LIFO, priority order)?
 * - How many tasks may execute concurrently?
 * - How many tasks may be queued pending execution?
 * - If a task has to be rejected because the system is overloaded,
 *   which task should be selected as the victim, and how should the application be notified?
 * - What actions should be taken before or after executing a task?
 *
 * Execution policies are a resource management tool,
 * and the optimal policy depends on the available computing resources and your quality-of-service requirements.
 *
 * Separating the specification of execution policy from task submission makes it practical
 * to select an execution policy at deployment time that is matched to the available hardware.
 *
 * Whenever you see code of the form:
 *
 *      new Thread(runnable).start()
 *
 * and you think you might at some point want a more flexible execution policy,
 * seriously consider replacing it with the user of an Executor.
 *
 * 6.2.3. Thread Pools
 *
 * Executing tasks in pool threads has a number of advantages over the thread-per-task approach.
 * Reusing an existing thread instead of creating a new one amortizes thread creation and teardown costs over multiple requests.
 * As an added bonus, since the worker thread often already exists at the time the request arrives,
 * the latency associated with thread creation does not delay task execution, thus improving responsiveness.
 * By properly tuning the size of the thread pool, you can have enough threads to keep the processors busy
 * while not having so many that your application runs out of memory or thrashes due to competition among threads for resources.
 *
 * You can create a thread pool by calling one of the static factory methods in Executors:
 *
 * - newFixedThreadPool
 *
 *   A fixed-size thread pool creates threads as tasks are submitted, up to the maximum pool size,
 *   and then attempts to keep the pool size constant (adding new threads if a thread dies due to an unexpected Exception).
 *
 * - newCachedThreadPool
 *
 *   A cached thread pool has more flexibility to reap idle threads when the current size of the pool exceeds the demand for processing,
 *   and to add new threads when demand increases, but places no bounds on the size of the pool.
 *
 * - newSingleThreadExecutor
 *
 *   A single-threaded executor creates a single worker thread to process tasks, replacing it if it dies unexpectedly.
 *   Tasks are guaranteed to be processed sequentially according to the order imposed by the task queue.
 *
 * - newScheduledThreadPool
 *
 *   A fixed-size thread pool that supports delayed and periodic task execution, similar to Timer.
 *
 * The newFixedThreadPool and newCachedThreadPool factories return instances of the general-purpose ThreadPoolExecutor,
 * which can also be used directly to construct more specialized executors.
 *
 * The web server in TaskExecutionWebServer uses an Executor with a bounded pool of worker threads.
 * Submitting a task with execute adds the task to the work queue,
 * and the worker threads repeatedly dequeue tasks from the work queue and execute them.
 *
 * Switching form a thread-per-task policy to a pool-based policy has a big effect on application stability:
 * the web server will no longer fail under heavy load.
 * It also degrades more gracefully, since it does not create thousands of threads that compete for limited CPU and memory resources.
 * And using an Executor opens the door to all sorts of additional opportunities for tuning, management, monitoring, logging, error reporting,
 * and other possibilities that would have been far more difficult to add without a task execution framework.
 *
 * 6.2.4. Executor Lifecycle
 *
 * To address the issue of execution service lifecycle, the ExecutorService interface extends Executor,
 * adding a number of methods for lifecycle management.
 *
 * The lifecycle management methods of ExecutorService are shown in Listing 6.7.
 *
 *      Listing 6.7. Lifecycle Methods in ExecutorService.
 *
 *      public interface ExecutorService extends Executor {
 *          void shutdown();
 *          List<Runnable> shutdownNow();
 *          boolean isShutdown();
 *          boolean isTerminated();
 *          boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
 *          // additional convenience methods for task submission
 *      }
 *
 * The lifecycle implied by ExecutorService has 3 states - running, shutting down, and terminated.
 *
 * ExecutorServices are initially created in the running state.
 * The shutdown method initiates a graceful shutdown:
 * no new tasks are accepted but previously submitted tasks are allowed to complete - including those that have not yet begun execution.
 * The shutdownNow method initiates an abrupt shutdown:
 * it attempts to cancel outstanding tasks and does not start any tasks that are queued but not begun.
 *
 * Tasks submitted to an ExecutorService after it has been shut down are handled by the rejected execution handler,
 * which might silently discard the task or might cause execute to throw the unchecked RejectedExecutionException.
 * Once all tasks have completed, the ExecutorService transitions to the terminated state.
 * You can wait for an ExecutorService to reach the terminated state with awaitTermination,
 * or pool for whether it has terminated with isTerminated.
 * It is common to follow shutdown immediately by awaitTermination,
 * creating the effect of synchronously shutting down the ExecutorService.
 *
 * LifecycleWebServer in Listing 6.8 extends our web server with lifecycle support.
 * It can be shut down in 2 ways:
 * programmatically by calling stop,
 * and through a client request by sending the web server a specially formatted HTTP request.
 *
 * 6.2.5. Delayed and Periodic Tasks
 *
 * The Timer facility manages the execution of deferred and periodic tasks.
 * However, Timer has some drawbacks, and ScheduledThreadPoolExecutor should be thought of as its replacement.
 * You can construct a ScheduledThreadPoolExecutor through its constructor or through the newScheduledThreadPool factory.
 *
 * A Timer creates only a single thread for executing timer tasks.
 * If a timer task takes too long to run, the timing accuracy of other TimerTasks can suffer.
 * Scheduled thread pools address this limitation by letting you provide multiple threads for executing deferred and periodic tasks.
 *
 * Another problem with Timer is that it behaves poorly if a TimerTasks throws an unchecked exception.
 * The Timer thread doesn't catch the exception, so an unchecked exception thrown from a TimerTask terminates the Timer thread.
 * Timer also doesn't resurrect the thread in this situation; instead it erroneously assumes the entire Timer was cancelled.
 * In this case, TimerTasks that are already scheduled but not yet executed are never run,
 * and new tasks cannot be scheduled.
 *
 * OutOfTime in Listing 6.9 illustrates how a Timer can become confused in this manner,
 * and how the Timer shares its confusion with the next hapless caller that tries to submit a TimerTask.
 *
 * ScheduledThreadPoolExecutor deals properly with ill-behaved tasks.
 *
 * If you need to build your own scheduling service, you may still be able to take advantage of the library
 * by using a DelayQueue, a BlockingQueue implementation that provides the scheduling functionality of ScheduledThreadPoolExecutor.
 * A DelayQueue manages a collection of Delayed objects.
 * A Delayed has a delay time associated with it:
 * DelayQueue lets you take an element only if its delay has expired.
 * Objects are returned from a DelayQueue ordered by the time associated with their delay.
 */
public class Sec0602_TheExecutorFramework {

    /**
     * 6.2.1. Example: Web Server using Executor
     */

    /**
     * Listing 6.4. Web Server Using a Thread Pool.
     */
    static class TaskExecutionWebServer {

        private static final int NTHREADS = 100;
        private static final Executor executor = Executors.newFixedThreadPool(NTHREADS);

        public static void main(String[] args) throws IOException {
            ServerSocket socket = new ServerSocket(80);
            while (true) {
                final Socket connection = socket.accept();
                Runnable task = new Runnable() {
                    public void run() {
                        handleRequest(connection);
                    }
                };
                executor.execute(task);
            }
        }

        private static void handleRequest(Socket connection) {}
    }

    /**
     * Listing 6.5. Executor that Starts a New Thread for Each Task.
     */
    public static class ThreadPerTaskExecutor implements Executor {
        public void execute(Runnable r) {
            new Thread(r).start();
        }
    }

    /**
     * Listing 6.6. Executor that Executes Tasks Synchronously in the Calling Thread.
     */
    public static class WithinThreadExecutor implements Executor {
        public void execute(Runnable r) {
            r.run();
        }
    }

    /**
     * 6.2.4. Executor Lifecycle
     */

    /**
     * Listing 6.8. Web Server with Shutdown Support.
     */
    static class LifecycleWebServer {

        private final ExecutorService executor = Executors.newFixedThreadPool(100);

        public void start() throws IOException {
            ServerSocket socket = new ServerSocket(80);

            while (!executor.isShutdown()) {
                try {
                    final Socket connection = socket.accept();
                    executor.execute(new Runnable() {
                        public void run() {
                            handleRequest(connection);
                        }
                    });
                } catch (RejectedExecutionException e) {
                    if (!executor.isShutdown()) {
                        log("task submission rejected", e);
                    }
                }
            }
        }

        public void stop() {
            executor.shutdown();
        }

        void handleRequest(Socket connection) {
            Request req = readRequest(connection);
            if (isShutdownRequest(req))
                stop();
            else
                dispatchRequest(req);
        }

        Request readRequest(Socket connection) { return null; }

        boolean isShutdownRequest(Request req) { return false; }

        void dispatchRequest(Request req) {}

        private void log(String msg, RejectedExecutionException e) {}
    }

    static class Request {

    }

    /**
     * 6.2.5. Delayed and Periodic Tasks
     */

    /**
     * Listing 6.9. Class Illustrating Confusing Timer Behavior.
     */
    public static class OutOfTime {
        public static void main(String[] args) throws Exception {
            Timer timer = new Timer();
            timer.schedule(new ThrowTask(), 1);
            TimeUnit.SECONDS.sleep(1);
            timer.schedule(new ThrowTask(), 1);
            TimeUnit.SECONDS.sleep(5);
        }

        static class ThrowTask extends TimerTask {
            public void run() {
                throw new RuntimeException();
            }
        }
    }



    public static void main(String[] args) {
        TaskExecutionWebServer taskExecutionWebServer = new TaskExecutionWebServer();
        ThreadPerTaskExecutor threadPerTaskExecutor = new ThreadPerTaskExecutor();
        WithinThreadExecutor withinThreadExecutor = new WithinThreadExecutor();
        LifecycleWebServer lifecycleWebServer = new LifecycleWebServer();
        OutOfTime outOfTime = new OutOfTime();
    }

}
