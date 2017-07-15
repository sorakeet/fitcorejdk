/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.ref;

public class ReferenceQueue<T>{
    static ReferenceQueue<Object> NULL=new Null<>();
    static ReferenceQueue<Object> ENQUEUED=new Null<>();
    private Lock lock=new Lock();
    private volatile Reference<? extends T> head=null;
    private long queueLength=0;
    ;

    public ReferenceQueue(){
    }

    boolean enqueue(Reference<? extends T> r){ /** Called only by Reference class */
        synchronized(lock){
            // Check that since getting the lock this reference hasn't already been
            // enqueued (and even then removed)
            ReferenceQueue<?> queue=r.queue;
            if((queue==NULL)||(queue==ENQUEUED)){
                return false;
            }
            assert queue==this;
            r.queue=ENQUEUED;
            r.next=(head==null)?r:head;
            head=r;
            queueLength++;
            if(r instanceof FinalReference){
                sun.misc.VM.addFinalRefCount(1);
            }
            lock.notifyAll();
            return true;
        }
    }

    public Reference<? extends T> poll(){
        if(head==null)
            return null;
        synchronized(lock){
            return reallyPoll();
        }
    }

    @SuppressWarnings("unchecked")
    private Reference<? extends T> reallyPoll(){       /** Must hold lock */
        Reference<? extends T> r=head;
        if(r!=null){
            head=(r.next==r)?
                    null:
                    r.next; // Unchecked due to the next field having a raw type in Reference
            r.queue=NULL;
            r.next=r;
            queueLength--;
            if(r instanceof FinalReference){
                sun.misc.VM.addFinalRefCount(-1);
            }
            return r;
        }
        return null;
    }

    public Reference<? extends T> remove() throws InterruptedException{
        return remove(0);
    }

    public Reference<? extends T> remove(long timeout)
            throws IllegalArgumentException, InterruptedException{
        if(timeout<0){
            throw new IllegalArgumentException("Negative timeout value");
        }
        synchronized(lock){
            Reference<? extends T> r=reallyPoll();
            if(r!=null) return r;
            long start=(timeout==0)?0:System.nanoTime();
            for(;;){
                lock.wait(timeout);
                r=reallyPoll();
                if(r!=null) return r;
                if(timeout!=0){
                    long end=System.nanoTime();
                    timeout-=(end-start)/1000_000;
                    if(timeout<=0) return null;
                    start=end;
                }
            }
        }
    }

    private static class Null<S> extends ReferenceQueue<S>{
        boolean enqueue(Reference<? extends S> r){
            return false;
        }
    }

    static private class Lock{
    }
}
