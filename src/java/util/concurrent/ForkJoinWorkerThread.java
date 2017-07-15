/**
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
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
/**
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent;

import java.security.AccessControlContext;
import java.security.ProtectionDomain;

public class ForkJoinWorkerThread extends Thread{
    // Set up to allow setting thread fields in constructor
    private static final sun.misc.Unsafe U;
    private static final long THREADLOCALS;
    private static final long INHERITABLETHREADLOCALS;
    private static final long INHERITEDACCESSCONTROLCONTEXT;

    static{
        try{
            U=sun.misc.Unsafe.getUnsafe();
            Class<?> tk=Thread.class;
            THREADLOCALS=U.objectFieldOffset
                    (tk.getDeclaredField("threadLocals"));
            INHERITABLETHREADLOCALS=U.objectFieldOffset
                    (tk.getDeclaredField("inheritableThreadLocals"));
            INHERITEDACCESSCONTROLCONTEXT=U.objectFieldOffset
                    (tk.getDeclaredField("inheritedAccessControlContext"));
        }catch(Exception e){
            throw new Error(e);
        }
    }

    final ForkJoinPool pool;                // the pool this thread works in
    final ForkJoinPool.WorkQueue workQueue; // work-stealing mechanics

    protected ForkJoinWorkerThread(ForkJoinPool pool){
        // Use a placeholder until a useful name can be set in registerWorker
        super("aForkJoinWorkerThread");
        this.pool=pool;
        this.workQueue=pool.registerWorker(this);
    }

    ForkJoinWorkerThread(ForkJoinPool pool,ThreadGroup threadGroup,
                         AccessControlContext acc){
        super(threadGroup,null,"aForkJoinWorkerThread");
        U.putOrderedObject(this,INHERITEDACCESSCONTROLCONTEXT,acc);
        eraseThreadLocals(); // clear before registering
        this.pool=pool;
        this.workQueue=pool.registerWorker(this);
    }

    final void eraseThreadLocals(){
        U.putObject(this,THREADLOCALS,null);
        U.putObject(this,INHERITABLETHREADLOCALS,null);
    }

    public ForkJoinPool getPool(){
        return pool;
    }

    public int getPoolIndex(){
        return workQueue.getPoolIndex();
    }

    public void run(){
        if(workQueue.array==null){ // only run once
            Throwable exception=null;
            try{
                onStart();
                pool.runWorker(workQueue);
            }catch(Throwable ex){
                exception=ex;
            }finally{
                try{
                    onTermination(exception);
                }catch(Throwable ex){
                    if(exception==null)
                        exception=ex;
                }finally{
                    pool.deregisterWorker(this,exception);
                }
            }
        }
    }

    protected void onStart(){
    }

    protected void onTermination(Throwable exception){
    }

    void afterTopLevelExec(){
    }

    static final class InnocuousForkJoinWorkerThread extends ForkJoinWorkerThread{
        private static final ThreadGroup innocuousThreadGroup=
                createThreadGroup();
        private static final AccessControlContext INNOCUOUS_ACC=
                new AccessControlContext(
                        new ProtectionDomain[]{
                                new ProtectionDomain(null,null)
                        });

        InnocuousForkJoinWorkerThread(ForkJoinPool pool){
            super(pool,innocuousThreadGroup,INNOCUOUS_ACC);
        }

        private static ThreadGroup createThreadGroup(){
            try{
                sun.misc.Unsafe u=sun.misc.Unsafe.getUnsafe();
                Class<?> tk=Thread.class;
                Class<?> gk=ThreadGroup.class;
                long tg=u.objectFieldOffset(tk.getDeclaredField("group"));
                long gp=u.objectFieldOffset(gk.getDeclaredField("parent"));
                ThreadGroup group=(ThreadGroup)
                        u.getObject(Thread.currentThread(),tg);
                while(group!=null){
                    ThreadGroup parent=(ThreadGroup)u.getObject(group,gp);
                    if(parent==null)
                        return new ThreadGroup(group,
                                "InnocuousForkJoinWorkerThreadGroup");
                    group=parent;
                }
            }catch(Exception e){
                throw new Error(e);
            }
            // fall through if null as cannot-happen safeguard
            throw new Error("Cannot create ThreadGroup");
        }

        @Override
            // to erase ThreadLocals
        void afterTopLevelExec(){
            eraseThreadLocals();
        }

        @Override // to always report system loader
        public ClassLoader getContextClassLoader(){
            return ClassLoader.getSystemClassLoader();
        }

        @Override // to silently fail
        public void setUncaughtExceptionHandler(UncaughtExceptionHandler x){
        }

        @Override // paranoically
        public void setContextClassLoader(ClassLoader cl){
            throw new SecurityException("setContextClassLoader");
        }
    }
}
