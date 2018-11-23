package org.concurrency.chap01_introduction;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * 1.2 Benefits of Threads
 *
 * 1.2.1 Exploiting Multiple Processors
 *
 * When properly designed, multithreaded programs can improve throughput
 * by utilizing available processor resources more effectively on multiprocessor systems.
 *
 * Using multiple threads can also help achieve better throughput on single-processor systems.
 * In a multithreaded program, a second thread can run while the first thread is waiting for a synchronous I/O operation to complete,
 * allowing the application to still make progress during the blocking I/O.
 *
 * 1.2.2 Simplicity of Modeling
 *
 * A program that processes one type of task sequentially is simpler to write, less error-prone, and easier to test
 * than one managing multiple different types of tasks at once.
 *
 * Assigning a thread to each type of task or each element in a simulation affords the illusion of sequentiality
 * and insulates domain logic from the details of scheduling, interleaved operations, asynchronous I/O, and resource waits.
 * A complicated, asynchronous workflow can be decomposed into a number of simpler, synchronous workflows
 * each running in a separate thread, interacting only with each other at specific synchronization points.
 *
 * 1.2.3 Simplified Handling of Asynchronous Events
 *
 * A server application that accepts socket connections from multiple remote clients may be easier to develop
 * when each connection is allocated its own thread and allowed to use synchronous I/O.
 *
 * 1.2.4 More Responsive User Interfaces
 *
 * If only short-lived tasks execute in the event thread, the interface remains responsive
 * since the event thread is always able to process user actions reasonably quickly.
 * However, processing a long-running task in the event thread impairs responsiveness.
 * If the long-running tasks is instead executed in a separate thread,
 * the event thread remains free to process UI events, making the UI more responsive.
 */
public class Sec0102_BenefitsOfThreads {

    public static void main(String[] args) {

    }

}
