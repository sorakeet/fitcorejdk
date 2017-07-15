/**
 * Copyright (c) 1994, 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

import sun.nio.ch.Interruptible;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.security.util.SecurityConstants;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Thread implements Runnable{
    public final static int MIN_PRIORITY=1;
    public final static int NORM_PRIORITY=5;
    public final static int MAX_PRIORITY=10;
    private static final StackTraceElement[] EMPTY_STACK_TRACE
            =new StackTraceElement[0];
    private static final RuntimePermission SUBCLASS_IMPLEMENTATION_PERMISSION=
            new RuntimePermission("enableContextClassLoaderOverride");
    private static int threadInitNumber;
    private static long threadSeqNumber;
    // null unless explicitly set
    private static volatile UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    static{
        registerNatives();
    }

    private final Object blockerLock=new Object();
    ThreadLocal.ThreadLocalMap threadLocals=null;
    ThreadLocal.ThreadLocalMap inheritableThreadLocals=null;
    volatile Object parkBlocker;
    // The following three initially uninitialized fields are exclusively
    // managed by class java.util.concurrent.ThreadLocalRandom. These
    // fields are used to build the high-performance PRNGs in the
    // concurrent code, and we can not risk accidental false sharing.
    // Hence, the fields are isolated with @Contended.
    @sun.misc.Contended("tlr")
    long threadLocalRandomSeed;
    @sun.misc.Contended("tlr")
    int threadLocalRandomProbe;
    @sun.misc.Contended("tlr")
    int threadLocalRandomSecondarySeed;
    private volatile String name;
    private int priority;
    private Thread threadQ;
    private long eetop;
    private boolean single_step;
    private boolean daemon=false;
    private boolean stillborn=false;
    private Runnable target;
    private ThreadGroup group;
    private ClassLoader contextClassLoader;
    private AccessControlContext inheritedAccessControlContext;
    private long stackSize;
    private long nativeParkEventPointer;
    private long tid;
    private volatile int threadStatus=0;
    private volatile Interruptible blocker;
    // null unless explicitly set
    private volatile UncaughtExceptionHandler uncaughtExceptionHandler;

    public Thread(){
        init(null,null,"Thread-"+nextThreadNum(),0);
    }

    private static synchronized int nextThreadNum(){
        return threadInitNumber++;
    }

    private void init(ThreadGroup g,Runnable target,String name,
                      long stackSize){
        init(g,target,name,stackSize,null,true);
    }

    private void init(ThreadGroup g,Runnable target,String name,
                      long stackSize,AccessControlContext acc,
                      boolean inheritThreadLocals){
        if(name==null){
            throw new NullPointerException("name cannot be null");
        }
        this.name=name;
        Thread parent=currentThread();
        SecurityManager security=System.getSecurityManager();
        if(g==null){
            /** Determine if it's an applet or not */
            /** If there is a security manager, ask the security manager
             what to do. */
            if(security!=null){
                g=security.getThreadGroup();
            }
            /** If the security doesn't have a strong opinion of the matter
             use the parent thread group. */
            if(g==null){
                g=parent.getThreadGroup();
            }
        }
        /** checkAccess regardless of whether or not threadgroup is
         explicitly passed in. */
        g.checkAccess();
        /**
         * Do we have the required permissions?
         */
        if(security!=null){
            if(isCCLOverridden(getClass())){
                security.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
            }
        }
        g.addUnstarted();
        this.group=g;
        this.daemon=parent.isDaemon();
        this.priority=parent.getPriority();
        if(security==null||isCCLOverridden(parent.getClass()))
            this.contextClassLoader=parent.getContextClassLoader();
        else
            this.contextClassLoader=parent.contextClassLoader;
        this.inheritedAccessControlContext=
                acc!=null?acc:AccessController.getContext();
        this.target=target;
        setPriority(priority);
        if(inheritThreadLocals&&parent.inheritableThreadLocals!=null)
            this.inheritableThreadLocals=
                    ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
        /** Stash the specified stack size in case the VM cares */
        this.stackSize=stackSize;
        /** Set thread ID */
        tid=nextThreadID();
    }

    private static synchronized long nextThreadID(){
        return ++threadSeqNumber;
    }    @Override
    protected Object clone() throws CloneNotSupportedException{
        throw new CloneNotSupportedException();
    }

    public static native Thread currentThread();

    private static boolean isCCLOverridden(Class<?> cl){
        if(cl==Thread.class)
            return false;
        processQueue(Caches.subclassAuditsQueue,Caches.subclassAudits);
        WeakClassKey key=new WeakClassKey(cl,Caches.subclassAuditsQueue);
        Boolean result=Caches.subclassAudits.get(key);
        if(result==null){
            result=Boolean.valueOf(auditSubclass(cl));
            Caches.subclassAudits.putIfAbsent(key,result);
        }
        return result.booleanValue();
    }

    private static boolean auditSubclass(final Class<?> subcl){
        Boolean result=AccessController.doPrivileged(
                new PrivilegedAction<Boolean>(){
                    public Boolean run(){
                        for(Class<?> cl=subcl;
                            cl!=Thread.class;
                            cl=cl.getSuperclass()){
                            try{
                                cl.getDeclaredMethod("getContextClassLoader",new Class<?>[0]);
                                return Boolean.TRUE;
                            }catch(NoSuchMethodException ex){
                            }
                            try{
                                Class<?>[] params={ClassLoader.class};
                                cl.getDeclaredMethod("setContextClassLoader",params);
                                return Boolean.TRUE;
                            }catch(NoSuchMethodException ex){
                            }
                        }
                        return Boolean.FALSE;
                    }
                }
        );
        return result.booleanValue();
    }

    static void processQueue(ReferenceQueue<Class<?>> queue,
                             ConcurrentMap<? extends
                                     WeakReference<Class<?>>,?> map){
        Reference<? extends Class<?>> ref;
        while((ref=queue.poll())!=null){
            map.remove(ref);
        }
    }

    public Thread(Runnable target){
        init(null,target,"Thread-"+nextThreadNum(),0);
    }

    Thread(Runnable target,AccessControlContext acc){
        init(null,target,"Thread-"+nextThreadNum(),0,acc,false);
    }

    public Thread(ThreadGroup group,Runnable target){
        init(group,target,"Thread-"+nextThreadNum(),0);
    }

    public Thread(String name){
        init(null,null,name,0);
    }

    public Thread(ThreadGroup group,String name){
        init(group,null,name,0);
    }

    public Thread(Runnable target,String name){
        init(null,target,name,0);
    }

    public Thread(ThreadGroup group,Runnable target,String name){
        init(group,target,name,0);
    }

    public Thread(ThreadGroup group,Runnable target,String name,
                  long stackSize){
        init(group,target,name,stackSize);
    }

    private static native void registerNatives();

    public static native void yield();

    public static void sleep(long millis,int nanos)
            throws InterruptedException{
        if(millis<0){
            throw new IllegalArgumentException("timeout value is negative");
        }
        if(nanos<0||nanos>999999){
            throw new IllegalArgumentException(
                    "nanosecond timeout value out of range");
        }
        if(nanos>=500000||(nanos!=0&&millis==0)){
            millis++;
        }
        sleep(millis);
    }

    public static native void sleep(long millis) throws InterruptedException;

    public static boolean interrupted(){
        return currentThread().isInterrupted(true);
    }

    public static int activeCount(){
        return currentThread().getThreadGroup().activeCount();
    }

    public static int enumerate(Thread tarray[]){
        return currentThread().getThreadGroup().enumerate(tarray);
    }

    public static void dumpStack(){
        new Exception("Stack trace").printStackTrace();
    }

    public static native boolean holdsLock(Object obj);

    public static Map<Thread,StackTraceElement[]> getAllStackTraces(){
        // check for getStackTrace permission
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            security.checkPermission(
                    SecurityConstants.GET_STACK_TRACE_PERMISSION);
            security.checkPermission(
                    SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
        }
        // Get a snapshot of the list of all threads
        Thread[] threads=getThreads();
        StackTraceElement[][] traces=dumpThreads(threads);
        Map<Thread,StackTraceElement[]> m=new HashMap<>(threads.length);
        for(int i=0;i<threads.length;i++){
            StackTraceElement[] stackTrace=traces[i];
            if(stackTrace!=null){
                m.put(threads[i],stackTrace);
            }
            // else terminated so we don't put it in the map
        }
        return m;
    }

    private native static Thread[] getThreads();

    private native static StackTraceElement[][] dumpThreads(Thread[] threads);

    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler(){
        return defaultUncaughtExceptionHandler;
    }    public final void setPriority(int newPriority){
        ThreadGroup g;
        checkAccess();
        if(newPriority>MAX_PRIORITY||newPriority<MIN_PRIORITY){
            throw new IllegalArgumentException();
        }
        if((g=getThreadGroup())!=null){
            if(newPriority>g.getMaxPriority()){
                newPriority=g.getMaxPriority();
            }
            setPriority0(priority=newPriority);
        }
    }

    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(
                    new RuntimePermission("setDefaultUncaughtExceptionHandler")
            );
        }
        defaultUncaughtExceptionHandler=eh;
    }

    void blockedOn(Interruptible b){
        synchronized(blockerLock){
            blocker=b;
        }
    }    public final int getPriority(){
        return priority;
    }

    public synchronized void start(){
        /**
         * This method is not invoked for the main method thread or "system"
         * group threads created/set up by the VM. Any new functionality added
         * to this method in the future may have to also be added to the VM.
         *
         * A zero status value corresponds to state "NEW".
         */
        if(threadStatus!=0)
            throw new IllegalThreadStateException();
        /** Notify the group that this thread is about to be started
         * so that it can be added to the group's list of threads
         * and the group's unstarted count can be decremented. */
        group.add(this);
        boolean started=false;
        try{
            start0();
            started=true;
        }finally{
            try{
                if(!started){
                    group.threadStartFailed(this);
                }
            }catch(Throwable ignore){
                /** do nothing. If start0 threw a Throwable then
                 it will be passed up the call stack */
            }
        }
    }

    private native void start0();    public final synchronized void setName(String name){
        checkAccess();
        if(name==null){
            throw new NullPointerException("name cannot be null");
        }
        this.name=name;
        if(threadStatus!=0){
            setNativeName(name);
        }
    }

    @Override
    public void run(){
        if(target!=null){
            target.run();
        }
    }

    private void exit(){
        if(group!=null){
            group.threadTerminated(this);
            group=null;
        }
        /** Aggressively null out all reference fields: see bug 4006245 */
        target=null;
        /** Speed the release of some of these resources */
        threadLocals=null;
        inheritableThreadLocals=null;
        inheritedAccessControlContext=null;
        blocker=null;
        uncaughtExceptionHandler=null;
    }    public final String getName(){
        return name;
    }

    @Deprecated
    public final void stop(){
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            checkAccess();
            if(this!=Thread.currentThread()){
                security.checkPermission(SecurityConstants.STOP_THREAD_PERMISSION);
            }
        }
        // A zero status value corresponds to "NEW", it can't change to
        // not-NEW because we hold the lock.
        if(threadStatus!=0){
            resume(); // Wake up thread if it was suspended; no-op otherwise
        }
        // The VM can handle all thread states
        stop0(new ThreadDeath());
    }

    @Deprecated
    public final void resume(){
        checkAccess();
        resume0();
    }    public final ThreadGroup getThreadGroup(){
        return group;
    }

    private native void resume0();

    private native void stop0(Object o);

    @Deprecated
    public final synchronized void stop(Throwable obj){
        throw new UnsupportedOperationException();
    }

    public void interrupt(){
        if(this!=Thread.currentThread())
            checkAccess();
        synchronized(blockerLock){
            Interruptible b=blocker;
            if(b!=null){
                interrupt0();           // Just to set the interrupt flag
                b.interrupt(this);
                return;
            }
        }
        interrupt0();
    }

    private native void interrupt0();

    public boolean isInterrupted(){
        return isInterrupted(false);
    }

    private native boolean isInterrupted(boolean ClearInterrupted);

    @Deprecated
    public void destroy(){
        throw new NoSuchMethodError();
    }

    @Deprecated
    public final void suspend(){
        checkAccess();
        suspend0();
    }

    private native void suspend0();

    @Deprecated
    public native int countStackFrames();    public final void checkAccess(){
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            security.checkAccess(this);
        }
    }

    public final synchronized void join(long millis,int nanos)
            throws InterruptedException{
        if(millis<0){
            throw new IllegalArgumentException("timeout value is negative");
        }
        if(nanos<0||nanos>999999){
            throw new IllegalArgumentException(
                    "nanosecond timeout value out of range");
        }
        if(nanos>=500000||(nanos!=0&&millis==0)){
            millis++;
        }
        join(millis);
    }

    public final synchronized void join(long millis)
            throws InterruptedException{
        long base=System.currentTimeMillis();
        long now=0;
        if(millis<0){
            throw new IllegalArgumentException("timeout value is negative");
        }
        if(millis==0){
            while(isAlive()){
                wait(0);
            }
        }else{
            while(isAlive()){
                long delay=millis-now;
                if(delay<=0){
                    break;
                }
                wait(delay);
                now=System.currentTimeMillis()-base;
            }
        }
    }    public String toString(){
        ThreadGroup group=getThreadGroup();
        if(group!=null){
            return "Thread["+getName()+","+getPriority()+","+
                    group.getName()+"]";
        }else{
            return "Thread["+getName()+","+getPriority()+","+
                    ""+"]";
        }
    }

    public final native boolean isAlive();

    public final void join() throws InterruptedException{
        join(0);
    }

    public final boolean isDaemon(){
        return daemon;
    }

    public final void setDaemon(boolean on){
        checkAccess();
        if(isAlive()){
            throw new IllegalThreadStateException();
        }
        daemon=on;
    }

    @CallerSensitive
    public ClassLoader getContextClassLoader(){
        if(contextClassLoader==null)
            return null;
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            ClassLoader.checkClassLoaderPermission(contextClassLoader,
                    Reflection.getCallerClass());
        }
        return contextClassLoader;
    }

    public void setContextClassLoader(ClassLoader cl){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        contextClassLoader=cl;
    }

    public StackTraceElement[] getStackTrace(){
        if(this!=Thread.currentThread()){
            // check for getStackTrace permission
            SecurityManager security=System.getSecurityManager();
            if(security!=null){
                security.checkPermission(
                        SecurityConstants.GET_STACK_TRACE_PERMISSION);
            }
            // optimization so we do not call into the vm for threads that
            // have not yet started or have terminated
            if(!isAlive()){
                return EMPTY_STACK_TRACE;
            }
            StackTraceElement[][] stackTraceArray=dumpThreads(new Thread[]{this});
            StackTraceElement[] stackTrace=stackTraceArray[0];
            // a thread that was alive during the previous isAlive call may have
            // since terminated, therefore not having a stacktrace.
            if(stackTrace==null){
                stackTrace=EMPTY_STACK_TRACE;
            }
            return stackTrace;
        }else{
            // Don't need JVM help for current thread
            return (new Exception()).getStackTrace();
        }
    }

    public long getId(){
        return tid;
    }

    public State getState(){
        // get current thread state
        return sun.misc.VM.toThreadState(threadStatus);
    }

    private void dispatchUncaughtException(Throwable e){
        getUncaughtExceptionHandler().uncaughtException(this,e);
    }

    public UncaughtExceptionHandler getUncaughtExceptionHandler(){
        return uncaughtExceptionHandler!=null?
                uncaughtExceptionHandler:group;
    }

    public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh){
        checkAccess();
        uncaughtExceptionHandler=eh;
    }

    public enum State{
        NEW,
        RUNNABLE,
        BLOCKED,
        WAITING,
        TIMED_WAITING,
        TERMINATED;
    }

    @FunctionalInterface
    public interface UncaughtExceptionHandler{
        void uncaughtException(Thread t,Throwable e);
    }

    private static class Caches{
        static final ConcurrentMap<WeakClassKey,Boolean> subclassAudits=
                new ConcurrentHashMap<>();
        static final ReferenceQueue<Class<?>> subclassAuditsQueue=
                new ReferenceQueue<>();
    }
    // Added in JSR-166

    static class WeakClassKey extends WeakReference<Class<?>>{
        private final int hash;

        WeakClassKey(Class<?> cl,ReferenceQueue<Class<?>> refQueue){
            super(cl,refQueue);
            hash=System.identityHashCode(cl);
        }

        @Override
        public int hashCode(){
            return hash;
        }

        @Override
        public boolean equals(Object obj){
            if(obj==this)
                return true;
            if(obj instanceof WeakClassKey){
                Object referent=get();
                return (referent!=null)&&
                        (referent==((WeakClassKey)obj).get());
            }else{
                return false;
            }
        }
    }

















    private native void setPriority0(int newPriority);

    private native void setNativeName(String name);
}
