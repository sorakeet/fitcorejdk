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

import sun.misc.Unsafe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class AbstractQueuedLongSynchronizer
        extends AbstractOwnableSynchronizer
        implements java.io.Serializable{
    // Queuing utilities
    static final long spinForTimeoutThreshold=1000L;
    private static final long serialVersionUID=7373984972572414692L;
    private static final Unsafe unsafe=Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static{
        try{
            stateOffset=unsafe.objectFieldOffset
                    (AbstractQueuedLongSynchronizer.class.getDeclaredField("state"));
            headOffset=unsafe.objectFieldOffset
                    (AbstractQueuedLongSynchronizer.class.getDeclaredField("head"));
            tailOffset=unsafe.objectFieldOffset
                    (AbstractQueuedLongSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset=unsafe.objectFieldOffset
                    (Node.class.getDeclaredField("waitStatus"));
            nextOffset=unsafe.objectFieldOffset
                    (Node.class.getDeclaredField("next"));
        }catch(Exception ex){
            throw new Error(ex);
        }
    }

    private transient volatile Node head;
    private transient volatile Node tail;
    private volatile long state;

    protected AbstractQueuedLongSynchronizer(){
    }

    protected final boolean compareAndSetState(long expect,long update){
        // See below for intrinsics setup to support this
        return unsafe.compareAndSwapLong(this,stateOffset,expect,update);
    }

    private void setHeadAndPropagate(Node node,long propagate){
        Node h=head; // Record old head for check below
        setHead(node);
        /**
         * Try to signal next queued node if:
         *   Propagation was indicated by caller,
         *     or was recorded (as h.waitStatus either before
         *     or after setHead) by a previous operation
         *     (note: this uses sign-check of waitStatus because
         *      PROPAGATE status may transition to SIGNAL.)
         * and
         *   The next node is waiting in shared mode,
         *     or we don't know, because it appears null
         *
         * The conservatism in both of these checks may cause
         * unnecessary wake-ups, but only when there are multiple
         * racing acquires/releases, so most need signals now or soon
         * anyway.
         */
        if(propagate>0||h==null||h.waitStatus<0||
                (h=head)==null||h.waitStatus<0){
            Node s=node.next;
            if(s==null||s.isShared())
                doReleaseShared();
        }
    }

    private void doAcquireShared(long arg){
        final Node node=addWaiter(Node.SHARED);
        boolean failed=true;
        try{
            boolean interrupted=false;
            for(;;){
                final Node p=node.predecessor();
                if(p==head){
                    long r=tryAcquireShared(arg);
                    if(r>=0){
                        setHeadAndPropagate(node,r);
                        p.next=null; // help GC
                        if(interrupted)
                            selfInterrupt();
                        failed=false;
                        return;
                    }
                }
                if(shouldParkAfterFailedAcquire(p,node)&&
                        parkAndCheckInterrupt())
                    interrupted=true;
            }
        }finally{
            if(failed)
                cancelAcquire(node);
        }
    }
    // Utilities for various versions of acquire

    private void doAcquireSharedInterruptibly(long arg)
            throws InterruptedException{
        final Node node=addWaiter(Node.SHARED);
        boolean failed=true;
        try{
            for(;;){
                final Node p=node.predecessor();
                if(p==head){
                    long r=tryAcquireShared(arg);
                    if(r>=0){
                        setHeadAndPropagate(node,r);
                        p.next=null; // help GC
                        failed=false;
                        return;
                    }
                }
                if(shouldParkAfterFailedAcquire(p,node)&&
                        parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        }finally{
            if(failed)
                cancelAcquire(node);
        }
    }

    private boolean doAcquireSharedNanos(long arg,long nanosTimeout)
            throws InterruptedException{
        if(nanosTimeout<=0L)
            return false;
        final long deadline=System.nanoTime()+nanosTimeout;
        final Node node=addWaiter(Node.SHARED);
        boolean failed=true;
        try{
            for(;;){
                final Node p=node.predecessor();
                if(p==head){
                    long r=tryAcquireShared(arg);
                    if(r>=0){
                        setHeadAndPropagate(node,r);
                        p.next=null; // help GC
                        failed=false;
                        return true;
                    }
                }
                nanosTimeout=deadline-System.nanoTime();
                if(nanosTimeout<=0L)
                    return false;
                if(shouldParkAfterFailedAcquire(p,node)&&
                        nanosTimeout>spinForTimeoutThreshold)
                    LockSupport.parkNanos(this,nanosTimeout);
                if(Thread.interrupted())
                    throw new InterruptedException();
            }
        }finally{
            if(failed)
                cancelAcquire(node);
        }
    }

    protected long tryAcquireShared(long arg){
        throw new UnsupportedOperationException();
    }

    protected boolean isHeldExclusively(){
        throw new UnsupportedOperationException();
    }

    public final void acquire(long arg){
        if(!tryAcquire(arg)&&
                acquireQueued(addWaiter(Node.EXCLUSIVE),arg))
            selfInterrupt();
    }

    private Node addWaiter(Node mode){
        Node node=new Node(Thread.currentThread(),mode);
        // Try the fast path of enq; backup to full enq on failure
        Node pred=tail;
        if(pred!=null){
            node.prev=pred;
            if(compareAndSetTail(pred,node)){
                pred.next=node;
                return node;
            }
        }
        enq(node);
        return node;
    }

    private Node enq(final Node node){
        for(;;){
            Node t=tail;
            if(t==null){ // Must initialize
                if(compareAndSetHead(new Node()))
                    tail=head;
            }else{
                node.prev=t;
                if(compareAndSetTail(t,node)){
                    t.next=node;
                    return t;
                }
            }
        }
    }

    private final boolean compareAndSetHead(Node update){
        return unsafe.compareAndSwapObject(this,headOffset,null,update);
    }

    static void selfInterrupt(){
        Thread.currentThread().interrupt();
    }

    final boolean acquireQueued(final Node node,long arg){
        boolean failed=true;
        try{
            boolean interrupted=false;
            for(;;){
                final Node p=node.predecessor();
                if(p==head&&tryAcquire(arg)){
                    setHead(node);
                    p.next=null; // help GC
                    failed=false;
                    return interrupted;
                }
                if(shouldParkAfterFailedAcquire(p,node)&&
                        parkAndCheckInterrupt())
                    interrupted=true;
            }
        }finally{
            if(failed)
                cancelAcquire(node);
        }
    }
    // Main exported methods

    private void setHead(Node node){
        head=node;
        node.thread=null;
        node.prev=null;
    }

    private void cancelAcquire(Node node){
        // Ignore if node doesn't exist
        if(node==null)
            return;
        node.thread=null;
        // Skip cancelled predecessors
        Node pred=node.prev;
        while(pred.waitStatus>0)
            node.prev=pred=pred.prev;
        // predNext is the apparent node to unsplice. CASes below will
        // fail if not, in which case, we lost race vs another cancel
        // or signal, so no further action is necessary.
        Node predNext=pred.next;
        // Can use unconditional write instead of CAS here.
        // After this atomic step, other Nodes can skip past us.
        // Before, we are free of interference from other threads.
        node.waitStatus=Node.CANCELLED;
        // If we are the tail, remove ourselves.
        if(node==tail&&compareAndSetTail(node,pred)){
            compareAndSetNext(pred,predNext,null);
        }else{
            // If successor needs signal, try to set pred's next-link
            // so it will get one. Otherwise wake it up to propagate.
            int ws;
            if(pred!=head&&
                    ((ws=pred.waitStatus)==Node.SIGNAL||
                            (ws<=0&&compareAndSetWaitStatus(pred,ws,Node.SIGNAL)))&&
                    pred.thread!=null){
                Node next=node.next;
                if(next!=null&&next.waitStatus<=0)
                    compareAndSetNext(pred,predNext,next);
            }else{
                unparkSuccessor(node);
            }
            node.next=node; // help GC
        }
    }

    private void unparkSuccessor(Node node){
        /**
         * If status is negative (i.e., possibly needing signal) try
         * to clear in anticipation of signalling.  It is OK if this
         * fails or if status is changed by waiting thread.
         */
        int ws=node.waitStatus;
        if(ws<0)
            compareAndSetWaitStatus(node,ws,0);
        /**
         * Thread to unpark is held in successor, which is normally
         * just the next node.  But if cancelled or apparently null,
         * traverse backwards from tail to find the actual
         * non-cancelled successor.
         */
        Node s=node.next;
        if(s==null||s.waitStatus>0){
            s=null;
            for(Node t=tail;t!=null&&t!=node;t=t.prev)
                if(t.waitStatus<=0)
                    s=t;
        }
        if(s!=null)
            LockSupport.unpark(s.thread);
    }

    private final boolean compareAndSetTail(Node expect,Node update){
        return unsafe.compareAndSwapObject(this,tailOffset,expect,update);
    }

    private static final boolean compareAndSetNext(Node node,
                                                   Node expect,
                                                   Node update){
        return unsafe.compareAndSwapObject(node,nextOffset,expect,update);
    }

    private static boolean shouldParkAfterFailedAcquire(Node pred,Node node){
        int ws=pred.waitStatus;
        if(ws==Node.SIGNAL)
        /**
         * This node has already set status asking a release
         * to signal it, so it can safely park.
         */
            return true;
        if(ws>0){
            /**
             * Predecessor was cancelled. Skip over predecessors and
             * indicate retry.
             */
            do{
                node.prev=pred=pred.prev;
            }while(pred.waitStatus>0);
            pred.next=node;
        }else{
            /**
             * waitStatus must be 0 or PROPAGATE.  Indicate that we
             * need a signal, but don't park yet.  Caller will need to
             * retry to make sure it cannot acquire before parking.
             */
            compareAndSetWaitStatus(pred,ws,Node.SIGNAL);
        }
        return false;
    }

    private static final boolean compareAndSetWaitStatus(Node node,
                                                         int expect,
                                                         int update){
        return unsafe.compareAndSwapInt(node,waitStatusOffset,
                expect,update);
    }

    private final boolean parkAndCheckInterrupt(){
        LockSupport.park(this);
        return Thread.interrupted();
    }

    protected boolean tryAcquire(long arg){
        throw new UnsupportedOperationException();
    }

    public final void acquireInterruptibly(long arg)
            throws InterruptedException{
        if(Thread.interrupted())
            throw new InterruptedException();
        if(!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }

    private void doAcquireInterruptibly(long arg)
            throws InterruptedException{
        final Node node=addWaiter(Node.EXCLUSIVE);
        boolean failed=true;
        try{
            for(;;){
                final Node p=node.predecessor();
                if(p==head&&tryAcquire(arg)){
                    setHead(node);
                    p.next=null; // help GC
                    failed=false;
                    return;
                }
                if(shouldParkAfterFailedAcquire(p,node)&&
                        parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        }finally{
            if(failed)
                cancelAcquire(node);
        }
    }

    public final boolean tryAcquireNanos(long arg,long nanosTimeout)
            throws InterruptedException{
        if(Thread.interrupted())
            throw new InterruptedException();
        return tryAcquire(arg)||
                doAcquireNanos(arg,nanosTimeout);
    }

    private boolean doAcquireNanos(long arg,long nanosTimeout)
            throws InterruptedException{
        if(nanosTimeout<=0L)
            return false;
        final long deadline=System.nanoTime()+nanosTimeout;
        final Node node=addWaiter(Node.EXCLUSIVE);
        boolean failed=true;
        try{
            for(;;){
                final Node p=node.predecessor();
                if(p==head&&tryAcquire(arg)){
                    setHead(node);
                    p.next=null; // help GC
                    failed=false;
                    return true;
                }
                nanosTimeout=deadline-System.nanoTime();
                if(nanosTimeout<=0L)
                    return false;
                if(shouldParkAfterFailedAcquire(p,node)&&
                        nanosTimeout>spinForTimeoutThreshold)
                    LockSupport.parkNanos(this,nanosTimeout);
                if(Thread.interrupted())
                    throw new InterruptedException();
            }
        }finally{
            if(failed)
                cancelAcquire(node);
        }
    }
    // Queue inspection methods

    public final void acquireShared(long arg){
        if(tryAcquireShared(arg)<0)
            doAcquireShared(arg);
    }

    public final void acquireSharedInterruptibly(long arg)
            throws InterruptedException{
        if(Thread.interrupted())
            throw new InterruptedException();
        if(tryAcquireShared(arg)<0)
            doAcquireSharedInterruptibly(arg);
    }

    public final boolean tryAcquireSharedNanos(long arg,long nanosTimeout)
            throws InterruptedException{
        if(Thread.interrupted())
            throw new InterruptedException();
        return tryAcquireShared(arg)>=0||
                doAcquireSharedNanos(arg,nanosTimeout);
    }

    public final boolean releaseShared(long arg){
        if(tryReleaseShared(arg)){
            doReleaseShared();
            return true;
        }
        return false;
    }

    private void doReleaseShared(){
        /**
         * Ensure that a release propagates, even if there are other
         * in-progress acquires/releases.  This proceeds in the usual
         * way of trying to unparkSuccessor of head if it needs
         * signal. But if it does not, status is set to PROPAGATE to
         * ensure that upon release, propagation continues.
         * Additionally, we must loop in case a new node is added
         * while we are doing this. Also, unlike other uses of
         * unparkSuccessor, we need to know if CAS to reset status
         * fails, if so rechecking.
         */
        for(;;){
            Node h=head;
            if(h!=null&&h!=tail){
                int ws=h.waitStatus;
                if(ws==Node.SIGNAL){
                    if(!compareAndSetWaitStatus(h,Node.SIGNAL,0))
                        continue;            // loop to recheck cases
                    unparkSuccessor(h);
                }else if(ws==0&&
                        !compareAndSetWaitStatus(h,0,Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
            if(h==head)                   // loop if head changed
                break;
        }
    }

    protected boolean tryReleaseShared(long arg){
        throw new UnsupportedOperationException();
    }

    public final boolean hasContended(){
        return head!=null;
    }
    // Instrumentation and monitoring methods

    public final Thread getFirstQueuedThread(){
        // handle only fast path, else relay
        return (head==tail)?null:fullGetFirstQueuedThread();
    }

    private Thread fullGetFirstQueuedThread(){
        /**
         * The first node is normally head.next. Try to get its
         * thread field, ensuring consistent reads: If thread
         * field is nulled out or s.prev is no longer head, then
         * some other thread(s) concurrently performed setHead in
         * between some of our reads. We try this twice before
         * resorting to traversal.
         */
        Node h, s;
        Thread st;
        if(((h=head)!=null&&(s=h.next)!=null&&
                s.prev==head&&(st=s.thread)!=null)||
                ((h=head)!=null&&(s=h.next)!=null&&
                        s.prev==head&&(st=s.thread)!=null))
            return st;
        /**
         * Head's next field might not have been set yet, or may have
         * been unset after setHead. So we must check to see if tail
         * is actually first node. If not, we continue on, safely
         * traversing from tail back to head to find first,
         * guaranteeing termination.
         */
        Node t=tail;
        Thread firstThread=null;
        while(t!=null&&t!=head){
            Thread tt=t.thread;
            if(tt!=null)
                firstThread=tt;
            t=t.prev;
        }
        return firstThread;
    }

    public final boolean isQueued(Thread thread){
        if(thread==null)
            throw new NullPointerException();
        for(Node p=tail;p!=null;p=p.prev)
            if(p.thread==thread)
                return true;
        return false;
    }

    final boolean apparentlyFirstQueuedIsExclusive(){
        Node h, s;
        return (h=head)!=null&&
                (s=h.next)!=null&&
                !s.isShared()&&
                s.thread!=null;
    }

    public final boolean hasQueuedPredecessors(){
        // The correctness of this depends on head being initialized
        // before tail and on head.next being accurate if the current
        // thread is first in queue.
        Node t=tail; // Read fields in reverse initialization order
        Node h=head;
        Node s;
        return h!=t&&
                ((s=h.next)==null||s.thread!=Thread.currentThread());
    }
    // Internal support methods for Conditions

    public final int getQueueLength(){
        int n=0;
        for(Node p=tail;p!=null;p=p.prev){
            if(p.thread!=null)
                ++n;
        }
        return n;
    }

    public final Collection<Thread> getQueuedThreads(){
        ArrayList<Thread> list=new ArrayList<Thread>();
        for(Node p=tail;p!=null;p=p.prev){
            Thread t=p.thread;
            if(t!=null)
                list.add(t);
        }
        return list;
    }

    public final Collection<Thread> getExclusiveQueuedThreads(){
        ArrayList<Thread> list=new ArrayList<Thread>();
        for(Node p=tail;p!=null;p=p.prev){
            if(!p.isShared()){
                Thread t=p.thread;
                if(t!=null)
                    list.add(t);
            }
        }
        return list;
    }

    public final Collection<Thread> getSharedQueuedThreads(){
        ArrayList<Thread> list=new ArrayList<Thread>();
        for(Node p=tail;p!=null;p=p.prev){
            if(p.isShared()){
                Thread t=p.thread;
                if(t!=null)
                    list.add(t);
            }
        }
        return list;
    }

    public String toString(){
        long s=getState();
        String q=hasQueuedThreads()?"non":"";
        return super.toString()+
                "[State = "+s+", "+q+"empty queue]";
    }
    // Instrumentation methods for conditions

    protected final long getState(){
        return state;
    }

    protected final void setState(long newState){
        state=newState;
    }

    public final boolean hasQueuedThreads(){
        return head!=tail;
    }

    final boolean transferForSignal(Node node){
        /**
         * If cannot change waitStatus, the node has been cancelled.
         */
        if(!compareAndSetWaitStatus(node,Node.CONDITION,0))
            return false;
        /**
         * Splice onto queue and try to set waitStatus of predecessor to
         * indicate that thread is (probably) waiting. If cancelled or
         * attempt to set waitStatus fails, wake up to resync (in which
         * case the waitStatus can be transiently and harmlessly wrong).
         */
        Node p=enq(node);
        int ws=p.waitStatus;
        if(ws>0||!compareAndSetWaitStatus(p,ws,Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }

    final boolean transferAfterCancelledWait(Node node){
        if(compareAndSetWaitStatus(node,Node.CONDITION,0)){
            enq(node);
            return true;
        }
        /**
         * If we lost out to a signal(), then we can't proceed
         * until it finishes its enq().  Cancelling during an
         * incomplete transfer is both rare and transient, so just
         * spin.
         */
        while(!isOnSyncQueue(node))
            Thread.yield();
        return false;
    }

    final boolean isOnSyncQueue(Node node){
        if(node.waitStatus==Node.CONDITION||node.prev==null)
            return false;
        if(node.next!=null) // If has successor, it must be on queue
            return true;
        /**
         * node.prev can be non-null, but not yet on queue because
         * the CAS to place it on queue can fail. So we have to
         * traverse from tail to make sure it actually made it.  It
         * will always be near the tail in calls to this method, and
         * unless the CAS failed (which is unlikely), it will be
         * there, so we hardly ever traverse much.
         */
        return findNodeFromTail(node);
    }

    private boolean findNodeFromTail(Node node){
        Node t=tail;
        for(;;){
            if(t==node)
                return true;
            if(t==null)
                return false;
            t=t.prev;
        }
    }

    final long fullyRelease(Node node){
        boolean failed=true;
        try{
            long savedState=getState();
            if(release(savedState)){
                failed=false;
                return savedState;
            }else{
                throw new IllegalMonitorStateException();
            }
        }finally{
            if(failed)
                node.waitStatus=Node.CANCELLED;
        }
    }

    public final boolean release(long arg){
        if(tryRelease(arg)){
            Node h=head;
            if(h!=null&&h.waitStatus!=0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }

    protected boolean tryRelease(long arg){
        throw new UnsupportedOperationException();
    }

    public final boolean hasWaiters(ConditionObject condition){
        if(!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.hasWaiters();
    }

    public final boolean owns(ConditionObject condition){
        return condition.isOwnedBy(this);
    }

    public final int getWaitQueueLength(ConditionObject condition){
        if(!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitQueueLength();
    }

    public final Collection<Thread> getWaitingThreads(ConditionObject condition){
        if(!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitingThreads();
    }

    static final class Node{
        static final Node SHARED=new Node();
        static final Node EXCLUSIVE=null;
        static final int CANCELLED=1;
        static final int SIGNAL=-1;
        static final int CONDITION=-2;
        static final int PROPAGATE=-3;
        volatile int waitStatus;
        volatile Node prev;
        volatile Node next;
        volatile Thread thread;
        Node nextWaiter;

        Node(){    // Used to establish initial head or SHARED marker
        }

        Node(Thread thread,Node mode){     // Used by addWaiter
            this.nextWaiter=mode;
            this.thread=thread;
        }

        Node(Thread thread,int waitStatus){ // Used by Condition
            this.waitStatus=waitStatus;
            this.thread=thread;
        }

        final boolean isShared(){
            return nextWaiter==SHARED;
        }

        final Node predecessor() throws NullPointerException{
            Node p=prev;
            if(p==null)
                throw new NullPointerException();
            else
                return p;
        }
    }

    public class ConditionObject implements Condition, java.io.Serializable{
        private static final long serialVersionUID=1173984872572414699L;
        private static final int REINTERRUPT=1;
        private static final int THROW_IE=-1;
        private transient Node firstWaiter;
        // Internal methods
        private transient Node lastWaiter;

        public ConditionObject(){
        }

        public final void await() throws InterruptedException{
            if(Thread.interrupted())
                throw new InterruptedException();
            Node node=addConditionWaiter();
            long savedState=fullyRelease(node);
            int interruptMode=0;
            while(!isOnSyncQueue(node)){
                LockSupport.park(this);
                if((interruptMode=checkInterruptWhileWaiting(node))!=0)
                    break;
            }
            if(acquireQueued(node,savedState)&&interruptMode!=THROW_IE)
                interruptMode=REINTERRUPT;
            if(node.nextWaiter!=null) // clean up if cancelled
                unlinkCancelledWaiters();
            if(interruptMode!=0)
                reportInterruptAfterWait(interruptMode);
        }

        public final void awaitUninterruptibly(){
            Node node=addConditionWaiter();
            long savedState=fullyRelease(node);
            boolean interrupted=false;
            while(!isOnSyncQueue(node)){
                LockSupport.park(this);
                if(Thread.interrupted())
                    interrupted=true;
            }
            if(acquireQueued(node,savedState)||interrupted)
                selfInterrupt();
        }
        // public methods

        public final long awaitNanos(long nanosTimeout)
                throws InterruptedException{
            if(Thread.interrupted())
                throw new InterruptedException();
            Node node=addConditionWaiter();
            long savedState=fullyRelease(node);
            final long deadline=System.nanoTime()+nanosTimeout;
            int interruptMode=0;
            while(!isOnSyncQueue(node)){
                if(nanosTimeout<=0L){
                    transferAfterCancelledWait(node);
                    break;
                }
                if(nanosTimeout>=spinForTimeoutThreshold)
                    LockSupport.parkNanos(this,nanosTimeout);
                if((interruptMode=checkInterruptWhileWaiting(node))!=0)
                    break;
                nanosTimeout=deadline-System.nanoTime();
            }
            if(acquireQueued(node,savedState)&&interruptMode!=THROW_IE)
                interruptMode=REINTERRUPT;
            if(node.nextWaiter!=null)
                unlinkCancelledWaiters();
            if(interruptMode!=0)
                reportInterruptAfterWait(interruptMode);
            return deadline-System.nanoTime();
        }

        public final boolean await(long time,TimeUnit unit)
                throws InterruptedException{
            long nanosTimeout=unit.toNanos(time);
            if(Thread.interrupted())
                throw new InterruptedException();
            Node node=addConditionWaiter();
            long savedState=fullyRelease(node);
            final long deadline=System.nanoTime()+nanosTimeout;
            boolean timedout=false;
            int interruptMode=0;
            while(!isOnSyncQueue(node)){
                if(nanosTimeout<=0L){
                    timedout=transferAfterCancelledWait(node);
                    break;
                }
                if(nanosTimeout>=spinForTimeoutThreshold)
                    LockSupport.parkNanos(this,nanosTimeout);
                if((interruptMode=checkInterruptWhileWaiting(node))!=0)
                    break;
                nanosTimeout=deadline-System.nanoTime();
            }
            if(acquireQueued(node,savedState)&&interruptMode!=THROW_IE)
                interruptMode=REINTERRUPT;
            if(node.nextWaiter!=null)
                unlinkCancelledWaiters();
            if(interruptMode!=0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        public final boolean awaitUntil(Date deadline)
                throws InterruptedException{
            long abstime=deadline.getTime();
            if(Thread.interrupted())
                throw new InterruptedException();
            Node node=addConditionWaiter();
            long savedState=fullyRelease(node);
            boolean timedout=false;
            int interruptMode=0;
            while(!isOnSyncQueue(node)){
                if(System.currentTimeMillis()>abstime){
                    timedout=transferAfterCancelledWait(node);
                    break;
                }
                LockSupport.parkUntil(this,abstime);
                if((interruptMode=checkInterruptWhileWaiting(node))!=0)
                    break;
            }
            if(acquireQueued(node,savedState)&&interruptMode!=THROW_IE)
                interruptMode=REINTERRUPT;
            if(node.nextWaiter!=null)
                unlinkCancelledWaiters();
            if(interruptMode!=0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        public final void signal(){
            if(!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first=firstWaiter;
            if(first!=null)
                doSignal(first);
        }

        private void doSignal(Node first){
            do{
                if((firstWaiter=first.nextWaiter)==null)
                    lastWaiter=null;
                first.nextWaiter=null;
            }while(!transferForSignal(first)&&
                    (first=firstWaiter)!=null);
        }

        public final void signalAll(){
            if(!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first=firstWaiter;
            if(first!=null)
                doSignalAll(first);
        }

        private void doSignalAll(Node first){
            lastWaiter=firstWaiter=null;
            do{
                Node next=first.nextWaiter;
                first.nextWaiter=null;
                transferForSignal(first);
                first=next;
            }while(first!=null);
        }

        private Node addConditionWaiter(){
            Node t=lastWaiter;
            // If lastWaiter is cancelled, clean out.
            if(t!=null&&t.waitStatus!=Node.CONDITION){
                unlinkCancelledWaiters();
                t=lastWaiter;
            }
            Node node=new Node(Thread.currentThread(),Node.CONDITION);
            if(t==null)
                firstWaiter=node;
            else
                t.nextWaiter=node;
            lastWaiter=node;
            return node;
        }

        private void unlinkCancelledWaiters(){
            Node t=firstWaiter;
            Node trail=null;
            while(t!=null){
                Node next=t.nextWaiter;
                if(t.waitStatus!=Node.CONDITION){
                    t.nextWaiter=null;
                    if(trail==null)
                        firstWaiter=next;
                    else
                        trail.nextWaiter=next;
                    if(next==null)
                        lastWaiter=trail;
                }else
                    trail=t;
                t=next;
            }
        }

        private int checkInterruptWhileWaiting(Node node){
            return Thread.interrupted()?
                    (transferAfterCancelledWait(node)?THROW_IE:REINTERRUPT):
                    0;
        }

        private void reportInterruptAfterWait(int interruptMode)
                throws InterruptedException{
            if(interruptMode==THROW_IE)
                throw new InterruptedException();
            else if(interruptMode==REINTERRUPT)
                selfInterrupt();
        }
        //  support for instrumentation

        final boolean isOwnedBy(AbstractQueuedLongSynchronizer sync){
            return sync==AbstractQueuedLongSynchronizer.this;
        }

        protected final boolean hasWaiters(){
            if(!isHeldExclusively())
                throw new IllegalMonitorStateException();
            for(Node w=firstWaiter;w!=null;w=w.nextWaiter){
                if(w.waitStatus==Node.CONDITION)
                    return true;
            }
            return false;
        }

        protected final int getWaitQueueLength(){
            if(!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int n=0;
            for(Node w=firstWaiter;w!=null;w=w.nextWaiter){
                if(w.waitStatus==Node.CONDITION)
                    ++n;
            }
            return n;
        }

        protected final Collection<Thread> getWaitingThreads(){
            if(!isHeldExclusively())
                throw new IllegalMonitorStateException();
            ArrayList<Thread> list=new ArrayList<Thread>();
            for(Node w=firstWaiter;w!=null;w=w.nextWaiter){
                if(w.waitStatus==Node.CONDITION){
                    Thread t=w.thread;
                    if(t!=null)
                        list.add(t);
                }
            }
            return list;
        }
    }
}
