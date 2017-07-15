/**
 * Copyright (c) 1995, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public abstract class Process{
    public abstract OutputStream getOutputStream();

    public abstract InputStream getInputStream();

    public abstract InputStream getErrorStream();

    public abstract int waitFor() throws InterruptedException;

    public boolean waitFor(long timeout,TimeUnit unit)
            throws InterruptedException{
        long startTime=System.nanoTime();
        long rem=unit.toNanos(timeout);
        do{
            try{
                exitValue();
                return true;
            }catch(IllegalThreadStateException ex){
                if(rem>0)
                    Thread.sleep(
                            Math.min(TimeUnit.NANOSECONDS.toMillis(rem)+1,100));
            }
            rem=unit.toNanos(timeout)-(System.nanoTime()-startTime);
        }while(rem>0);
        return false;
    }

    public abstract int exitValue();

    public Process destroyForcibly(){
        destroy();
        return this;
    }

    public abstract void destroy();

    public boolean isAlive(){
        try{
            exitValue();
            return false;
        }catch(IllegalThreadStateException e){
            return true;
        }
    }
}
