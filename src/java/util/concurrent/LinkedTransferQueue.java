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

import java.util.*;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public class LinkedTransferQueue<E> extends AbstractQueue<E>
        implements TransferQueue<E>, java.io.Serializable{
    static final int SWEEP_THRESHOLD=32;
    private static final long serialVersionUID=-3223113410248163686L;
    private static final boolean MP=
            Runtime.getRuntime().availableProcessors()>1;
    private static final int FRONT_SPINS=1<<7;
    private static final int CHAINED_SPINS=FRONT_SPINS>>>1;
    private static final int NOW=0; // for untimed poll, tryTransfer
    private static final int ASYNC=1; // for offer, put, add
    private static final int SYNC=2; // for transfer, take
    private static final int TIMED=3; // for timed poll, tryTransfer
    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long sweepVotesOffset;

    static{
        try{
            UNSAFE=sun.misc.Unsafe.getUnsafe();
            Class<?> k=LinkedTransferQueue.class;
            headOffset=UNSAFE.objectFieldOffset
                    (k.getDeclaredField("head"));
            tailOffset=UNSAFE.objectFieldOffset
                    (k.getDeclaredField("tail"));
            sweepVotesOffset=UNSAFE.objectFieldOffset
                    (k.getDeclaredField("sweepVotes"));
        }catch(Exception e){
            throw new Error(e);
        }
    }

    transient volatile Node head;
    private transient volatile Node tail;
    private transient volatile int sweepVotes;

    public LinkedTransferQueue(Collection<? extends E> c){
        this();
        addAll(c);
    }

    public LinkedTransferQueue(){
    }

    final Node firstDataNode(){
        for(Node p=head;p!=null;){
            Object item=p.item;
            if(p.isData){
                if(item!=null&&item!=p)
                    return p;
            }else if(item==null)
                break;
            if(p==(p=p.next))
                p=head;
        }
        return null;
    }

    public Spliterator<E> spliterator(){
        return new LTQSpliterator<E>(this);
    }

    public void put(E e){
        xfer(e,true,ASYNC,0);
    }

    private E xfer(E e,boolean haveData,int how,long nanos){
        if(haveData&&(e==null))
            throw new NullPointerException();
        Node s=null;                        // the node to append, if needed
        retry:
        for(;;){                            // restart on append race
            for(Node h=head, p=h;p!=null;){ // find & match first node
                boolean isData=p.isData;
                Object item=p.item;
                if(item!=p&&(item!=null)==isData){ // unmatched
                    if(isData==haveData)   // can't match
                        break;
                    if(p.casItem(item,e)){ // match
                        for(Node q=p;q!=h;){
                            Node n=q.next;  // update by 2 unless singleton
                            if(head==h&&casHead(h,n==null?q:n)){
                                h.forgetNext();
                                break;
                            }                 // advance and retry
                            if((h=head)==null||
                                    (q=h.next)==null||!q.isMatched())
                                break;        // unless slack < 2
                        }
                        LockSupport.unpark(p.waiter);
                        return LinkedTransferQueue.<E>cast(item);
                    }
                }
                Node n=p.next;
                p=(p!=n)?n:(h=head); // Use head if p offlist
            }
            if(how!=NOW){                 // No matches available
                if(s==null)
                    s=new Node(e,haveData);
                Node pred=tryAppend(s,haveData);
                if(pred==null)
                    continue retry;           // lost race vs opposite mode
                if(how!=ASYNC)
                    return awaitMatch(s,pred,e,(how==TIMED),nanos);
            }
            return e; // not waiting
        }
    }

    private boolean casHead(Node cmp,Node val){
        return UNSAFE.compareAndSwapObject(this,headOffset,cmp,val);
    }

    @SuppressWarnings("unchecked")
    static <E> E cast(Object item){
        // assert item == null || item.getClass() != Node.class;
        return (E)item;
    }

    private Node tryAppend(Node s,boolean haveData){
        for(Node t=tail, p=t;;){        // move p to last node and append
            Node n, u;                        // temps for reads of next & tail
            if(p==null&&(p=head)==null){
                if(casHead(null,s))
                    return s;                 // initialize
            }else if(p.cannotPrecede(haveData))
                return null;                  // lost race vs opposite mode
            else if((n=p.next)!=null)    // not last; keep traversing
                p=p!=t&&t!=(u=tail)?(t=u): // stale tail
                        (p!=n)?n:null;      // restart if off list
            else if(!p.casNext(null,s))
                p=p.next;                   // re-read on CAS failure
            else{
                if(p!=t){                 // update if slack now >= 2
                    while((tail!=t||!casTail(t,s))&&
                            (t=tail)!=null&&
                            (s=t.next)!=null&& // advance and retry
                            (s=s.next)!=null&&s!=t) ;
                }
                return p;
            }
        }
    }

    // CAS methods for fields
    private boolean casTail(Node cmp,Node val){
        return UNSAFE.compareAndSwapObject(this,tailOffset,cmp,val);
    }

    private E awaitMatch(Node s,Node pred,E e,boolean timed,long nanos){
        final long deadline=timed?System.nanoTime()+nanos:0L;
        Thread w=Thread.currentThread();
        int spins=-1; // initialized after first item and cancel checks
        ThreadLocalRandom randomYields=null; // bound if needed
        for(;;){
            Object item=s.item;
            if(item!=e){                  // matched
                // assert item != s;
                s.forgetContents();           // avoid garbage
                return LinkedTransferQueue.<E>cast(item);
            }
            if((w.isInterrupted()||(timed&&nanos<=0))&&
                    s.casItem(e,s)){        // cancel
                unsplice(pred,s);
                return e;
            }
            if(spins<0){                  // establish spins at/near front
                if((spins=spinsFor(pred,s.isData))>0)
                    randomYields=ThreadLocalRandom.current();
            }else if(spins>0){             // spin
                --spins;
                if(randomYields.nextInt(CHAINED_SPINS)==0)
                    Thread.yield();           // occasionally yield
            }else if(s.waiter==null){
                s.waiter=w;                 // request unpark then recheck
            }else if(timed){
                nanos=deadline-System.nanoTime();
                if(nanos>0L)
                    LockSupport.parkNanos(this,nanos);
            }else{
                LockSupport.park(this);
            }
        }
    }

    private static int spinsFor(Node pred,boolean haveData){
        if(MP&&pred!=null){
            if(pred.isData!=haveData)      // phase change
                return FRONT_SPINS+CHAINED_SPINS;
            if(pred.isMatched())             // probably at front
                return FRONT_SPINS;
            if(pred.waiter==null)          // pred apparently spinning
                return CHAINED_SPINS;
        }
        return 0;
    }

    final void unsplice(Node pred,Node s){
        s.forgetContents(); // forget unneeded fields
        /**
         * See above for rationale. Briefly: if pred still points to
         * s, try to unlink s.  If s cannot be unlinked, because it is
         * trailing node or pred might be unlinked, and neither pred
         * nor s are head or offlist, add to sweepVotes, and if enough
         * votes have accumulated, sweep.
         */
        if(pred!=null&&pred!=s&&pred.next==s){
            Node n=s.next;
            if(n==null||
                    (n!=s&&pred.casNext(s,n)&&pred.isMatched())){
                for(;;){               // check if at, or could be, head
                    Node h=head;
                    if(h==pred||h==s||h==null)
                        return;          // at head or list empty
                    if(!h.isMatched())
                        break;
                    Node hn=h.next;
                    if(hn==null)
                        return;          // now empty
                    if(hn!=h&&casHead(h,hn))
                        h.forgetNext();  // advance head
                }
                if(pred.next!=pred&&s.next!=s){ // recheck if offlist
                    for(;;){           // sweep now if enough votes
                        int v=sweepVotes;
                        if(v<SWEEP_THRESHOLD){
                            if(casSweepVotes(v,v+1))
                                break;
                        }else if(casSweepVotes(v,0)){
                            sweep();
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean casSweepVotes(int cmp,int val){
        return UNSAFE.compareAndSwapInt(this,sweepVotesOffset,cmp,val);
    }

    private void sweep(){
        for(Node p=head, s, n;p!=null&&(s=p.next)!=null;){
            if(!s.isMatched())
                // Unmatched nodes are never self-linked
                p=s;
            else if((n=s.next)==null) // trailing node is pinned
                break;
            else if(s==n)    // stale
                // No need to also check for p == s, since that implies s == n
                p=head;
            else
                p.casNext(s,n);
        }
    }

    public boolean offer(E e,long timeout,TimeUnit unit){
        xfer(e,true,ASYNC,0);
        return true;
    }

    public E take() throws InterruptedException{
        E e=xfer(null,false,SYNC,0);
        if(e!=null)
            return e;
        Thread.interrupted();
        throw new InterruptedException();
    }

    public E poll(long timeout,TimeUnit unit) throws InterruptedException{
        E e=xfer(null,false,TIMED,unit.toNanos(timeout));
        if(e!=null||!Thread.interrupted())
            return e;
        throw new InterruptedException();
    }

    public int remainingCapacity(){
        return Integer.MAX_VALUE;
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

    public boolean add(E e){
        xfer(e,true,ASYNC,0);
        return true;
    }

    public boolean tryTransfer(E e){
        return xfer(e,true,NOW,0)==null;
    }

    public void transfer(E e) throws InterruptedException{
        if(xfer(e,true,SYNC,0)!=null){
            Thread.interrupted(); // failure possible only due to interrupt
            throw new InterruptedException();
        }
    }

    public boolean tryTransfer(E e,long timeout,TimeUnit unit)
            throws InterruptedException{
        if(xfer(e,true,TIMED,unit.toNanos(timeout))==null)
            return true;
        if(!Thread.interrupted())
            return false;
        throw new InterruptedException();
    }

    public boolean hasWaitingConsumer(){
        return firstOfMode(false)!=null;
    }

    private Node firstOfMode(boolean isData){
        for(Node p=head;p!=null;p=succ(p)){
            if(!p.isMatched())
                return (p.isData==isData)?p:null;
        }
        return null;
    }

    final Node succ(Node p){
        Node next=p.next;
        return (p==next)?head:next;
    }

    public int getWaitingConsumerCount(){
        return countOfMode(false);
    }

    private int countOfMode(boolean data){
        int count=0;
        for(Node p=head;p!=null;){
            if(!p.isMatched()){
                if(p.isData!=data)
                    return 0;
                if(++count==Integer.MAX_VALUE) // saturated
                    break;
            }
            Node n=p.next;
            if(n!=p)
                p=n;
            else{
                count=0;
                p=head;
            }
        }
        return count;
    }

    public Iterator<E> iterator(){
        return new Itr();
    }

    public int size(){
        return countOfMode(true);
    }

    public boolean isEmpty(){
        for(Node p=head;p!=null;p=succ(p)){
            if(!p.isMatched())
                return !p.isData;
        }
        return true;
    }

    public boolean contains(Object o){
        if(o==null) return false;
        for(Node p=head;p!=null;p=succ(p)){
            Object item=p.item;
            if(p.isData){
                if(item!=null&&item!=p&&o.equals(item))
                    return true;
            }else if(item==null)
                break;
        }
        return false;
    }

    public boolean remove(Object o){
        return findAndRemove(o);
    }

    private boolean findAndRemove(Object e){
        if(e!=null){
            for(Node pred=null, p=head;p!=null;){
                Object item=p.item;
                if(p.isData){
                    if(item!=null&&item!=p&&e.equals(item)&&
                            p.tryMatchData()){
                        unsplice(pred,p);
                        return true;
                    }
                }else if(item==null)
                    break;
                pred=p;
                if((p=p.next)==pred){ // stale
                    pred=null;
                    p=head;
                }
            }
        }
        return false;
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        s.defaultWriteObject();
        for(E e : this)
            s.writeObject(e);
        // Use trailing null as sentinel
        s.writeObject(null);
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException{
        s.defaultReadObject();
        for(;;){
            @SuppressWarnings("unchecked")
            E item=(E)s.readObject();
            if(item==null)
                break;
            else
                offer(item);
        }
    }

    public boolean offer(E e){
        xfer(e,true,ASYNC,0);
        return true;
    }

    public E poll(){
        return xfer(null,false,NOW,0);
    }

    public E peek(){
        return firstDataItem();
    }

    private E firstDataItem(){
        for(Node p=head;p!=null;p=succ(p)){
            Object item=p.item;
            if(p.isData){
                if(item!=null&&item!=p)
                    return LinkedTransferQueue.<E>cast(item);
            }else if(item==null)
                return null;
        }
        return null;
    }

    static final class Node{
        private static final long serialVersionUID=-3375979862319811754L;
        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long itemOffset;
        private static final long nextOffset;
        private static final long waiterOffset;

        static{
            try{
                UNSAFE=sun.misc.Unsafe.getUnsafe();
                Class<?> k=Node.class;
                itemOffset=UNSAFE.objectFieldOffset
                        (k.getDeclaredField("item"));
                nextOffset=UNSAFE.objectFieldOffset
                        (k.getDeclaredField("next"));
                waiterOffset=UNSAFE.objectFieldOffset
                        (k.getDeclaredField("waiter"));
            }catch(Exception e){
                throw new Error(e);
            }
        }

        final boolean isData;   // false if this is a request node
        volatile Object item;   // initially non-null if isData; CASed to match
        volatile Node next;
        volatile Thread waiter; // null until waiting

        Node(Object item,boolean isData){
            UNSAFE.putObject(this,itemOffset,item); // relaxed write
            this.isData=isData;
        }

        // CAS methods for fields
        final boolean casNext(Node cmp,Node val){
            return UNSAFE.compareAndSwapObject(this,nextOffset,cmp,val);
        }

        final void forgetNext(){
            UNSAFE.putObject(this,nextOffset,this);
        }

        final void forgetContents(){
            UNSAFE.putObject(this,itemOffset,this);
            UNSAFE.putObject(this,waiterOffset,null);
        }

        final boolean isMatched(){
            Object x=item;
            return (x==this)||((x==null)==isData);
        }

        final boolean isUnmatchedRequest(){
            return !isData&&item==null;
        }

        final boolean cannotPrecede(boolean haveData){
            boolean d=isData;
            Object x;
            return d!=haveData&&(x=item)!=this&&(x!=null)==d;
        }

        final boolean tryMatchData(){
            // assert isData;
            Object x=item;
            if(x!=null&&x!=this&&casItem(x,null)){
                LockSupport.unpark(waiter);
                return true;
            }
            return false;
        }

        final boolean casItem(Object cmp,Object val){
            // assert cmp == null || cmp.getClass() != Node.class;
            return UNSAFE.compareAndSwapObject(this,itemOffset,cmp,val);
        }
    }

    static final class LTQSpliterator<E> implements Spliterator<E>{
        static final int MAX_BATCH=1<<25;  // max batch array size;
        final LinkedTransferQueue<E> queue;
        Node current;    // current node; null until initialized
        int batch;          // batch size for splits
        boolean exhausted;  // true when no more nodes

        LTQSpliterator(LinkedTransferQueue<E> queue){
            this.queue=queue;
        }

        @SuppressWarnings("unchecked")
        public boolean tryAdvance(Consumer<? super E> action){
            Node p;
            if(action==null) throw new NullPointerException();
            final LinkedTransferQueue<E> q=this.queue;
            if(!exhausted&&
                    ((p=current)!=null||(p=q.firstDataNode())!=null)){
                Object e;
                do{
                    if((e=p.item)==p)
                        e=null;
                    if(p==(p=p.next))
                        p=q.firstDataNode();
                }while(e==null&&p!=null&&p.isData);
                if((current=p)==null)
                    exhausted=true;
                if(e!=null){
                    action.accept((E)e);
                    return true;
                }
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action){
            Node p;
            if(action==null) throw new NullPointerException();
            final LinkedTransferQueue<E> q=this.queue;
            if(!exhausted&&
                    ((p=current)!=null||(p=q.firstDataNode())!=null)){
                exhausted=true;
                do{
                    Object e=p.item;
                    if(e!=null&&e!=p)
                        action.accept((E)e);
                    if(p==(p=p.next))
                        p=q.firstDataNode();
                }while(p!=null&&p.isData);
            }
        }

        public Spliterator<E> trySplit(){
            Node p;
            final LinkedTransferQueue<E> q=this.queue;
            int b=batch;
            int n=(b<=0)?1:(b>=MAX_BATCH)?MAX_BATCH:b+1;
            if(!exhausted&&
                    ((p=current)!=null||(p=q.firstDataNode())!=null)&&
                    p.next!=null){
                Object[] a=new Object[n];
                int i=0;
                do{
                    Object e=p.item;
                    if(e!=p&&(a[i]=e)!=null)
                        ++i;
                    if(p==(p=p.next))
                        p=q.firstDataNode();
                }while(p!=null&&i<n&&p.isData);
                if((current=p)==null)
                    exhausted=true;
                if(i>0){
                    batch=i;
                    return Spliterators.spliterator
                            (a,0,i,Spliterator.ORDERED|Spliterator.NONNULL|
                                    Spliterator.CONCURRENT);
                }
            }
            return null;
        }

        public long estimateSize(){
            return Long.MAX_VALUE;
        }

        public int characteristics(){
            return Spliterator.ORDERED|Spliterator.NONNULL|
                    Spliterator.CONCURRENT;
        }
    }

    final class Itr implements Iterator<E>{
        private Node nextNode;   // next node to return item for
        private E nextItem;      // the corresponding item
        private Node lastRet;    // last returned node, to support remove
        private Node lastPred;   // predecessor to unlink lastRet

        Itr(){
            advance(null);
        }

        private void advance(Node prev){
            /**
             * To track and avoid buildup of deleted nodes in the face
             * of calls to both Queue.remove and Itr.remove, we must
             * include variants of unsplice and sweep upon each
             * advance: Upon Itr.remove, we may need to catch up links
             * from lastPred, and upon other removes, we might need to
             * skip ahead from stale nodes and unsplice deleted ones
             * found while advancing.
             */
            Node r, b; // reset lastPred upon possible deletion of lastRet
            if((r=lastRet)!=null&&!r.isMatched())
                lastPred=r;    // next lastPred is old lastRet
            else if((b=lastPred)==null||b.isMatched())
                lastPred=null; // at start of list
            else{
                Node s, n;       // help with removal of lastPred.next
                while((s=b.next)!=null&&
                        s!=b&&s.isMatched()&&
                        (n=s.next)!=null&&n!=s)
                    b.casNext(s,n);
            }
            this.lastRet=prev;
            for(Node p=prev, s, n;;){
                s=(p==null)?head:p.next;
                if(s==null)
                    break;
                else if(s==p){
                    p=null;
                    continue;
                }
                Object item=s.item;
                if(s.isData){
                    if(item!=null&&item!=s){
                        nextItem=LinkedTransferQueue.<E>cast(item);
                        nextNode=s;
                        return;
                    }
                }else if(item==null)
                    break;
                // assert s.isMatched();
                if(p==null)
                    p=s;
                else if((n=s.next)==null)
                    break;
                else if(s==n)
                    p=null;
                else
                    p.casNext(s,n);
            }
            nextNode=null;
            nextItem=null;
        }

        public final boolean hasNext(){
            return nextNode!=null;
        }

        public final E next(){
            Node p=nextNode;
            if(p==null) throw new NoSuchElementException();
            E e=nextItem;
            advance(p);
            return e;
        }

        public final void remove(){
            final Node lastRet=this.lastRet;
            if(lastRet==null)
                throw new IllegalStateException();
            this.lastRet=null;
            if(lastRet.tryMatchData())
                unsplice(lastPred,lastRet);
        }
    }
}
