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
package java.util.concurrent.locks;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class ReentrantLock implements Lock, java.io.Serializable{
    private static final long serialVersionUID=7373984872572414699L;
    private final Sync sync;

    public ReentrantLock(){
        sync=new NonfairSync();
    }

    public ReentrantLock(boolean fair){
        sync=fair?new FairSync():new NonfairSync();
    }

    public void lock(){
        sync.lock();
    }

    public void lockInterruptibly() throws InterruptedException{
        sync.acquireInterruptibly(1);
    }

    public boolean tryLock(){
        return sync.nonfairTryAcquire(1);
    }

    public boolean tryLock(long timeout,TimeUnit unit)
            throws InterruptedException{
        return sync.tryAcquireNanos(1,unit.toNanos(timeout));
    }

    public void unlock(){
        sync.release(1);
    }

    public Condition newCondition(){
        return sync.newCondition();
    }

    public int getHoldCount(){
        return sync.getHoldCount();
    }

    public boolean isHeldByCurrentThread(){
        return sync.isHeldExclusively();
    }

    public boolean isLocked(){
        return sync.isLocked();
    }

    public final boolean isFair(){
        return sync instanceof FairSync;
    }

    protected Thread getOwner(){
        return sync.getOwner();
    }

    public final boolean hasQueuedThreads(){
        return sync.hasQueuedThreads();
    }

    public final boolean hasQueuedThread(Thread thread){
        return sync.isQueued(thread);
    }

    public final int getQueueLength(){
        return sync.getQueueLength();
    }

    protected Collection<Thread> getQueuedThreads(){
        return sync.getQueuedThreads();
    }

    public boolean hasWaiters(Condition condition){
        if(condition==null)
            throw new NullPointerException();
        if(!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    public int getWaitQueueLength(Condition condition){
        if(condition==null)
            throw new NullPointerException();
        if(!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    protected Collection<Thread> getWaitingThreads(Condition condition){
        if(condition==null)
            throw new NullPointerException();
        if(!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    public String toString(){
        Thread o=sync.getOwner();
        return super.toString()+((o==null)?
                "[Unlocked]":
                "[Locked by thread "+o.getName()+"]");
    }

    abstract static class Sync extends AbstractQueuedSynchronizer{
        private static final long serialVersionUID=-5179523762034025860L;

        abstract void lock();

        final boolean nonfairTryAcquire(int acquires){
            final Thread current=Thread.currentThread();
            int c=getState();
            if(c==0){
                if(compareAndSetState(0,acquires)){
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }else if(current==getExclusiveOwnerThread()){
                int nextc=c+acquires;
                if(nextc<0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }

        final ConditionObject newCondition(){
            return new ConditionObject();
        }

        final Thread getOwner(){
            return getState()==0?null:getExclusiveOwnerThread();
        }        protected final boolean tryRelease(int releases){
            int c=getState()-releases;
            if(Thread.currentThread()!=getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free=false;
            if(c==0){
                free=true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

        final int getHoldCount(){
            return isHeldExclusively()?getState():0;
        }

        final boolean isLocked(){
            return getState()!=0;
        }        protected final boolean isHeldExclusively(){
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getExclusiveOwnerThread()==Thread.currentThread();
        }

        private void readObject(java.io.ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException{
            s.defaultReadObject();
            setState(0); // reset to unlocked state
        }
        // Methods relayed from outer class




    }

    static final class NonfairSync extends Sync{
        private static final long serialVersionUID=7316153563782823691L;

        final void lock(){
            if(compareAndSetState(0,1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        protected final boolean tryAcquire(int acquires){
            return nonfairTryAcquire(acquires);
        }
    }

    static final class FairSync extends Sync{
        private static final long serialVersionUID=-3000897897090466540L;

        final void lock(){
            acquire(1);
        }

        protected final boolean tryAcquire(int acquires){
            final Thread current=Thread.currentThread();
            int c=getState();
            if(c==0){
                if(!hasQueuedPredecessors()&&
                        compareAndSetState(0,acquires)){
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }else if(current==getExclusiveOwnerThread()){
                int nextc=c+acquires;
                if(nextc<0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }
}
