package com.concurrency_in_practice.part_1_fundamentals.chap03_sharing_objects;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * Chapter 3. Sharing Objects
 *
 * This chapter examines techniques for sharing and publishing objects so they can be safely accessed by multiple threads.
 *
 * It is a common misconception that synchronized is only about atomicity or demarcating "critical sections".
 * Synchronization also has another significant, and subtle, aspect: memory visibility.
 * We want not only to prevent one thread from modifying the state of an object when another is using it,
 * but also to ensure that when a thread modifies the state of an object, other threads can actually see the changes that were made.
 * You can ensure that objects are published safely either by using explicit synchronization
 * or by taking advantage of the synchronization built into library classes.
 */
public class Chapt03_SharingObjects {

    public static void main(String[] args) {

    }

}
