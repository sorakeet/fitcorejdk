/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.ref;

import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;
import sun.misc.VM;

import java.security.AccessController;
import java.security.PrivilegedAction;

final class Finalizer extends FinalReference<Object>{
    private static final Object lock=new Object();
    private static ReferenceQueue<Object> queue=new ReferenceQueue<>();
    private static Finalizer unfinalized=null;

    static{
        ThreadGroup tg=Thread.currentThread().getThreadGroup();
        for(ThreadGroup tgn=tg;
            tgn!=null;
            tg=tgn,tgn=tg.getParent())
            ;
        Thread finalizer=new FinalizerThread(tg);
        finalizer.setPriority(Thread.MAX_PRIORITY-2);
        finalizer.setDaemon(true);
        finalizer.start();
    }

    private Finalizer
            next=null,
            prev=null;

    private Finalizer(Object finalizee){
        super(finalizee,queue);
        add();
    }

    private void add(){
        synchronized(lock){
            if(unfinalized!=null){
                this.next=unfinalized;
                unfinalized.prev=this;
            }
            unfinalized=this;
        }
    }

    static void register(Object finalizee){
        new Finalizer(finalizee);
    }

    static void runFinalization(){
        if(!VM.isBooted()){
            return;
        }
        forkSecondaryFinalizer(new Runnable(){
            private volatile boolean running;

            public void run(){
                if(running)
                    return;
                final JavaLangAccess jla=SharedSecrets.getJavaLangAccess();
                running=true;
                for(;;){
                    Finalizer f=(Finalizer)queue.poll();
                    if(f==null) break;
                    f.runFinalizer(jla);
                }
            }
        });
    }

    private static void forkSecondaryFinalizer(final Runnable proc){
        AccessController.doPrivileged(
                new PrivilegedAction<Void>(){
                    public Void run(){
                        ThreadGroup tg=Thread.currentThread().getThreadGroup();
                        for(ThreadGroup tgn=tg;
                            tgn!=null;
                            tg=tgn,tgn=tg.getParent())
                            ;
                        Thread sft=new Thread(tg,proc,"Secondary finalizer");
                        sft.start();
                        try{
                            sft.join();
                        }catch(InterruptedException x){
                            /** Ignore */
                        }
                        return null;
                    }
                });
    }

    static void runAllFinalizers(){
        if(!VM.isBooted()){
            return;
        }
        forkSecondaryFinalizer(new Runnable(){
            private volatile boolean running;

            public void run(){
                if(running)
                    return;
                final JavaLangAccess jla=SharedSecrets.getJavaLangAccess();
                running=true;
                for(;;){
                    Finalizer f;
                    synchronized(lock){
                        f=unfinalized;
                        if(f==null) break;
                        unfinalized=f.next;
                    }
                    f.runFinalizer(jla);
                }
            }
        });
    }

    private void runFinalizer(JavaLangAccess jla){
        synchronized(this){
            if(hasBeenFinalized()) return;
            remove();
        }
        try{
            Object finalizee=this.get();
            if(finalizee!=null&&!(finalizee instanceof Enum)){
                jla.invokeFinalize(finalizee);
                /** Clear stack slot containing this variable, to decrease
                 the chances of false retention with a conservative GC */
                finalizee=null;
            }
        }catch(Throwable x){
        }
        super.clear();
    }

    private boolean hasBeenFinalized(){
        return (next==this);
    }

    private void remove(){
        synchronized(lock){
            if(unfinalized==this){
                if(this.next!=null){
                    unfinalized=this.next;
                }else{
                    unfinalized=this.prev;
                }
            }
            if(this.next!=null){
                this.next.prev=this.prev;
            }
            if(this.prev!=null){
                this.prev.next=this.next;
            }
            this.next=this;   /** Indicates that this has been finalized */
            this.prev=this;
        }
    }

    private static class FinalizerThread extends Thread{
        private volatile boolean running;

        FinalizerThread(ThreadGroup g){
            super(g,"Finalizer");
        }

        public void run(){
            if(running)
                return;
            // Finalizer thread starts before System.initializeSystemClass
            // is called.  Wait until JavaLangAccess is available
            while(!VM.isBooted()){
                // delay until VM completes initialization
                try{
                    VM.awaitBooted();
                }catch(InterruptedException x){
                    // ignore and continue
                }
            }
            final JavaLangAccess jla=SharedSecrets.getJavaLangAccess();
            running=true;
            for(;;){
                try{
                    Finalizer f=(Finalizer)queue.remove();
                    f.runFinalizer(jla);
                }catch(InterruptedException x){
                    // ignore and continue
                }
            }
        }
    }
}
