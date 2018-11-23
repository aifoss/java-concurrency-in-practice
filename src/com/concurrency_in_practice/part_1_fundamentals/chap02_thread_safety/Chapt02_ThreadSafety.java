package com.concurrency_in_practice.part_1_fundamentals.chap02_thread_safety;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * Chapter 2. Thread Safety
 *
 * Writing thread-safe code is, at its core, about managing access to state, and in particular to shared, mutable state.
 *
 * ************************************************************************************************************************
 * If multiple threads access the same mutable state variable without appropriate synchronization, your program is broken.
 * There are 3 ways to fix it:
 *
 * (a) Don't share the state variable across threads.
 * (b) Make the state variable immutable.
 * (c) Use synchronization whenever accessing the state variable.
 * ************************************************************************************************************************
 *
 * ************************************************************************************************************************
 * When designing thread-safe classes, good object-oriented techniques
 * - encapsulation, immutability, and clear specification of invariants -
 * are your best friends.
 * ************************************************************************************************************************
 */
public class Chapt02_ThreadSafety {

    public static void main(String[] args) {

    }

}
