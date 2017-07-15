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
 * File: Mutex.java
 * <p>
 * Originally written by Doug Lea and released into the public domain.
 * This may be used for any purposes whatsoever without acknowledgment.
 * Thanks for the assistance and support of Sun Microsystems Labs,
 * and everyone contributing, testing, and using this code.
 * <p>
 * History:
 * Date       Who                What
 * 11Jun1998  dl               Create public version
 */
/**
 File: Mutex.java

 Originally written by Doug Lea and released into the public domain.
 This may be used for any purposes whatsoever without acknowledgment.
 Thanks for the assistance and support of Sun Microsystems Labs,
 and everyone contributing, testing, and using this code.

 History:
 Date       Who                What
 11Jun1998  dl               Create public version
 */
package com.sun.corba.se.impl.orbutil.concurrent;

public class Mutex implements Sync{
    protected boolean inuse_=false;

    public void acquire() throws InterruptedException{
        if(Thread.interrupted()) throw new InterruptedException();
        synchronized(this){
            try{
                while(inuse_) wait();
                inuse_=true;
            }catch(InterruptedException ex){
                notify();
                throw ex;
            }
        }
    }

    public boolean attempt(long msecs) throws InterruptedException{
        if(Thread.interrupted()) throw new InterruptedException();
        synchronized(this){
            if(!inuse_){
                inuse_=true;
                return true;
            }else if(msecs<=0)
                return false;
            else{
                long waitTime=msecs;
                long start=System.currentTimeMillis();
                try{
                    for(;;){
                        wait(waitTime);
                        if(!inuse_){
                            inuse_=true;
                            return true;
                        }else{
                            waitTime=msecs-(System.currentTimeMillis()-start);
                            if(waitTime<=0)
                                return false;
                        }
                    }
                }catch(InterruptedException ex){
                    notify();
                    throw ex;
                }
            }
        }
    }

    public synchronized void release(){
        inuse_=false;
        notify();
    }
}
