package com.concurrency_in_practice.part_1_fundamentals.chap05_building_blocks;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by sofia on 5/27/17.
 */

/**
 * 5.3. Blocking Queues and the Producer-Consumer Pattern
 *
 * Blocking queues provide a blocking put and take methods as well as the timed equivalents offer and poll.
 *
 * Blocking queues support the producer-consumer design pattern.
 * A producer-consumer design separates the identification of work to be done from the execution of that work
 * by placing work items on a "to do" list for later processing,
 * rather than processing them immediately as they are identified.
 *
 * The producer-consumer pattern simplifies development because it removes code dependencies between producer and consumer classes,
 * and simplifies workload management by decoupling activities that may produce or consume data at different or variable rates.
 *
 * BlockingQueue simplifies the implementation of producer-consumer designs with any number of producers and consumers.
 * One of the most common producer-consumer designs is a thread pool coupled with a work queue;
 * this pattern is embodied in the Executor task execution framework.
 *
 * *******************************************************************************************************************************
 * Bounded queues are a powerful resource management tool for building reliable applications:
 * they make your program most robust to overload by throttling activities that threaten to produce more work than can be handled.
 * *******************************************************************************************************************************
 *
 * While the producer-consumer pattern enables producer and consumer code to be decoupled from each other,
 * their behavior is still coupled indirectly through the shared work queue.
 * Build resource management into your design early using blocking queues.
 * Blocking queues make this easy for a number of situations,
 * but if blocking queues don't fit easily into your design, you can create other blocking data structures using Semaphore.
 *
 * The class library contains several implementations of BlockingQueue.
 * LinkedBlockingQueue and ArrayBlockingQueue are FIFO queues.
 * PriorityBlockingQueue is a priority-ordered queue.
 *
 * The last BlockingQueue implementation, SynchronousQueue, is not really a queue at all,
 * in that it maintains no storage space for queued elements.
 * Instead, it maintains a list of queued threads waiting to enqueue or dequeue an element.
 * It reduces the latency associated with moving data from producer to consumer because the work can be handed off directly.
 * The direct handoff also feeds back more information about the state of the task to the producer;
 * when the handoff is accepted, it knows a consumer has taken responsibility for it.
 * Synchronous queues are generally suitable only when there are enough consumers that there nearly always will be one ready to take the handoff.
 *
 * 5.3.1. Example: Desktop Search
 *
 * DiskCrawler in Listing 5.8 shows a producer task that searches a file hierarchy for files meeting an indexing criterion
 * and puts their names on the work queue;
 * Indexer in Listing 5.8 shows the consumer task that takes file names from the queue and indexes them.
 *
 * The producer-consumer pattern offers a thread-friendly means of decomposing the desktop search problem into simpler components.
 * The producer-consumer pattern also enables several performance benefits.
 * Producers and consumers can execute concurrently.
 *
 * Listing 5.9 starts several crawlers and indexers, each in their own thread.
 * As written, the consumer threads never exit, which prevents the program from terminating.
 *
 * While this example uses explicitly managed threads,
 * many producer-consumer designs can be expressed using the Executor task execution framework.
 *
 * 5.3.2. Serial Thread Confinement
 *
 * For mutable objects, producer-consumer designs and blocking queues facilitate serial thread confinement
 * for handing off ownership of objects from producers to consumers.
 * A thread-confined object is owned exclusively by a single thread,
 * but that ownership can be "transferred" by publishing it safely.
 * The safe publication ensures that the object's state is visible to the new owner,
 * and since the original owner will not touch it again, it is now confined to the new thread.
 * The new owner may modify it freely since it has exclusive access.
 *
 * Object pools exploit serial thread confinement, "lending" an object to a requesting thread.
 * As long as the pool contains sufficient internal synchronization to publish the pooled object safely,
 * and as long as the clients do not themselves publish the pooled object or use it after returning it to the pool,
 * ownership can be transferred safely from thread to thread.
 *
 * Blocking queues make this easy;
 * with a little more work, it could also be done with the atomic remove method of ConcurrentMap
 * or the compareAndSet method of AtomicReference.
 *
 * 5.3.3. Deques and Work Stealing
 *
 * Java 6 also adds two collection types, Deque and BlockingDeque, that extend Queue and BlockingQueue.
 *
 * Just as blocking queues lend themselves to the producer-consumer pattern,
 * deques lend themselves to a related pattern called "stealing".
 *
 * A producer-consumer design has one shared work queue for all consumers;
 * in a work stealing design, every consumer has its own deque.
 * If a consumer exhausts the work in its own deque, it can steal work from the tail of someone else's deque.
 *
 * Work stealing can be more scalable than a traditional producer-consumer design
 * because workers don't contend for a shared work queue;
 * most of the time they access only their own deque, reducing contention.
 * When a worker has to access another's queue, it does so from the tail rather than the head,
 * further reducing contention.
 *
 * Work stealing is well-suited to problems in which consumers are also producers
 * - when performing a unit of work is likely to result in the identification of more work.
 * For example, processing a page in a web crawler usually results in the identification of new pages to be crawled.
 * Similarly, many graph-exploring algorithms, such as marking the heap during garbage collection,
 * can be efficiently parallelized using work stealing.
 * When a worker identifies a new unit of work, it places it at the end of its own deque;
 * when its deque is empty, it looks for work at the end of someone else's deque,
 * ensuring that each worker stays busy.
 */
public class Sec0503_BlockingQueues {

    /**
     * 5.3.1. Example: Desktop Search
     */

    /**
     * Listing 5.8. Producer and Consumer Tasks in a Desktop Search Application.
     */
    public static class FileCrawler implements Runnable {
        private final BlockingQueue<File> fileQueue;
        private final FileFilter fileFilter;
        private final File root;

        public FileCrawler(BlockingQueue<File> fileQueue, FileFilter fileFilter, File root) {
            this.fileQueue = fileQueue;
            this.fileFilter = fileFilter;
            this.root = root;
        }

        public void run() {
            try {
                crawl(root);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void crawl(File root) throws InterruptedException {
            File[] entries = root.listFiles(fileFilter);
            if (entries != null) {
                for (File entry : entries) {
                    if (entry.isDirectory())
                        crawl(entry);
                    else if (!alreadyIndexed(entry))
                        fileQueue.put(entry);
                }
            }
        }

        private boolean alreadyIndexed(File entry) {
            return false;
        }
    }

    public static class Indexer implements Runnable {
        private final BlockingQueue<File> queue;

        public Indexer(BlockingQueue<File> queue) {
            this.queue = queue;
        }

        public void run() {
            try {
                while (true)
                    indexFile(queue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void indexFile(File file) {}
    }

    /**
     * Listing 5.9. Starting the Desktop Search.
     */
    public static void startIndexing(File[] roots) {
        BlockingQueue<File> queue = new LinkedBlockingDeque<>(BOUND);
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return true;
            }
        };

        for (File root : roots) {
            new Thread(new FileCrawler(queue, filter, root)).start();
        }

        for (int i = 0; i < N_CONSUMERS; i++) {
            new Thread(new Indexer(queue)).start();
        }
    }

    static int BOUND = 10;
    static int N_CONSUMERS = 5;



    public static void main(String[] args) {

    }

}
