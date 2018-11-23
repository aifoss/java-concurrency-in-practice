package org.concurrency.chap01_introduction;

/**
 * Created by sofia on 5/25/17.
 */

/**
 * 1.4 Threads are Everywhere
 *
 * Every Java application uses threads.
 * When the JVM starts, it creates threads for JVM housekeeping tasks (garbage collection, finalization) and a main thread for running the main method.
 *
 * ************************************************************************************************************************
 * Frameworks introduce concurrency into applications by calling application components from framework threads.
 * Components invariably access applications state, thus requiring that all code paths accessing that state be thread-safe.
 * ************************************************************************************************************************
 *
 * e.g., Timer, Servlets and JavaServlet Pages (JSPs), Remote Method Invocation (RMI), Swing and AWT
 */
public class Sec0104_ThreadsAreEverywhere {

    public static void main(String[] args) {

    }

}
