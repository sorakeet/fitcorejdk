/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.ref;

import sun.misc.Cleaner;
import sun.misc.JavaLangRefAccess;
import sun.misc.SharedSecrets;

public abstract class Reference<T>{
    private static Lock lock=new Lock();
    private static Reference<Object> pending=null;

    static{
        ThreadGroup tg=Thread.currentThread().getThreadGroup();
        for(ThreadGroup tgn=tg;
            tgn!=null;
            tg=tgn,tgn=tg.getParent())
            ;
        Thread handler=new ReferenceHandler(tg,"Reference Handler");
        /** If there were a special system-only priority greater than
         * MAX_PRIORITY, it would be used here
         */
        handler.setPriority(Thread.MAX_PRIORITY);
        handler.setDaemon(true);
        handler.start();
        // provide access in SharedSecrets
        SharedSecrets.setJavaLangRefAccess(new JavaLangRefAccess(){
            @Override
            public boolean tryHandlePendingReference(){
                return tryHandlePending(false);
            }
        });
    }

    volatile ReferenceQueue<? super T> queue;
    @SuppressWarnings("rawtypes")
    Reference next;
    private T referent;
    /** Treated specially by GC */
    transient private Reference<T> discovered;

    Reference(T referent){
        this(referent,null);
    }

    Reference(T referent,ReferenceQueue<? super T> queue){
        this.referent=referent;
        this.queue=(queue==null)?ReferenceQueue.NULL:queue;
    }

    static boolean tryHandlePending(boolean waitForNotify){
        Reference<Object> r;
        Cleaner c;
        try{
            synchronized(lock){
                if(pending!=null){
                    r=pending;
                    // 'instanceof' might throw OutOfMemoryError sometimes
                    // so do this before un-linking 'r' from the 'pending' chain...
                    c=r instanceof Cleaner?(Cleaner)r:null;
                    // unlink 'r' from 'pending' chain
                    pending=r.discovered;
                    r.discovered=null;
                }else{
                    // The waiting on the lock may cause an OutOfMemoryError
                    // because it may try to allocate exception objects.
                    if(waitForNotify){
                        lock.wait();
                    }
                    // retry if waited
                    return waitForNotify;
                }
            }
        }catch(OutOfMemoryError x){
            // Give other threads CPU time so they hopefully drop some live references
            // and GC reclaims some space.
            // Also prevent CPU intensive spinning in case 'r instanceof Cleaner' above
            // persistently throws OOME for some time...
            Thread.yield();
            // retry
            return true;
        }catch(InterruptedException x){
            // retry
            return true;
        }
        // Fast path for cleaners
        if(c!=null){
            c.clean();
            return true;
        }
        ReferenceQueue<? super Object> q=r.queue;
        if(q!=ReferenceQueue.NULL) q.enqueue(r);
        return true;
    }

    public T get(){
        return this.referent;
    }

    public void clear(){
        this.referent=null;
    }

    public boolean isEnqueued(){
        return (this.queue==ReferenceQueue.ENQUEUED);
    }

    public boolean enqueue(){
        return this.queue.enqueue(this);
    }

    /** -- Constructors -- */
    static private class Lock{
    }

    private static class ReferenceHandler extends Thread{
        static{
            // pre-load and initialize InterruptedException and Cleaner classes
            // so that we don't get into trouble later in the run loop if there's
            // memory shortage while loading/initializing them lazily.
            ensureClassInitialized(InterruptedException.class);
            ensureClassInitialized(Cleaner.class);
        }

        ReferenceHandler(ThreadGroup g,String name){
            super(g,name);
        }

        private static void ensureClassInitialized(Class<?> clazz){
            try{
                Class.forName(clazz.getName(),true,clazz.getClassLoader());
            }catch(ClassNotFoundException e){
                throw (Error)new NoClassDefFoundError(e.getMessage()).initCause(e);
            }
        }

        public void run(){
            while(true){
                tryHandlePending(true);
            }
        }
    }
}
