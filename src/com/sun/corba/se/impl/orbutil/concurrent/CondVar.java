/**
 * Copyright (c) 2001, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * File: ConditionVariable.java
 * <p>
 * Originally written by Doug Lea and released into the public domain.
 * This may be used for any purposes whatsoever without acknowledgment.
 * Thanks for the assistance and support of Sun Microsystems Labs,
 * and everyone contributing, testing, and using this code.
 * <p>
 * History:
 * Date       Who                What
 * 11Jun1998  dl               Create public version
 * 08dec2001  kmc              Added support for Reentrant Mutexes
 */
/**
 File: ConditionVariable.java

 Originally written by Doug Lea and released into the public domain.
 This may be used for any purposes whatsoever without acknowledgment.
 Thanks for the assistance and support of Sun Microsystems Labs,
 and everyone contributing, testing, and using this code.

 History:
 Date       Who                What
 11Jun1998  dl               Create public version
 08dec2001  kmc              Added support for Reentrant Mutexes
 */
package com.sun.corba.se.impl.orbutil.concurrent;

import com.sun.corba.se.impl.orbutil.ORBUtility;

public class CondVar{
    protected final Sync mutex_;
    protected final ReentrantMutex remutex_;
    protected boolean debug_;

    public CondVar(Sync mutex){
        this(mutex,false);
    }

    public CondVar(Sync mutex,boolean debug){
        debug_=debug;
        mutex_=mutex;
        if(mutex instanceof ReentrantMutex)
            remutex_=(ReentrantMutex)mutex;
        else
            remutex_=null;
    }

    public void await() throws InterruptedException{
        int count=0;
        if(Thread.interrupted())
            throw new InterruptedException();
        try{
            if(debug_)
                ORBUtility.dprintTrace(this,"await enter");
            synchronized(this){
                count=releaseMutex();
                try{
                    wait();
                }catch(InterruptedException ex){
                    notify();
                    throw ex;
                }
            }
        }finally{
            // Must ignore interrupt on re-acquire
            boolean interrupted=false;
            for(;;){
                try{
                    acquireMutex(count);
                    break;
                }catch(InterruptedException ex){
                    interrupted=true;
                }
            }
            if(interrupted){
                Thread.currentThread().interrupt();
            }
            if(debug_)
                ORBUtility.dprintTrace(this,"await exit");
        }
    }

    private int releaseMutex(){
        int count=1;
        if(remutex_!=null)
            count=remutex_.releaseAll();
        else
            mutex_.release();
        return count;
    }

    private void acquireMutex(int count) throws InterruptedException{
        if(remutex_!=null)
            remutex_.acquireAll(count);
        else
            mutex_.acquire();
    }

    public boolean timedwait(long msecs) throws InterruptedException{
        if(Thread.interrupted())
            throw new InterruptedException();
        boolean success=false;
        int count=0;
        try{
            if(debug_)
                ORBUtility.dprintTrace(this,"timedwait enter");
            synchronized(this){
                count=releaseMutex();
                try{
                    if(msecs>0){
                        long start=System.currentTimeMillis();
                        wait(msecs);
                        success=System.currentTimeMillis()-start<=msecs;
                    }
                }catch(InterruptedException ex){
                    notify();
                    throw ex;
                }
            }
        }finally{
            // Must ignore interrupt on re-acquire
            boolean interrupted=false;
            for(;;){
                try{
                    acquireMutex(count);
                    break;
                }catch(InterruptedException ex){
                    interrupted=true;
                }
            }
            if(interrupted){
                Thread.currentThread().interrupt();
            }
            if(debug_)
                ORBUtility.dprintTrace(this,"timedwait exit");
        }
        return success;
    }

    public synchronized void signal(){
        notify();
    }

    public synchronized void broadcast(){
        notifyAll();
    }
}
