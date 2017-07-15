/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.internal;

import com.sun.jmx.remote.util.ClassLogger;

public abstract class ServerCommunicatorAdmin{
    private static final ClassLogger logger=
            new ClassLogger("javax.management.remote.misc",
                    "ServerCommunicatorAdmin");
    private static final ClassLogger timelogger=
            new ClassLogger("javax.management.remote.timeout",
                    "ServerCommunicatorAdmin");
    private final int[] lock=new int[0];
    // --------------------------------------------------------------
// private variables
// --------------------------------------------------------------
    private long timestamp;
    private int currentJobs=0;
    private long timeout;
    // state issue
    private boolean terminated=false;

    public ServerCommunicatorAdmin(long timeout){
        if(logger.traceOn()){
            logger.trace("Constructor",
                    "Creates a new ServerCommunicatorAdmin object "+
                            "with the timeout "+timeout);
        }
        this.timeout=timeout;
        timestamp=0;
        if(timeout<Long.MAX_VALUE){
            Runnable timeoutTask=new Timeout();
            final Thread t=new Thread(timeoutTask);
            t.setName("JMX server connection timeout "+t.getId());
            // If you change this name you will need to change a unit test
            // (NoServerTimeoutTest)
            t.setDaemon(true);
            t.start();
        }
    }

    public boolean reqIncoming(){
        if(logger.traceOn()){
            logger.trace("reqIncoming","Receive a new request.");
        }
        synchronized(lock){
            if(terminated){
                logger.warning("reqIncoming",
                        "The server has decided to close "+
                                "this client connection.");
            }
            ++currentJobs;
            return terminated;
        }
    }

    public boolean rspOutgoing(){
        if(logger.traceOn()){
            logger.trace("reqIncoming","Finish a request.");
        }
        synchronized(lock){
            if(--currentJobs==0){
                timestamp=System.currentTimeMillis();
                logtime("Admin: Timestamp=",timestamp);
                // tells the adminor to restart waiting with timeout
                lock.notify();
            }
            return terminated;
        }
    }

    private void logtime(String desc,long time){
        timelogger.trace("synchro",desc+time);
    }

    protected abstract void doStop();

    public void terminate(){
        if(logger.traceOn()){
            logger.trace("terminate",
                    "terminate the ServerCommunicatorAdmin object.");
        }
        synchronized(lock){
            if(terminated){
                return;
            }
            terminated=true;
            // tell Timeout to terminate
            lock.notify();
        }
    }

    // --------------------------------------------------------------
// private classes
// --------------------------------------------------------------
    private class Timeout implements Runnable{
        public void run(){
            boolean stopping=false;
            synchronized(lock){
                if(timestamp==0) timestamp=System.currentTimeMillis();
                logtime("Admin: timeout=",timeout);
                logtime("Admin: Timestamp=",timestamp);
                while(!terminated){
                    try{
                        // wait until there is no more job
                        while(!terminated&&currentJobs!=0){
                            if(logger.traceOn()){
                                logger.trace("Timeout-run",
                                        "Waiting without timeout.");
                            }
                            lock.wait();
                        }
                        if(terminated) return;
                        final long remaining=
                                timeout-(System.currentTimeMillis()-timestamp);
                        logtime("Admin: remaining timeout=",remaining);
                        if(remaining>0){
                            if(logger.traceOn()){
                                logger.trace("Timeout-run",
                                        "Waiting with timeout: "+
                                                remaining+" ms remaining");
                            }
                            lock.wait(remaining);
                        }
                        if(currentJobs>0) continue;
                        final long elapsed=
                                System.currentTimeMillis()-timestamp;
                        logtime("Admin: elapsed=",elapsed);
                        if(!terminated&&elapsed>timeout){
                            if(logger.traceOn()){
                                logger.trace("Timeout-run",
                                        "timeout elapsed");
                            }
                            logtime("Admin: timeout elapsed! "+
                                    elapsed+">",timeout);
                            // stopping
                            terminated=true;
                            stopping=true;
                            break;
                        }
                    }catch(InterruptedException ire){
                        logger.warning("Timeout-run","Unexpected Exception: "+
                                ire);
                        logger.debug("Timeout-run",ire);
                        return;
                    }
                }
            }
            if(stopping){
                if(logger.traceOn()){
                    logger.trace("Timeout-run","Call the doStop.");
                }
                doStop();
            }
        }
    }
}