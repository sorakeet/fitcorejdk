/**
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.internal;

import com.sun.jmx.remote.util.ClassLogger;
import com.sun.jmx.remote.util.EnvHelp;

import java.io.IOException;
import java.io.InterruptedIOException;

public abstract class ClientCommunicatorAdmin{
    // state
    private final static int CONNECTED=0;
    private final static int RE_CONNECTING=1;
    private final static int FAILED=2;
    private final static int TERMINATED=3;
    private static final ClassLogger logger=
            new ClassLogger("javax.management.remote.misc",
                    "ClientCommunicatorAdmin");
    private static volatile long threadNo=1;
    // --------------------------------------------------------------
// private variables
// --------------------------------------------------------------
    private final Checker checker;
    private final int[] lock=new int[0];
    private long period;
    private int state=CONNECTED;
    public ClientCommunicatorAdmin(long period){
        this.period=period;
        if(period>0){
            checker=new Checker();
            Thread t=new Thread(checker,"JMX client heartbeat "+ ++threadNo);
            t.setDaemon(true);
            t.start();
        }else
            checker=null;
    }

    public void gotIOException(IOException ioe) throws IOException{
        restart(ioe);
    }

    private void restart(IOException ioe) throws IOException{
        // check state
        synchronized(lock){
            if(state==TERMINATED){
                throw new IOException("The client has been closed.");
            }else if(state==FAILED){ // already failed to re-start by another thread
                throw ioe;
            }else if(state==RE_CONNECTING){
                // restart process has been called by another thread
                // we need to wait
                while(state==RE_CONNECTING){
                    try{
                        lock.wait();
                    }catch(InterruptedException ire){
                        // be asked to give up
                        InterruptedIOException iioe=new InterruptedIOException(ire.toString());
                        EnvHelp.initCause(iioe,ire);
                        throw iioe;
                    }
                }
                if(state==TERMINATED){
                    throw new IOException("The client has been closed.");
                }else if(state!=CONNECTED){
                    // restarted is failed by another thread
                    throw ioe;
                }
                return;
            }else{
                state=RE_CONNECTING;
                lock.notifyAll();
            }
        }
        // re-starting
        try{
            doStart();
            synchronized(lock){
                if(state==TERMINATED){
                    throw new IOException("The client has been closed.");
                }
                state=CONNECTED;
                lock.notifyAll();
            }
            return;
        }catch(Exception e){
            logger.warning("restart","Failed to restart: "+e);
            logger.debug("restart",e);
            synchronized(lock){
                if(state==TERMINATED){
                    throw new IOException("The client has been closed.");
                }
                state=FAILED;
                lock.notifyAll();
            }
            try{
                doStop();
            }catch(Exception eee){
                // OK.
                // We know there is a problem.
            }
            terminate();
            throw ioe;
        }
    }

    protected abstract void doStart() throws IOException;

    protected abstract void doStop();

    public void terminate(){
        synchronized(lock){
            if(state==TERMINATED){
                return;
            }
            state=TERMINATED;
            lock.notifyAll();
            if(checker!=null)
                checker.stop();
        }
    }

    protected abstract void checkConnection() throws IOException;

    // --------------------------------------------------------------
// private varaibles
// --------------------------------------------------------------
    private class Checker implements Runnable{
        private Thread myThread;

        public void run(){
            myThread=Thread.currentThread();
            while(state!=TERMINATED&&!myThread.isInterrupted()){
                try{
                    Thread.sleep(period);
                }catch(InterruptedException ire){
                    // OK.
                    // We will check the state at the following steps
                }
                if(state==TERMINATED||myThread.isInterrupted()){
                    break;
                }
                try{
                    checkConnection();
                }catch(Exception e){
                    synchronized(lock){
                        if(state==TERMINATED||myThread.isInterrupted()){
                            break;
                        }
                    }
                    e=(Exception)EnvHelp.getCause(e);
                    if(e instanceof IOException&&
                            !(e instanceof InterruptedIOException)){
                        try{
                            gotIOException((IOException)e);
                        }catch(Exception ee){
                            logger.warning("Checker-run",
                                    "Failed to check connection: "+e);
                            logger.warning("Checker-run","stopping");
                            logger.debug("Checker-run",e);
                            break;
                        }
                    }else{
                        logger.warning("Checker-run",
                                "Failed to check the connection: "+e);
                        logger.debug("Checker-run",e);
                        // XXX stop checking?
                        break;
                    }
                }
            }
            if(logger.traceOn()){
                logger.trace("Checker-run","Finished.");
            }
        }

        private void stop(){
            if(myThread!=null&&myThread!=Thread.currentThread()){
                myThread.interrupt();
            }
        }
    }
}
