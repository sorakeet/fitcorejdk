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

public class ReentrantReadWriteLock
        implements ReadWriteLock, java.io.Serializable{
    private static final long serialVersionUID=-6992448646407690164L;
    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long TID_OFFSET;

    static{
        try{
            UNSAFE=sun.misc.Unsafe.getUnsafe();
            Class<?> tk=Thread.class;
            TID_OFFSET=UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("tid"));
        }catch(Exception e){
            throw new Error(e);
        }
    }

    final Sync sync;
    private final ReadLock readerLock;
    private final WriteLock writerLock;

    public ReentrantReadWriteLock(){
        this(false);
    }

    public ReentrantReadWriteLock(boolean fair){
        sync=fair?new FairSync():new NonfairSync();
        readerLock=new ReadLock(this);
        writerLock=new WriteLock(this);
    }

    static final long getThreadId(Thread thread){
        return UNSAFE.getLongVolatile(thread,TID_OFFSET);
    }

    public ReadLock readLock(){
        return readerLock;
    }

    public WriteLock writeLock(){
        return writerLock;
    }

    public final boolean isFair(){
        return sync instanceof FairSync;
    }
    // Instrumentation and status

    protected Thread getOwner(){
        return sync.getOwner();
    }

    public int getReadLockCount(){
        return sync.getReadLockCount();
    }

    public boolean isWriteLocked(){
        return sync.isWriteLocked();
    }

    public boolean isWriteLockedByCurrentThread(){
        return sync.isHeldExclusively();
    }

    public int getWriteHoldCount(){
        return sync.getWriteHoldCount();
    }

    public int getReadHoldCount(){
        return sync.getReadHoldCount();
    }

    protected Collection<Thread> getQueuedWriterThreads(){
        return sync.getExclusiveQueuedThreads();
    }

    protected Collection<Thread> getQueuedReaderThreads(){
        return sync.getSharedQueuedThreads();
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
        int c=sync.getCount();
        int w=Sync.exclusiveCount(c);
        int r=Sync.sharedCount(c);
        return super.toString()+
                "[Write locks = "+w+", Read locks = "+r+"]";
    }

    abstract static class Sync extends AbstractQueuedSynchronizer{
        static final int SHARED_SHIFT=16;
        static final int SHARED_UNIT=(1<<SHARED_SHIFT);
        static final int MAX_COUNT=(1<<SHARED_SHIFT)-1;
        static final int EXCLUSIVE_MASK=(1<<SHARED_SHIFT)-1;
        private static final long serialVersionUID=6317671515068378041L;
        private transient ThreadLocalHoldCounter readHolds;
        private transient HoldCounter cachedHoldCounter;
        private transient Thread firstReader=null;
        private transient int firstReaderHoldCount;

        Sync(){
            readHolds=new ThreadLocalHoldCounter();
            setState(getState()); // ensures visibility of readHolds
        }

        protected final boolean tryAcquire(int acquires){
            /**
             * Walkthrough:
             * 1. If read count nonzero or write count nonzero
             *    and owner is a different thread, fail.
             * 2. If count would saturate, fail. (This can only
             *    happen if count is already nonzero.)
             * 3. Otherwise, this thread is eligible for lock if
             *    it is either a reentrant acquire or
             *    queue policy allows it. If so, update state
             *    and set owner.
             */
            Thread current=Thread.currentThread();
            int c=getState();
            int w=exclusiveCount(c);
            if(c!=0){
                // (Note: if c != 0 and w == 0 then shared count != 0)
                if(w==0||current!=getExclusiveOwnerThread())
                    return false;
                if(w+exclusiveCount(acquires)>MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                // Reentrant acquire
                setState(c+acquires);
                return true;
            }
            if(writerShouldBlock()||
                    !compareAndSetState(c,c+acquires))
                return false;
            setExclusiveOwnerThread(current);
            return true;
        }

        abstract boolean writerShouldBlock();

        protected final boolean tryRelease(int releases){
            if(!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int nextc=getState()-releases;
            boolean free=exclusiveCount(nextc)==0;
            if(free)
                setExclusiveOwnerThread(null);
            setState(nextc);
            return free;
        }

        protected final int tryAcquireShared(int unused){
            /**
             * Walkthrough:
             * 1. If write lock held by another thread, fail.
             * 2. Otherwise, this thread is eligible for
             *    lock wrt state, so ask if it should block
             *    because of queue policy. If not, try
             *    to grant by CASing state and updating count.
             *    Note that step does not check for reentrant
             *    acquires, which is postponed to full version
             *    to avoid having to check hold count in
             *    the more typical non-reentrant case.
             * 3. If step 2 fails either because thread
             *    apparently not eligible or CAS fails or count
             *    saturated, chain to version with full retry loop.
             */
            Thread current=Thread.currentThread();
            int c=getState();
            if(exclusiveCount(c)!=0&&
                    getExclusiveOwnerThread()!=current)
                return -1;
            int r=sharedCount(c);
            if(!readerShouldBlock()&&
                    r<MAX_COUNT&&
                    compareAndSetState(c,c+SHARED_UNIT)){
                if(r==0){
                    firstReader=current;
                    firstReaderHoldCount=1;
                }else if(firstReader==current){
                    firstReaderHoldCount++;
                }else{
                    HoldCounter rh=cachedHoldCounter;
                    if(rh==null||rh.tid!=getThreadId(current))
                        cachedHoldCounter=rh=readHolds.get();
                    else if(rh.count==0)
                        readHolds.set(rh);
                    rh.count++;
                }
                return 1;
            }
            return fullTryAcquireShared(current);
        }

        static int sharedCount(int c){
            return c>>>SHARED_SHIFT;
        }

        abstract boolean readerShouldBlock();

        protected final boolean tryReleaseShared(int unused){
            Thread current=Thread.currentThread();
            if(firstReader==current){
                // assert firstReaderHoldCount > 0;
                if(firstReaderHoldCount==1)
                    firstReader=null;
                else
                    firstReaderHoldCount--;
            }else{
                HoldCounter rh=cachedHoldCounter;
                if(rh==null||rh.tid!=getThreadId(current))
                    rh=readHolds.get();
                int count=rh.count;
                if(count<=1){
                    readHolds.remove();
                    if(count<=0)
                        throw unmatchedUnlockException();
                }
                --rh.count;
            }
            for(;;){
                int c=getState();
                int nextc=c-SHARED_UNIT;
                if(compareAndSetState(c,nextc))
                    // Releasing the read lock has no effect on readers,
                    // but it may allow waiting writers to proceed if
                    // both read and write locks are now free.
                    return nextc==0;
            }
        }

        private IllegalMonitorStateException unmatchedUnlockException(){
            return new IllegalMonitorStateException(
                    "attempt to unlock read lock, not locked by current thread");
        }

        protected final boolean isHeldExclusively(){
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getExclusiveOwnerThread()==Thread.currentThread();
        }

        final int fullTryAcquireShared(Thread current){
            /**
             * This code is in part redundant with that in
             * tryAcquireShared but is simpler overall by not
             * complicating tryAcquireShared with interactions between
             * retries and lazily reading hold counts.
             */
            HoldCounter rh=null;
            for(;;){
                int c=getState();
                if(exclusiveCount(c)!=0){
                    if(getExclusiveOwnerThread()!=current)
                        return -1;
                    // else we hold the exclusive lock; blocking here
                    // would cause deadlock.
                }else if(readerShouldBlock()){
                    // Make sure we're not acquiring read lock reentrantly
                    if(firstReader==current){
                        // assert firstReaderHoldCount > 0;
                    }else{
                        if(rh==null){
                            rh=cachedHoldCounter;
                            if(rh==null||rh.tid!=getThreadId(current)){
                                rh=readHolds.get();
                                if(rh.count==0)
                                    readHolds.remove();
                            }
                        }
                        if(rh.count==0)
                            return -1;
                    }
                }
                if(sharedCount(c)==MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                if(compareAndSetState(c,c+SHARED_UNIT)){
                    if(sharedCount(c)==0){
                        firstReader=current;
                        firstReaderHoldCount=1;
                    }else if(firstReader==current){
                        firstReaderHoldCount++;
                    }else{
                        if(rh==null)
                            rh=cachedHoldCounter;
                        if(rh==null||rh.tid!=getThreadId(current))
                            rh=readHolds.get();
                        else if(rh.count==0)
                            readHolds.set(rh);
                        rh.count++;
                        cachedHoldCounter=rh; // cache for release
                    }
                    return 1;
                }
            }
        }

        static int exclusiveCount(int c){
            return c&EXCLUSIVE_MASK;
        }

        final boolean tryWriteLock(){
            Thread current=Thread.currentThread();
            int c=getState();
            if(c!=0){
                int w=exclusiveCount(c);
                if(w==0||current!=getExclusiveOwnerThread())
                    return false;
                if(w==MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
            }
            if(!compareAndSetState(c,c+1))
                return false;
            setExclusiveOwnerThread(current);
            return true;
        }

        final boolean tryReadLock(){
            Thread current=Thread.currentThread();
            for(;;){
                int c=getState();
                if(exclusiveCount(c)!=0&&
                        getExclusiveOwnerThread()!=current)
                    return false;
                int r=sharedCount(c);
                if(r==MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                if(compareAndSetState(c,c+SHARED_UNIT)){
                    if(r==0){
                        firstReader=current;
                        firstReaderHoldCount=1;
                    }else if(firstReader==current){
                        firstReaderHoldCount++;
                    }else{
                        HoldCounter rh=cachedHoldCounter;
                        if(rh==null||rh.tid!=getThreadId(current))
                            cachedHoldCounter=rh=readHolds.get();
                        else if(rh.count==0)
                            readHolds.set(rh);
                        rh.count++;
                    }
                    return true;
                }
            }
        }

        final ConditionObject newCondition(){
            return new ConditionObject();
        }

        final Thread getOwner(){
            // Must read state before owner to ensure memory consistency
            return ((exclusiveCount(getState())==0)?
                    null:
                    getExclusiveOwnerThread());
        }
        // Methods relayed to outer class

        final boolean isWriteLocked(){
            return exclusiveCount(getState())!=0;
        }

        final int getWriteHoldCount(){
            return isHeldExclusively()?exclusiveCount(getState()):0;
        }

        final int getReadHoldCount(){
            if(getReadLockCount()==0)
                return 0;
            Thread current=Thread.currentThread();
            if(firstReader==current)
                return firstReaderHoldCount;
            HoldCounter rh=cachedHoldCounter;
            if(rh!=null&&rh.tid==getThreadId(current))
                return rh.count;
            int count=readHolds.get().count;
            if(count==0) readHolds.remove();
            return count;
        }

        final int getReadLockCount(){
            return sharedCount(getState());
        }

        private void readObject(java.io.ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException{
            s.defaultReadObject();
            readHolds=new ThreadLocalHoldCounter();
            setState(0); // reset to unlocked state
        }

        final int getCount(){
            return getState();
        }

        static final class HoldCounter{
            // Use id, not reference, to avoid garbage retention
            final long tid=getThreadId(Thread.currentThread());
            int count=0;
        }

        static final class ThreadLocalHoldCounter
                extends ThreadLocal<HoldCounter>{
            public HoldCounter initialValue(){
                return new HoldCounter();
            }
        }
    }

    static final class NonfairSync extends Sync{
        private static final long serialVersionUID=-8159625535654395037L;

        final boolean readerShouldBlock(){
            /** As a heuristic to avoid indefinite writer starvation,
             * block if the thread that momentarily appears to be head
             * of queue, if one exists, is a waiting writer.  This is
             * only a probabilistic effect since a new reader will not
             * block if there is a waiting writer behind other enabled
             * readers that have not yet drained from the queue.
             */
            return apparentlyFirstQueuedIsExclusive();
        }

        final boolean writerShouldBlock(){
            return false; // writers can always barge
        }
    }

    static final class FairSync extends Sync{
        private static final long serialVersionUID=-2274990926593161451L;

        final boolean writerShouldBlock(){
            return hasQueuedPredecessors();
        }

        final boolean readerShouldBlock(){
            return hasQueuedPredecessors();
        }
    }

    public static class ReadLock implements Lock, java.io.Serializable{
        private static final long serialVersionUID=-5992448646407690164L;
        private final Sync sync;

        protected ReadLock(ReentrantReadWriteLock lock){
            sync=lock.sync;
        }

        public void lock(){
            sync.acquireShared(1);
        }

        public String toString(){
            int r=sync.getReadLockCount();
            return super.toString()+
                    "[Read locks = "+r+"]";
        }

        public void lockInterruptibly() throws InterruptedException{
            sync.acquireSharedInterruptibly(1);
        }

        public boolean tryLock(){
            return sync.tryReadLock();
        }

        public boolean tryLock(long timeout,TimeUnit unit)
                throws InterruptedException{
            return sync.tryAcquireSharedNanos(1,unit.toNanos(timeout));
        }

        public void unlock(){
            sync.releaseShared(1);
        }

        public Condition newCondition(){
            throw new UnsupportedOperationException();
        }
    }

    public static class WriteLock implements Lock, java.io.Serializable{
        private static final long serialVersionUID=-4992448646407690164L;
        private final Sync sync;

        protected WriteLock(ReentrantReadWriteLock lock){
            sync=lock.sync;
        }

        public boolean isHeldByCurrentThread(){
            return sync.isHeldExclusively();
        }

        public int getHoldCount(){
            return sync.getWriteHoldCount();
        }        public void lock(){
            sync.acquire(1);
        }



        public void lockInterruptibly() throws InterruptedException{
            sync.acquireInterruptibly(1);
        }

        public boolean tryLock(){
            return sync.tryWriteLock();
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

        public String toString(){
            Thread o=sync.getOwner();
            return super.toString()+((o==null)?
                    "[Unlocked]":
                    "[Locked by thread "+o.getName()+"]");
        }
    }
}
