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

import java.util.concurrent.locks.LockSupport;

public class FutureTask<V> implements RunnableFuture<V>{
    private static final int NEW=0;
    private static final int COMPLETING=1;
    private static final int NORMAL=2;
    private static final int EXCEPTIONAL=3;
    private static final int CANCELLED=4;
    private static final int INTERRUPTING=5;
    private static final int INTERRUPTED=6;
    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    private static final long runnerOffset;
    private static final long waitersOffset;

    static{
        try{
            UNSAFE=sun.misc.Unsafe.getUnsafe();
            Class<?> k=FutureTask.class;
            stateOffset=UNSAFE.objectFieldOffset
                    (k.getDeclaredField("state"));
            runnerOffset=UNSAFE.objectFieldOffset
                    (k.getDeclaredField("runner"));
            waitersOffset=UNSAFE.objectFieldOffset
                    (k.getDeclaredField("waiters"));
        }catch(Exception e){
            throw new Error(e);
        }
    }

    private volatile int state;
    private Callable<V> callable;
    private Object outcome; // non-volatile, protected by state reads/writes
    private volatile Thread runner;
    private volatile WaitNode waiters;

    public FutureTask(Callable<V> callable){
        if(callable==null)
            throw new NullPointerException();
        this.callable=callable;
        this.state=NEW;       // ensure visibility of callable
    }

    public FutureTask(Runnable runnable,V result){
        this.callable=Executors.callable(runnable,result);
        this.state=NEW;       // ensure visibility of callable
    }

    public boolean cancel(boolean mayInterruptIfRunning){
        if(!(state==NEW&&
                UNSAFE.compareAndSwapInt(this,stateOffset,NEW,
                        mayInterruptIfRunning?INTERRUPTING:CANCELLED)))
            return false;
        try{    // in case call to interrupt throws exception
            if(mayInterruptIfRunning){
                try{
                    Thread t=runner;
                    if(t!=null)
                        t.interrupt();
                }finally{ // final state
                    UNSAFE.putOrderedInt(this,stateOffset,INTERRUPTED);
                }
            }
        }finally{
            finishCompletion();
        }
        return true;
    }

    public boolean isCancelled(){
        return state>=CANCELLED;
    }

    public boolean isDone(){
        return state!=NEW;
    }

    public V get() throws InterruptedException, ExecutionException{
        int s=state;
        if(s<=COMPLETING)
            s=awaitDone(false,0L);
        return report(s);
    }

    @SuppressWarnings("unchecked")
    private V report(int s) throws ExecutionException{
        Object x=outcome;
        if(s==NORMAL)
            return (V)x;
        if(s>=CANCELLED)
            throw new CancellationException();
        throw new ExecutionException((Throwable)x);
    }

    public V get(long timeout,TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException{
        if(unit==null)
            throw new NullPointerException();
        int s=state;
        if(s<=COMPLETING&&
                (s=awaitDone(true,unit.toNanos(timeout)))<=COMPLETING)
            throw new TimeoutException();
        return report(s);
    }

    private int awaitDone(boolean timed,long nanos)
            throws InterruptedException{
        final long deadline=timed?System.nanoTime()+nanos:0L;
        WaitNode q=null;
        boolean queued=false;
        for(;;){
            if(Thread.interrupted()){
                removeWaiter(q);
                throw new InterruptedException();
            }
            int s=state;
            if(s>COMPLETING){
                if(q!=null)
                    q.thread=null;
                return s;
            }else if(s==COMPLETING) // cannot time out yet
                Thread.yield();
            else if(q==null)
                q=new WaitNode();
            else if(!queued)
                queued=UNSAFE.compareAndSwapObject(this,waitersOffset,
                        q.next=waiters,q);
            else if(timed){
                nanos=deadline-System.nanoTime();
                if(nanos<=0L){
                    removeWaiter(q);
                    return state;
                }
                LockSupport.parkNanos(this,nanos);
            }else
                LockSupport.park(this);
        }
    }

    private void removeWaiter(WaitNode node){
        if(node!=null){
            node.thread=null;
            retry:
            for(;;){          // restart on removeWaiter race
                for(WaitNode pred=null, q=waiters, s;q!=null;q=s){
                    s=q.next;
                    if(q.thread!=null)
                        pred=q;
                    else if(pred!=null){
                        pred.next=s;
                        if(pred.thread==null) // check for race
                            continue retry;
                    }else if(!UNSAFE.compareAndSwapObject(this,waitersOffset,
                            q,s))
                        continue retry;
                }
                break;
            }
        }
    }

    private void finishCompletion(){
        // assert state > COMPLETING;
        for(WaitNode q;(q=waiters)!=null;){
            if(UNSAFE.compareAndSwapObject(this,waitersOffset,q,null)){
                for(;;){
                    Thread t=q.thread;
                    if(t!=null){
                        q.thread=null;
                        LockSupport.unpark(t);
                    }
                    WaitNode next=q.next;
                    if(next==null)
                        break;
                    q.next=null; // unlink to help gc
                    q=next;
                }
                break;
            }
        }
        done();
        callable=null;        // to reduce footprint
    }

    protected void done(){
    }

    protected void set(V v){
        if(UNSAFE.compareAndSwapInt(this,stateOffset,NEW,COMPLETING)){
            outcome=v;
            UNSAFE.putOrderedInt(this,stateOffset,NORMAL); // final state
            finishCompletion();
        }
    }

    public void run(){
        if(state!=NEW||
                !UNSAFE.compareAndSwapObject(this,runnerOffset,
                        null,Thread.currentThread()))
            return;
        try{
            Callable<V> c=callable;
            if(c!=null&&state==NEW){
                V result;
                boolean ran;
                try{
                    result=c.call();
                    ran=true;
                }catch(Throwable ex){
                    result=null;
                    ran=false;
                    setException(ex);
                }
                if(ran)
                    set(result);
            }
        }finally{
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner=null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            int s=state;
            if(s>=INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }

    protected boolean runAndReset(){
        if(state!=NEW||
                !UNSAFE.compareAndSwapObject(this,runnerOffset,
                        null,Thread.currentThread()))
            return false;
        boolean ran=false;
        int s=state;
        try{
            Callable<V> c=callable;
            if(c!=null&&s==NEW){
                try{
                    c.call(); // don't set result
                    ran=true;
                }catch(Throwable ex){
                    setException(ex);
                }
            }
        }finally{
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner=null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            s=state;
            if(s>=INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
        return ran&&s==NEW;
    }

    protected void setException(Throwable t){
        if(UNSAFE.compareAndSwapInt(this,stateOffset,NEW,COMPLETING)){
            outcome=t;
            UNSAFE.putOrderedInt(this,stateOffset,EXCEPTIONAL); // final state
            finishCompletion();
        }
    }

    private void handlePossibleCancellationInterrupt(int s){
        // It is possible for our interrupter to stall before getting a
        // chance to interrupt us.  Let's spin-wait patiently.
        if(s==INTERRUPTING)
            while(state==INTERRUPTING)
                Thread.yield(); // wait out pending interrupt
        // assert state == INTERRUPTED;
        // We want to clear any interrupt we may have received from
        // cancel(true).  However, it is permissible to use interrupts
        // as an independent mechanism for a task to communicate with
        // its caller, and there is no way to clear only the
        // cancellation interrupt.
        //
        // Thread.interrupted();
    }

    static final class WaitNode{
        volatile Thread thread;
        volatile WaitNode next;

        WaitNode(){
            thread=Thread.currentThread();
        }
    }
}
