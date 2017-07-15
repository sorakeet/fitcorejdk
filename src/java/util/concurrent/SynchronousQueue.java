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
 * Written by Doug Lea, Bill Scherer, and Michael Scott with
 * assistance from members of JCP JSR-166 Expert Group and released to
 * the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
/**
 *
 *
 *
 *
 *
 * Written by Doug Lea, Bill Scherer, and Michael Scott with
 * assistance from members of JCP JSR-166 Expert Group and released to
 * the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent;

import java.util.*;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronousQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable{
    static final int NCPUS=Runtime.getRuntime().availableProcessors();
    static final int maxTimedSpins=(NCPUS<2)?0:32;
    static final int maxUntimedSpins=maxTimedSpins*16;
    static final long spinForTimeoutThreshold=1000L;
    private static final long serialVersionUID=-3223113410248163686L;
    private transient volatile Transferer<E> transferer;
    private ReentrantLock qlock;
    private WaitQueue waitingProducers;
    private WaitQueue waitingConsumers;

    public SynchronousQueue(){
        this(false);
    }

    public SynchronousQueue(boolean fair){
        transferer=fair?new TransferQueue<E>():new TransferStack<E>();
    }

    // Unsafe mechanics
    static long objectFieldOffset(sun.misc.Unsafe UNSAFE,
                                  String field,Class<?> klazz){
        try{
            return UNSAFE.objectFieldOffset(klazz.getDeclaredField(field));
        }catch(NoSuchFieldException e){
            // Convert Exception to corresponding Error
            NoSuchFieldError error=new NoSuchFieldError(field);
            error.initCause(e);
            throw error;
        }
    }

    public void put(E e) throws InterruptedException{
        if(e==null) throw new NullPointerException();
        if(transferer.transfer(e,false,0)==null){
            Thread.interrupted();
            throw new InterruptedException();
        }
    }

    public boolean offer(E e,long timeout,TimeUnit unit)
            throws InterruptedException{
        if(e==null) throw new NullPointerException();
        if(transferer.transfer(e,true,unit.toNanos(timeout))!=null)
            return true;
        if(!Thread.interrupted())
            return false;
        throw new InterruptedException();
    }

    public E take() throws InterruptedException{
        E e=transferer.transfer(null,false,0);
        if(e!=null)
            return e;
        Thread.interrupted();
        throw new InterruptedException();
    }

    public E poll(long timeout,TimeUnit unit) throws InterruptedException{
        E e=transferer.transfer(null,true,unit.toNanos(timeout));
        if(e!=null||!Thread.interrupted())
            return e;
        throw new InterruptedException();
    }

    public int remainingCapacity(){
        return 0;
    }

    public int drainTo(Collection<? super E> c){
        if(c==null)
            throw new NullPointerException();
        if(c==this)
            throw new IllegalArgumentException();
        int n=0;
        for(E e;(e=poll())!=null;){
            c.add(e);
            ++n;
        }
        return n;
    }

    public int drainTo(Collection<? super E> c,int maxElements){
        if(c==null)
            throw new NullPointerException();
        if(c==this)
            throw new IllegalArgumentException();
        int n=0;
        for(E e;n<maxElements&&(e=poll())!=null;){
            c.add(e);
            ++n;
        }
        return n;
    }

    public boolean offer(E e){
        if(e==null) throw new NullPointerException();
        return transferer.transfer(e,true,0)!=null;
    }

    public E poll(){
        return transferer.transfer(null,true,0);
    }

    public E peek(){
        return null;
    }

    public void clear(){
    }

    public Iterator<E> iterator(){
        return Collections.emptyIterator();
    }

    public int size(){
        return 0;
    }

    public boolean isEmpty(){
        return true;
    }

    public boolean contains(Object o){
        return false;
    }

    public Object[] toArray(){
        return new Object[0];
    }

    public <T> T[] toArray(T[] a){
        if(a.length>0)
            a[0]=null;
        return a;
    }

    public boolean remove(Object o){
        return false;
    }

    public boolean containsAll(Collection<?> c){
        return c.isEmpty();
    }

    public boolean removeAll(Collection<?> c){
        return false;
    }

    public boolean retainAll(Collection<?> c){
        return false;
    }

    public Spliterator<E> spliterator(){
        return Spliterators.emptySpliterator();
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        boolean fair=transferer instanceof TransferQueue;
        if(fair){
            qlock=new ReentrantLock(true);
            waitingProducers=new FifoWaitQueue();
            waitingConsumers=new FifoWaitQueue();
        }else{
            qlock=new ReentrantLock();
            waitingProducers=new LifoWaitQueue();
            waitingConsumers=new LifoWaitQueue();
        }
        s.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException{
        s.defaultReadObject();
        if(waitingProducers instanceof FifoWaitQueue)
            transferer=new TransferQueue<E>();
        else
            transferer=new TransferStack<E>();
    }

    abstract static class Transferer<E>{
        abstract E transfer(E e,boolean timed,long nanos);
    }

    static final class TransferStack<E> extends Transferer<E>{
        /**
         * This extends Scherer-Scott dual stack algorithm, differing,
         * among other ways, by using "covering" nodes rather than
         * bit-marked pointers: Fulfilling operations push on marker
         * nodes (with FULFILLING bit set in mode) to reserve a spot
         * to match a waiting node.
         */
        static final int REQUEST=0;
        static final int DATA=1;
        static final int FULFILLING=2;
        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset;

        static{
            try{
                UNSAFE=sun.misc.Unsafe.getUnsafe();
                Class<?> k=TransferStack.class;
                headOffset=UNSAFE.objectFieldOffset
                        (k.getDeclaredField("head"));
            }catch(Exception e){
                throw new Error(e);
            }
        }

        volatile SNode head;

        @SuppressWarnings("unchecked")
        E transfer(E e,boolean timed,long nanos){
            /**
             * Basic algorithm is to loop trying one of three actions:
             *
             * 1. If apparently empty or already containing nodes of same
             *    mode, try to push node on stack and wait for a match,
             *    returning it, or null if cancelled.
             *
             * 2. If apparently containing node of complementary mode,
             *    try to push a fulfilling node on to stack, match
             *    with corresponding waiting node, pop both from
             *    stack, and return matched item. The matching or
             *    unlinking might not actually be necessary because of
             *    other threads performing action 3:
             *
             * 3. If top of stack already holds another fulfilling node,
             *    help it out by doing its match and/or pop
             *    operations, and then continue. The code for helping
             *    is essentially the same as for fulfilling, except
             *    that it doesn't return the item.
             */
            SNode s=null; // constructed/reused as needed
            int mode=(e==null)?REQUEST:DATA;
            for(;;){
                SNode h=head;
                if(h==null||h.mode==mode){  // empty or same-mode
                    if(timed&&nanos<=0){      // can't wait
                        if(h!=null&&h.isCancelled())
                            casHead(h,h.next);     // pop cancelled node
                        else
                            return null;
                    }else if(casHead(h,s=snode(s,e,h,mode))){
                        SNode m=awaitFulfill(s,timed,nanos);
                        if(m==s){               // wait was cancelled
                            clean(s);
                            return null;
                        }
                        if((h=head)!=null&&h.next==s)
                            casHead(h,s.next);     // help s's fulfiller
                        return (E)((mode==REQUEST)?m.item:s.item);
                    }
                }else if(!isFulfilling(h.mode)){ // try to fulfill
                    if(h.isCancelled())            // already cancelled
                        casHead(h,h.next);         // pop and retry
                    else if(casHead(h,s=snode(s,e,h,FULFILLING|mode))){
                        for(;;){ // loop until matched or waiters disappear
                            SNode m=s.next;       // m is s's match
                            if(m==null){        // all waiters are gone
                                casHead(s,null);   // pop fulfill node
                                s=null;           // use new node next time
                                break;              // restart main loop
                            }
                            SNode mn=m.next;
                            if(m.tryMatch(s)){
                                casHead(s,mn);     // pop both s and m
                                return (E)((mode==REQUEST)?m.item:s.item);
                            }else                  // lost match
                                s.casNext(m,mn);   // help unlink
                        }
                    }
                }else{                            // help a fulfiller
                    SNode m=h.next;               // m is h's match
                    if(m==null)                  // waiter is gone
                        casHead(h,null);           // pop fulfilling node
                    else{
                        SNode mn=m.next;
                        if(m.tryMatch(h))          // help match
                            casHead(h,mn);         // pop both h and m
                        else                        // lost match
                            h.casNext(m,mn);       // help unlink
                    }
                }
            }
        }

        boolean casHead(SNode h,SNode nh){
            return h==head&&
                    UNSAFE.compareAndSwapObject(this,headOffset,h,nh);
        }

        static SNode snode(SNode s,Object e,SNode next,int mode){
            if(s==null) s=new SNode(e);
            s.mode=mode;
            s.next=next;
            return s;
        }

        SNode awaitFulfill(SNode s,boolean timed,long nanos){
            /**
             * When a node/thread is about to block, it sets its waiter
             * field and then rechecks state at least one more time
             * before actually parking, thus covering race vs
             * fulfiller noticing that waiter is non-null so should be
             * woken.
             *
             * When invoked by nodes that appear at the point of call
             * to be at the head of the stack, calls to park are
             * preceded by spins to avoid blocking when producers and
             * consumers are arriving very close in time.  This can
             * happen enough to bother only on multiprocessors.
             *
             * The order of checks for returning out of main loop
             * reflects fact that interrupts have precedence over
             * normal returns, which have precedence over
             * timeouts. (So, on timeout, one last check for match is
             * done before giving up.) Except that calls from untimed
             * SynchronousQueue.{poll/offer} don't check interrupts
             * and don't wait at all, so are trapped in transfer
             * method rather than calling awaitFulfill.
             */
            final long deadline=timed?System.nanoTime()+nanos:0L;
            Thread w=Thread.currentThread();
            int spins=(shouldSpin(s)?
                    (timed?maxTimedSpins:maxUntimedSpins):0);
            for(;;){
                if(w.isInterrupted())
                    s.tryCancel();
                SNode m=s.match;
                if(m!=null)
                    return m;
                if(timed){
                    nanos=deadline-System.nanoTime();
                    if(nanos<=0L){
                        s.tryCancel();
                        continue;
                    }
                }
                if(spins>0)
                    spins=shouldSpin(s)?(spins-1):0;
                else if(s.waiter==null)
                    s.waiter=w; // establish waiter so can park next iter
                else if(!timed)
                    LockSupport.park(this);
                else if(nanos>spinForTimeoutThreshold)
                    LockSupport.parkNanos(this,nanos);
            }
        }

        boolean shouldSpin(SNode s){
            SNode h=head;
            return (h==s||h==null||isFulfilling(h.mode));
        }

        static boolean isFulfilling(int m){
            return (m&FULFILLING)!=0;
        }

        void clean(SNode s){
            s.item=null;   // forget item
            s.waiter=null; // forget thread
            /**
             * At worst we may need to traverse entire stack to unlink
             * s. If there are multiple concurrent calls to clean, we
             * might not see s if another thread has already removed
             * it. But we can stop when we see any node known to
             * follow s. We use s.next unless it too is cancelled, in
             * which case we try the node one past. We don't check any
             * further because we don't want to doubly traverse just to
             * find sentinel.
             */
            SNode past=s.next;
            if(past!=null&&past.isCancelled())
                past=past.next;
            // Absorb cancelled nodes at head
            SNode p;
            while((p=head)!=null&&p!=past&&p.isCancelled())
                casHead(p,p.next);
            // Unsplice embedded nodes
            while(p!=null&&p!=past){
                SNode n=p.next;
                if(n!=null&&n.isCancelled())
                    p.casNext(n,n.next);
                else
                    p=n;
            }
        }

        static final class SNode{
            // Unsafe mechanics
            private static final sun.misc.Unsafe UNSAFE;
            private static final long matchOffset;
            private static final long nextOffset;

            static{
                try{
                    UNSAFE=sun.misc.Unsafe.getUnsafe();
                    Class<?> k=SNode.class;
                    matchOffset=UNSAFE.objectFieldOffset
                            (k.getDeclaredField("match"));
                    nextOffset=UNSAFE.objectFieldOffset
                            (k.getDeclaredField("next"));
                }catch(Exception e){
                    throw new Error(e);
                }
            }

            volatile SNode next;        // next node in stack
            // Note: item and mode fields don't need to be volatile
            // since they are always written before, and read after,
            // other volatile/atomic operations.
            volatile SNode match;       // the node matched to this
            volatile Thread waiter;     // to control park/unpark
            Object item;                // data; or null for REQUESTs
            int mode;

            SNode(Object item){
                this.item=item;
            }

            boolean casNext(SNode cmp,SNode val){
                return cmp==next&&
                        UNSAFE.compareAndSwapObject(this,nextOffset,cmp,val);
            }

            boolean tryMatch(SNode s){
                if(match==null&&
                        UNSAFE.compareAndSwapObject(this,matchOffset,null,s)){
                    Thread w=waiter;
                    if(w!=null){    // waiters need at most one unpark
                        waiter=null;
                        LockSupport.unpark(w);
                    }
                    return true;
                }
                return match==s;
            }

            void tryCancel(){
                UNSAFE.compareAndSwapObject(this,matchOffset,null,this);
            }

            boolean isCancelled(){
                return match==this;
            }
        }
    }

    static final class TransferQueue<E> extends Transferer<E>{
        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset;
        private static final long tailOffset;
        private static final long cleanMeOffset;

        static{
            try{
                UNSAFE=sun.misc.Unsafe.getUnsafe();
                Class<?> k=TransferQueue.class;
                headOffset=UNSAFE.objectFieldOffset
                        (k.getDeclaredField("head"));
                tailOffset=UNSAFE.objectFieldOffset
                        (k.getDeclaredField("tail"));
                cleanMeOffset=UNSAFE.objectFieldOffset
                        (k.getDeclaredField("cleanMe"));
            }catch(Exception e){
                throw new Error(e);
            }
        }

        transient volatile QNode head;
        transient volatile QNode tail;        void advanceHead(QNode h,QNode nh){
            if(h==head&&
                    UNSAFE.compareAndSwapObject(this,headOffset,h,nh))
                h.next=h; // forget old next
        }
        transient volatile QNode cleanMe;

        TransferQueue(){
            QNode h=new QNode(null,false); // initialize to dummy node.
            head=h;
            tail=h;
        }        void advanceTail(QNode t,QNode nt){
            if(tail==t)
                UNSAFE.compareAndSwapObject(this,tailOffset,t,nt);
        }

        static final class QNode{
            // Unsafe mechanics
            private static final sun.misc.Unsafe UNSAFE;
            private static final long itemOffset;
            private static final long nextOffset;

            static{
                try{
                    UNSAFE=sun.misc.Unsafe.getUnsafe();
                    Class<?> k=QNode.class;
                    itemOffset=UNSAFE.objectFieldOffset
                            (k.getDeclaredField("item"));
                    nextOffset=UNSAFE.objectFieldOffset
                            (k.getDeclaredField("next"));
                }catch(Exception e){
                    throw new Error(e);
                }
            }

            final boolean isData;
            volatile QNode next;          // next node in queue
            volatile Object item;         // CAS'ed to or from null
            volatile Thread waiter;       // to control park/unpark

            QNode(Object item,boolean isData){
                this.item=item;
                this.isData=isData;
            }

            boolean casNext(QNode cmp,QNode val){
                return next==cmp&&
                        UNSAFE.compareAndSwapObject(this,nextOffset,cmp,val);
            }

            boolean casItem(Object cmp,Object val){
                return item==cmp&&
                        UNSAFE.compareAndSwapObject(this,itemOffset,cmp,val);
            }

            void tryCancel(Object cmp){
                UNSAFE.compareAndSwapObject(this,itemOffset,cmp,this);
            }

            boolean isCancelled(){
                return item==this;
            }

            boolean isOffList(){
                return next==this;
            }
        }

        boolean casCleanMe(QNode cmp,QNode val){
            return cleanMe==cmp&&
                    UNSAFE.compareAndSwapObject(this,cleanMeOffset,cmp,val);
        }



        @SuppressWarnings("unchecked")
        E transfer(E e,boolean timed,long nanos){
            /** Basic algorithm is to loop trying to take either of
             * two actions:
             *
             * 1. If queue apparently empty or holding same-mode nodes,
             *    try to add node to queue of waiters, wait to be
             *    fulfilled (or cancelled) and return matching item.
             *
             * 2. If queue apparently contains waiting items, and this
             *    call is of complementary mode, try to fulfill by CAS'ing
             *    item field of waiting node and dequeuing it, and then
             *    returning matching item.
             *
             * In each case, along the way, check for and try to help
             * advance head and tail on behalf of other stalled/slow
             * threads.
             *
             * The loop starts off with a null check guarding against
             * seeing uninitialized head or tail values. This never
             * happens in current SynchronousQueue, but could if
             * callers held non-volatile/final ref to the
             * transferer. The check is here anyway because it places
             * null checks at top of loop, which is usually faster
             * than having them implicitly interspersed.
             */
            QNode s=null; // constructed/reused as needed
            boolean isData=(e!=null);
            for(;;){
                QNode t=tail;
                QNode h=head;
                if(t==null||h==null)         // saw uninitialized value
                    continue;                       // spin
                if(h==t||t.isData==isData){ // empty or same-mode
                    QNode tn=t.next;
                    if(t!=tail)                  // inconsistent read
                        continue;
                    if(tn!=null){               // lagging tail
                        advanceTail(t,tn);
                        continue;
                    }
                    if(timed&&nanos<=0)        // can't wait
                        return null;
                    if(s==null)
                        s=new QNode(e,isData);
                    if(!t.casNext(null,s))        // failed to link in
                        continue;
                    advanceTail(t,s);              // swing tail and wait
                    Object x=awaitFulfill(s,e,timed,nanos);
                    if(x==s){                   // wait was cancelled
                        clean(t,s);
                        return null;
                    }
                    if(!s.isOffList()){           // not already unlinked
                        advanceHead(t,s);          // unlink if head
                        if(x!=null)              // and forget fields
                            s.item=s;
                        s.waiter=null;
                    }
                    return (x!=null)?(E)x:e;
                }else{                            // complementary-mode
                    QNode m=h.next;               // node to fulfill
                    if(t!=tail||m==null||h!=head)
                        continue;                   // inconsistent read
                    Object x=m.item;
                    if(isData==(x!=null)||    // m already fulfilled
                            x==m||                   // m cancelled
                            !m.casItem(x,e)){         // lost CAS
                        advanceHead(h,m);          // dequeue and retry
                        continue;
                    }
                    advanceHead(h,m);              // successfully fulfilled
                    LockSupport.unpark(m.waiter);
                    return (x!=null)?(E)x:e;
                }
            }
        }



        Object awaitFulfill(QNode s,E e,boolean timed,long nanos){
            /** Same idea as TransferStack.awaitFulfill */
            final long deadline=timed?System.nanoTime()+nanos:0L;
            Thread w=Thread.currentThread();
            int spins=((head.next==s)?
                    (timed?maxTimedSpins:maxUntimedSpins):0);
            for(;;){
                if(w.isInterrupted())
                    s.tryCancel(e);
                Object x=s.item;
                if(x!=e)
                    return x;
                if(timed){
                    nanos=deadline-System.nanoTime();
                    if(nanos<=0L){
                        s.tryCancel(e);
                        continue;
                    }
                }
                if(spins>0)
                    --spins;
                else if(s.waiter==null)
                    s.waiter=w;
                else if(!timed)
                    LockSupport.park(this);
                else if(nanos>spinForTimeoutThreshold)
                    LockSupport.parkNanos(this,nanos);
            }
        }

        void clean(QNode pred,QNode s){
            s.waiter=null; // forget thread
            /**
             * At any given time, exactly one node on list cannot be
             * deleted -- the last inserted node. To accommodate this,
             * if we cannot delete s, we save its predecessor as
             * "cleanMe", deleting the previously saved version
             * first. At least one of node s or the node previously
             * saved can always be deleted, so this always terminates.
             */
            while(pred.next==s){ // Return early if already unlinked
                QNode h=head;
                QNode hn=h.next;   // Absorb cancelled first node as head
                if(hn!=null&&hn.isCancelled()){
                    advanceHead(h,hn);
                    continue;
                }
                QNode t=tail;      // Ensure consistent read for tail
                if(t==h)
                    return;
                QNode tn=t.next;
                if(t!=tail)
                    continue;
                if(tn!=null){
                    advanceTail(t,tn);
                    continue;
                }
                if(s!=t){        // If not tail, try to unsplice
                    QNode sn=s.next;
                    if(sn==s||pred.casNext(s,sn))
                        return;
                }
                QNode dp=cleanMe;
                if(dp!=null){    // Try unlinking previous cancelled node
                    QNode d=dp.next;
                    QNode dn;
                    if(d==null||               // d is gone or
                            d==dp||                 // d is off list or
                            !d.isCancelled()||        // d not cancelled or
                            (d!=t&&                 // d not tail and
                                    (dn=d.next)!=null&&  //   has successor
                                    dn!=d&&                //   that is on list
                                    dp.casNext(d,dn)))       // d unspliced
                        casCleanMe(dp,null);
                    if(dp==pred)
                        return;      // s is already saved node
                }else if(casCleanMe(null,pred))
                    return;          // Postpone cleaning s
            }
        }
    }

    @SuppressWarnings("serial")
    static class WaitQueue implements java.io.Serializable{
    }

    static class LifoWaitQueue extends WaitQueue{
        private static final long serialVersionUID=-3633113410248163686L;
    }

    static class FifoWaitQueue extends WaitQueue{
        private static final long serialVersionUID=-3623113410248163686L;
    }
}
