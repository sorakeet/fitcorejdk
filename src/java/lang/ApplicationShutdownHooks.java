/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

import java.util.Collection;
import java.util.IdentityHashMap;

class ApplicationShutdownHooks{
    private static IdentityHashMap<Thread,Thread> hooks;

    static{
        try{
            Shutdown.add(1 /** shutdown hook invocation order */,
                    false /** not registered if shutdown in progress */,
                    new Runnable(){
                        public void run(){
                            runHooks();
                        }
                    }
            );
            hooks=new IdentityHashMap<>();
        }catch(IllegalStateException e){
            // application shutdown hooks cannot be added if
            // shutdown is in progress.
            hooks=null;
        }
    }

    private ApplicationShutdownHooks(){
    }

    static synchronized void add(Thread hook){
        if(hooks==null)
            throw new IllegalStateException("Shutdown in progress");
        if(hook.isAlive())
            throw new IllegalArgumentException("Hook already running");
        if(hooks.containsKey(hook))
            throw new IllegalArgumentException("Hook previously registered");
        hooks.put(hook,hook);
    }

    static synchronized boolean remove(Thread hook){
        if(hooks==null)
            throw new IllegalStateException("Shutdown in progress");
        if(hook==null)
            throw new NullPointerException();
        return hooks.remove(hook)!=null;
    }

    static void runHooks(){
        Collection<Thread> threads;
        synchronized(ApplicationShutdownHooks.class){
            threads=hooks.keySet();
            hooks=null;
        }
        for(Thread hook : threads){
            hook.start();
        }
        for(Thread hook : threads){
            try{
                hook.join();
            }catch(InterruptedException x){
            }
        }
    }
}
