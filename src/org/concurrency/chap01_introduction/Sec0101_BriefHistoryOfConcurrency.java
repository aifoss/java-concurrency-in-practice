package org.concurrency.chap01_introduction;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * 1.1. A (Very) Brief History of Concurrency
 *
 * Several motivating factors led to the development of operating systems that allowed multiple programs to execute simultaneously:
 *
 * - Resource utilization
 * - Fairness
 * - Convenience
 *
 * The same concerns that motivated the development of processes also motivated the development of threads.
 *
 * Threads allow multiple streams of program control flow to coexist within a process.
 * They share process-wide resources such as memory and file handles,
 * but each thread has its own program counter, stack, and local variables.
 * Threads also provide a natural decomposition for exploiting hardware parallelism on multiprocessor systems;
 * multiple threads within the same program can be scheduled simultaneously on multiple CPUs.
 *
 * Threads are sometimes called lightweight processes,
 * and most modern operating systems treat threads, not processes, as the basic units of scheduling.
 *
 * In the absence of explicit coordination, threads execute simultaneously and asynchronously with respect to one another.
 * Since threads share the memory address space of their owning process,
 * all threads within a process have access to the same variables and allocate objects from the same heap,
 * which allows finer-grained data sharing that inter-process mechanisms.
 * But without explicit synchronization to coordinate access to shared data,
 * a thread may modify variables that another thread is in the middle of using, with unpredictable results.
 */
public class Sec0101_BriefHistoryOfConcurrency {

    public static void main(String[] args) {

    }

}
