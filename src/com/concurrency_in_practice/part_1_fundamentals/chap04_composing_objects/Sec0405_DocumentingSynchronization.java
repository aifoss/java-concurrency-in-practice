package com.concurrency_in_practice.part_1_fundamentals.chap04_composing_objects;

/**
 * Created by sofia on 5/27/17.
 */

/**
 * 4.5. Documenting Synchronization Policies
 *
 * Documentation is one of the most powerful tools for managing thread safety.
 *
 * ************************************************************
 * Document a class's thread safety guarantees for its clients;
 * document its synchronization policy for its maintainers.
 * ************************************************************
 *
 * Each use of synchronized, volatile, or any thread-safe class reflects a synchronization policy
 * defining a strategy for ensuring the integrity of data in the face of concurrent access.
 * That policy is an element of your program's design, and should be documented.
 *
 * Crafting a synchronization policy requires a number of decision.
 * Some of these are strictly implementation details and should be documented for the sake of future maintainers,
 * but some affect the publicly observable locking behavior of your class and should be documented as part of its specification.
 *
 * At the very least, document the thread safety guarantees made by a class.
 *
 * Is it thread-safe?
 * Does it make callbacks with a lock held?
 * Are there any specific locks that affect is behavior?
 * Don't force clients to make risky guesses.
 * If you don't want to commit to supporting client-side locking, that's fine, but say so.
 * If you want clients to be able to create new atomic operations on your class,
 * you need to document which locks they should acquire to do so safely.
 * If you use locks to guard state, document this for future maintainers - the @GuardedBy annotation will do the trick.
 * If you use more subtle means to maintain thread safety, document them because they may not be obvious to maintainers.
 *
 * 4.5.1. Interpreting Vague Documentation
 *
 * One way to improve the quality of your guess is to interpret the specification from the perspective of someone who will implement it,
 * as opposed to someone who will merely use it.
 */
public class Sec0405_DocumentingSynchronization {

    public static void main(String[] args) {

    }

}
