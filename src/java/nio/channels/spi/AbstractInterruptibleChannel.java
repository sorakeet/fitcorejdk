/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/**
 */
package java.nio.channels.spi;

import sun.nio.ch.Interruptible;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Channel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.InterruptibleChannel;

public abstract class AbstractInterruptibleChannel
        implements Channel, InterruptibleChannel{
    private final Object closeLock=new Object();
    private volatile boolean open=true;
    // -- Interruption machinery --
    private Interruptible interruptor;
    private volatile Thread interrupted;

    protected AbstractInterruptibleChannel(){
    }

    public final boolean isOpen(){
        return open;
    }

    public final void close() throws IOException{
        synchronized(closeLock){
            if(!open)
                return;
            open=false;
            implCloseChannel();
        }
    }

    protected abstract void implCloseChannel() throws IOException;

    protected final void begin(){
        if(interruptor==null){
            interruptor=new Interruptible(){
                public void interrupt(Thread target){
                    synchronized(closeLock){
                        if(!open)
                            return;
                        open=false;
                        interrupted=target;
                        try{
                            AbstractInterruptibleChannel.this.implCloseChannel();
                        }catch(IOException x){
                        }
                    }
                }
            };
        }
        blockedOn(interruptor);
        Thread me=Thread.currentThread();
        if(me.isInterrupted())
            interruptor.interrupt(me);
    }

    // -- sun.misc.SharedSecrets --
    static void blockedOn(Interruptible intr){         // package-private
        sun.misc.SharedSecrets.getJavaLangAccess().blockedOn(Thread.currentThread(),
                intr);
    }

    protected final void end(boolean completed)
            throws AsynchronousCloseException{
        blockedOn(null);
        Thread interrupted=this.interrupted;
        if(interrupted!=null&&interrupted==Thread.currentThread()){
            interrupted=null;
            throw new ClosedByInterruptException();
        }
        if(!completed&&!open)
            throw new AsynchronousCloseException();
    }
}
