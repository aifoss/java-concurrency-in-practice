package org.concurrency.part_1_fundamentals.chap05_building_blocks;

/**
 * Created by sofia on 5/27/17.
 */

import javafx.concurrent.Task;

import java.util.concurrent.BlockingQueue;

/**
 * 5.4. Blocking and Interruptible Methods
 *
 * Threads may block, or pause, for several reasons:
 * waiting for I/O completion, waiting to acquire a lock, waiting to wake up from Thread.sleep,
 * or waiting for the result of a computation in another thread.
 * When a thread blocks, it is usually suspended and placed in one of the blocked thread states (BLOCKED, WAITING, or TIMED_WAITING).
 * The distinction between a blocking operation and an ordinary operation that merely takes a long time to finish
 * is that a blocked thread must wait for an event that is beyond its control before it can proceed.
 * When that external event occurs, the thread is placed back in the RUNNABLE state and becomes eligible again for scheduling.
 *
 * When a method can throw InterruptedException, it is telling you that it is a blocking method,
 * and further that if it is interrupted, it will make an effort to stop blocking early.
 *
 * Thread provides the interrupt method for interrupting a thread and for querying whether a thread has been interrupted.
 *
 * Interruption is a cooperative mechanism.
 * One thread cannot force another to stop what it is doing and do something else;
 * when thread A interrupts thread B, A is merely requesting that B stop what it is doing when it gets to a convenient stopping point.
 *
 * The most sensible use for interruption is to cancel an activity.
 * Blocking methods that are responsive to interruption make it easier to cancel long-running activities on a timely basis.
 *
 * When your code calls a method that throws InterruptedException, then your method is a blocking method too,
 * and must have a plan for responding to interruption.
 * For library code, there are basically two choices:
 *
 * (a) Propagate the Interrupted Exception.
 *
 *     This is often the most sensible policy if you can get away with it.
 *     This could involve not catching InterruptedException,
 *     or catching it and throwing it again after performing some brief activity-specific cleanup.
 *
 * (b) Restored the interrupt.
 *
 *     Sometimes you cannot throw InterruptedException, for instance, when your code is part of a Runnable.
 *     In these situations, you must catch InterruptedException and restore the interrupted status by calling interrupt on the current thread,
 *     so that code higher up the call stack can see that an interrupt was issued, as demonstrated in Listing 5.10.
 *
 * There is one thing you should not do with InterruptedException: catch it and do nothing in response.
 * This deprives code higher up on the call stack of the opportunity to act on the interruption,
 * because the evidence that the thread was interrupted is lost.
 * The only situation in which it is acceptable to swallow an interrupt
 * is when you are extending Thread and therefore control all the code higher up on the call stack.
 *
 */
public class Sec0504_InterruptibleMethods {

    /**
     * Listing 5.10. Restoring the Interrupted Status so as Not to Swallow the Interrupt.
     */
    public static class TaskRunnable implements Runnable {
        BlockingQueue<Task> queue;

        public void run() {
            try {
                processTask(queue.take());
            } catch (InterruptedException e) {
                // restore interrupted status
                Thread.currentThread().interrupt();
            }
        }

        private void processTask(Task task) {}
    }



    public static void main(String[] args) {
        TaskRunnable taskRunnable = new TaskRunnable();
    }

}
