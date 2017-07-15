/**
 * Copyright (c) 1999, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

class Shutdown{
    private static final int RUNNING=0;
    private static final int HOOKS=1;
    private static final int FINALIZERS=2;
    // The system shutdown hooks are registered with a predefined slot.
    // The list of shutdown hooks is as follows:
    // (0) Console restore hook
    // (1) Application hooks
    // (2) DeleteOnExit hook
    private static final int MAX_SYSTEM_HOOKS=10;
    private static final Runnable[] hooks=new Runnable[MAX_SYSTEM_HOOKS];
    private static int state=RUNNING;
    private static boolean runFinalizersOnExit=false;
    // the index of the currently running shutdown hook to the hooks array
    private static int currentRunningHook=0;
    private static Object lock=new Lock();
    ;
    private static Object haltLock=new Lock();

    static void setRunFinalizersOnExit(boolean run){
        synchronized(lock){
            runFinalizersOnExit=run;
        }
    }

    static void add(int slot,boolean registerShutdownInProgress,Runnable hook){
        synchronized(lock){
            if(hooks[slot]!=null)
                throw new InternalError("Shutdown hook at slot "+slot+" already registered");
            if(!registerShutdownInProgress){
                if(state>RUNNING)
                    throw new IllegalStateException("Shutdown in progress");
            }else{
                if(state>HOOKS||(state==HOOKS&&slot<=currentRunningHook))
                    throw new IllegalStateException("Shutdown in progress");
            }
            hooks[slot]=hook;
        }
    }

    static void exit(int status){
        boolean runMoreFinalizers=false;
        synchronized(lock){
            if(status!=0) runFinalizersOnExit=false;
            switch(state){
                case RUNNING:       /** Initiate shutdown */
                    state=HOOKS;
                    break;
                case HOOKS:         /** Stall and halt */
                    break;
                case FINALIZERS:
                    if(status!=0){
                        /** Halt immediately on nonzero status */
                        halt(status);
                    }else{
                        /** Compatibility with old behavior:
                         * Run more finalizers and then halt
                         */
                        runMoreFinalizers=runFinalizersOnExit;
                    }
                    break;
            }
        }
        if(runMoreFinalizers){
            runAllFinalizers();
            halt(status);
        }
        synchronized(Shutdown.class){
            /** Synchronize on the class object, causing any other thread
             * that attempts to initiate shutdown to stall indefinitely
             */
            sequence();
            halt(status);
        }
    }

    static void halt(int status){
        synchronized(haltLock){
            halt0(status);
        }
    }

    static native void halt0(int status);

    private static native void runAllFinalizers();

    private static void sequence(){
        synchronized(lock){
            /** Guard against the possibility of a daemon thread invoking exit
             * after DestroyJavaVM initiates the shutdown sequence
             */
            if(state!=HOOKS) return;
        }
        runHooks();
        boolean rfoe;
        synchronized(lock){
            state=FINALIZERS;
            rfoe=runFinalizersOnExit;
        }
        if(rfoe) runAllFinalizers();
    }

    private static void runHooks(){
        for(int i=0;i<MAX_SYSTEM_HOOKS;i++){
            try{
                Runnable hook;
                synchronized(lock){
                    // acquire the lock to make sure the hook registered during
                    // shutdown is visible here.
                    currentRunningHook=i;
                    hook=hooks[i];
                }
                if(hook!=null) hook.run();
            }catch(Throwable t){
                if(t instanceof ThreadDeath){
                    ThreadDeath td=(ThreadDeath)t;
                    throw td;
                }
            }
        }
    }

    static void shutdown(){
        synchronized(lock){
            switch(state){
                case RUNNING:       /** Initiate shutdown */
                    state=HOOKS;
                    break;
                case HOOKS:         /** Stall and then return */
                case FINALIZERS:
                    break;
            }
        }
        synchronized(Shutdown.class){
            sequence();
        }
    }

    private static class Lock{
    }
}
