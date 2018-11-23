package org.concurrency.part_2_structuring_concurrent_applications.chap06_task_execution;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by sofia on 5/28/17.
 */

/**
 * 6.1. Executing Tasks in Threads
 *
 * The first step in organizing a program around task execution is identifying sensible task boundaries.
 * Ideally, tasks are independent activities: work that doesn't depend on the state, result, or side effects of other tasks.
 * Independence facilitates concurrency, as independent tasks can be executed in parallel if there are adequate processing resources.
 * For greater flexibility in scheduling and load balancing tasks,
 * each task should also represent a small fraction of your application's processing capacity.
 *
 * Server applications should exhibit both good throughput and good responsiveness under normal load.
 * Further, applications should exhibit graceful degradation as they become overloaded.
 * Choosing good task boundaries, coupled with a sensible task execution policy, can help achieve these goals.
 *
 * Most server applications offer a natural choice of task boundary: individual client requests.
 * Using individual requests as task boundaries usually offers both independence and appropriate task sizing.
 *
 * 6.1.1. Executing Tasks Sequentially
 *
 * There are a number of possible policies for scheduling tasks within an application,
 * some of which exploit the potential for concurrency better than others.
 * The simplest is to execute tasks sequentially in a single thread.
 *
 * SingleThreadWebServer in Listing 6.1 processes its tasks - HTTP requests arriving on port 80 - sequentially.
 * SingleThreadWebServer would perform poorly in production because it can handle only one request at a time.
 *
 * 6.1.2. Explicitly Creating Threads for Tasks
 *
 * A more responsive approach is to create a new thread for servicing each request,
 * as shown in ThreadPerTaskWebServer in Listing 6.2.
 *
 * ThreadPerTaskWebServer is similar in structure to the single-threaded version
 * - the main thread still alternates between accepting an incoming connection and dispatching the request.
 * The difference is that for each connection, the main loop creates a new thread to process the request
 * instead of processing it within the main thread.
 *
 * This has 3 main consequences:
 *
 * (1) Task processing is offloaded from the main thread,
 *     enabling the main loop to resume waiting for the next incoming connection more quickly.
 *     This enables new connections to be accepted before previous requests complete, improving responsiveness.
 * (2) Tasks can be processed in parallel, enabling multiple requests to be serviced simultaneously.
 *     This may improve throughput if there are multiple processors,
 *     or if tasks need to block for any reason such as I/O completion, lock acquisition, or resource availability.
 * (3) Task-handling cost must be thread-safe, because it may be invoked concurrently for multiple tasks.
 *
 * Under light to moderate load, the thread-per-task approach is an improvement over sequential execution.
 *
 * 6.1.3. Disadvantages of Unbounded Thread Creation
 *
 * For production use, however, the thread-per-task approach has some practical drawbacks,
 * especially when a large number of threads may be created:
 *
 * (1) Thread lifecycle overhead.
 *
 *     Thread creation and teardown are not free.
 *     If requests are frequent and lightweight, as in most server applications,
 *     creating a new thread for each request can consume significant computing resources.
 *
 * (2) Resource consumption.
 *
 *     Active threads consume system resources, especially memory.
 *     When there are more runnable threads than available processors, threads sit idle.
 *     Having many idle threads can tie up a lot of memory, putting pressure on the garbage collector,
 *     and having many threads competing for the CPUs can impose other performance costs as well.
 *
 * (3) Stability.
 *
 *     There is a limit on how many threads can be created.
 *     When you hit this limit, the most likely result is an OutOfMemoryError.
 *     Trying to recover from such an error is very risky.
 *
 * The problem with the thread-per-tasks approach is that nothing places any limit on the number of threads created.
 * For a server application that is supposed to provide high availability and graceful degradation under load, this is a serious failing.
 */
public class Sec0601_ExecutingTasksInThreads {

    /**
     * 6.1.1. Executing Tasks Sequentially
     */

    /**
     * Listing 6.1. Sequential Web Server
     */
    static class SingleThreadWebServer {
        public static void main(String[] args) throws IOException {
            ServerSocket socket = new ServerSocket(80);
            while (true) {
                Socket connection = socket.accept();
                handleRequest(connection);
            }
        }

        private static void handleRequest(Socket connection) {

        }
    }

    /**
     * 6.1.2. Explicitly Creating Threads for Tasks
     */

    /**
     * Listing 6.2. Web Server that Starts a New Thread for Each Request.
     */
    static class ThreadPerTaskWebServer {
        public static void main(String[] args) throws IOException {
            ServerSocket socket = new ServerSocket(80);
            while (true) {
                final Socket connection = socket.accept();
                Runnable task = new Runnable() {
                    public void run() {
                        handleRequest(connection);
                    }
                };
                new Thread(task).start();
            }
        }

        private static void handleRequest(Socket connection) {

        }
    }



    public static void main(String[] args) {

    }

}
