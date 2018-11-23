package com.concurrency_in_practice.part_1_fundamentals.chap02_thread_safety;

import com.concurrency_in_practice.common.Annotation.ThreadSafe;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.math.BigInteger;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * 2.1. What is Thread Safety?
 *
 * At the heart of any reasonable definition of thread safety is the concept of correctness.
 *
 * **************************************************************************************************************
 * A class is thread-safe if it behaves correctly when accessed from multiple threads,
 * regardless of the scheduling or interleaving of the execution of those threads by the runtime environment,
 * and with no additional synchronization or other coordination on the part of the calling code.
 * **************************************************************************************************************
 *
 * **************************************************************************************************************
 * Thread-safe classes encapsulate any needed synchronization so that clients need not provide their own.
 * **************************************************************************************************************
 *
 * 2.1.1 Example: A Stateless Servlet
 *
 * StatelessFactorizer is stateless: it has no fields and references no fields from other classes.
 * The transient state for a particular computation exists solely in local variables
 * that are stored on the thread's stack and are accessible only to the executing thread.
 *
 * **************************************************************************************************************
 * Stateless objects are always thread-safe.
 * **************************************************************************************************************
 *
 * The actions of a thread accessing a stateless object cannot affect the correctness of operations in other threads.
 *
 */
public class Sec0201_WhatIsThreadSafety {

    /**
     * 2.1.1. Example: A Stateless Servlet
     */

    /**
     * Listing 2.1. A Stateless Servlet.
     */
    @ThreadSafe
    public abstract class StatelessFactorizer implements Servlet {

        public void service(ServletRequest req, ServletResponse resp) {
            BigInteger i = extractFromRequest(req);
            BigInteger[] factors = factor(i);
            encodeIntoResponse(resp, factors);
        }

        private BigInteger extractFromRequest(ServletRequest req) {
            return null;
        }

        private BigInteger[] factor(BigInteger i) {
            return null;
        }

        private void encodeIntoResponse(ServletResponse resp, BigInteger[] factors) {

        }
    }



    public static void main(String[] args) {

    }

}
