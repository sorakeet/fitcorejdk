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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class LinkedBlockingDeque<E>
        extends AbstractQueue<E>
        implements BlockingDeque<E>, java.io.Serializable{
    /**
     * Implemented as a simple doubly-linked list protected by a
     * single lock and using conditions to manage blocking.
     *
     * To implement weakly consistent iterators, it appears we need to
     * keep all Nodes GC-reachable from a predecessor dequeued Node.
     * That would cause two problems:
     * - allow a rogue Iterator to cause unbounded memory retention
     * - cause cross-generational linking of old Nodes to new Nodes if
     *   a Node was tenured while live, which generational GCs have a
     *   hard time dealing with, causing repeated major collections.
     * However, only non-deleted Nodes need to be reachable from
     * dequeued Nodes, and reachability does not necessarily have to
     * be of the kind understood by the GC.  We use the trick of
     * linking a Node that has just been dequeued to itself.  Such a
     * self-link implicitly means to jump to "first" (for next links)
     * or "last" (for prev links).
     */
    private static final long serialVersionUID=-387911632671998426L;
    final ReentrantLock lock=new ReentrantLock();
    private final int capacity;
    private final Condition notEmpty=lock.newCondition();
    private final Condition notFull=lock.newCondition();
    transient Node<E> first;
    transient Node<E> last;
    private transient int count;

    public LinkedBlockingDeque(){
        this(Integer.MAX_VALUE);
    }

    public LinkedBlockingDeque(int capacity){
        if(capacity<=0) throw new IllegalArgumentException();
        this.capacity=capacity;
    }

    public LinkedBlockingDeque(Collection<? extends E> c){
        this(Integer.MAX_VALUE);
        final ReentrantLock lock=this.lock;
        lock.lock(); // Never contended, but necessary for visibility
        try{
            for(E e : c){
                if(e==null)
                    throw new NullPointerException();
                if(!linkLast(new Node<E>(e)))
                    throw new IllegalStateException("Deque full");
            }
        }finally{
            lock.unlock();
        }
    }

    private boolean linkLast(Node<E> node){
        // assert lock.isHeldByCurrentThread();
        if(count>=capacity)
            return false;
        Node<E> l=last;
        node.prev=l;
        last=node;
        if(first==null)
            first=node;
        else
            l.next=node;
        ++count;
        notEmpty.signal();
        return true;
    }
    // Basic linking and unlinking operations, called only while holding lock

    public boolean offer(E e){
        return offerLast(e);
    }

    public E poll(){
        return pollFirst();
    }    private boolean linkFirst(Node<E> node){
        // assert lock.isHeldByCurrentThread();
        if(count>=capacity)
            return false;
        Node<E> f=first;
        node.next=f;
        first=node;
        if(last==null)
            last=node;
        else
            f.prev=node;
        ++count;
        notEmpty.signal();
        return true;
    }

    public E peek(){
        return peekFirst();
    }

    public int remainingCapacity(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            return capacity-count;
        }finally{
            lock.unlock();
        }
    }

    public int drainTo(Collection<? super E> c){
        return drainTo(c,Integer.MAX_VALUE);
    }

    public int drainTo(Collection<? super E> c,int maxElements){
        if(c==null)
            throw new NullPointerException();
        if(c==this)
            throw new IllegalArgumentException();
        if(maxElements<=0)
            return 0;
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            int n=Math.min(maxElements,count);
            for(int i=0;i<n;i++){
                c.add(first.item);   // In this order, in case add() throws.
                unlinkFirst();
            }
            return n;
        }finally{
            lock.unlock();
        }
    }

    public Iterator<E> iterator(){
        return new Itr();
    }

    public int size(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            return count;
        }finally{
            lock.unlock();
        }
    }    void unlink(Node<E> x){
        // assert lock.isHeldByCurrentThread();
        Node<E> p=x.prev;
        Node<E> n=x.next;
        if(p==null){
            unlinkFirst();
        }else if(n==null){
            unlinkLast();
        }else{
            p.next=n;
            n.prev=p;
            x.item=null;
            // Don't mess with x's links.  They may still be in use by
            // an iterator.
            --count;
            notFull.signal();
        }
    }
    // BlockingDeque methods

    public boolean contains(Object o){
        if(o==null) return false;
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            for(Node<E> p=first;p!=null;p=p.next)
                if(o.equals(p.item))
                    return true;
            return false;
        }finally{
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public Object[] toArray(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            Object[] a=new Object[count];
            int k=0;
            for(Node<E> p=first;p!=null;p=p.next)
                a[k++]=p.item;
            return a;
        }finally{
            lock.unlock();
        }
    }    public void addFirst(E e){
        if(!offerFirst(e))
            throw new IllegalStateException("Deque full");
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            if(a.length<count)
                a=(T[])java.lang.reflect.Array.newInstance
                        (a.getClass().getComponentType(),count);
            int k=0;
            for(Node<E> p=first;p!=null;p=p.next)
                a[k++]=(T)p.item;
            if(a.length>k)
                a[k]=null;
            return a;
        }finally{
            lock.unlock();
        }
    }

    public boolean remove(Object o){
        return removeFirstOccurrence(o);
    }    public void addLast(E e){
        if(!offerLast(e))
            throw new IllegalStateException("Deque full");
    }

    public String toString(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            Node<E> p=first;
            if(p==null)
                return "[]";
            StringBuilder sb=new StringBuilder();
            sb.append('[');
            for(;;){
                E e=p.item;
                sb.append(e==this?"(this Collection)":e);
                p=p.next;
                if(p==null)
                    return sb.append(']').toString();
                sb.append(',').append(' ');
            }
        }finally{
            lock.unlock();
        }
    }

    public Spliterator<E> spliterator(){
        return new LBDSpliterator<E>(this);
    }    public boolean offerFirst(E e){
        if(e==null) throw new NullPointerException();
        Node<E> node=new Node<E>(e);
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            return linkFirst(node);
        }finally{
            lock.unlock();
        }
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            // Write out capacity and any hidden stuff
            s.defaultWriteObject();
            // Write out all elements in the proper order.
            for(Node<E> p=first;p!=null;p=p.next)
                s.writeObject(p.item);
            // Use trailing null as sentinel
            s.writeObject(null);
        }finally{
            lock.unlock();
        }
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException{
        s.defaultReadObject();
        count=0;
        first=null;
        last=null;
        // Read in all elements and place in queue
        for(;;){
            @SuppressWarnings("unchecked")
            E item=(E)s.readObject();
            if(item==null)
                break;
            add(item);
        }
    }    public boolean offerLast(E e){
        if(e==null) throw new NullPointerException();
        Node<E> node=new Node<E>(e);
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            return linkLast(node);
        }finally{
            lock.unlock();
        }
    }

    public boolean add(E e){
        addLast(e);
        return true;
    }

    public E remove(){
        return removeFirst();
    }    public void putFirst(E e) throws InterruptedException{
        if(e==null) throw new NullPointerException();
        Node<E> node=new Node<E>(e);
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            while(!linkFirst(node))
                notFull.await();
        }finally{
            lock.unlock();
        }
    }

    public E removeFirst(){
        E x=pollFirst();
        if(x==null) throw new NoSuchElementException();
        return x;
    }

    public E removeLast(){
        E x=pollLast();
        if(x==null) throw new NoSuchElementException();
        return x;
    }    public void putLast(E e) throws InterruptedException{
        if(e==null) throw new NullPointerException();
        Node<E> node=new Node<E>(e);
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            while(!linkLast(node))
                notFull.await();
        }finally{
            lock.unlock();
        }
    }

    public E pollFirst(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            return unlinkFirst();
        }finally{
            lock.unlock();
        }
    }

    private E unlinkFirst(){
        // assert lock.isHeldByCurrentThread();
        Node<E> f=first;
        if(f==null)
            return null;
        Node<E> n=f.next;
        E item=f.item;
        f.item=null;
        f.next=f; // help GC
        first=n;
        if(n==null)
            last=null;
        else
            n.prev=null;
        --count;
        notFull.signal();
        return item;
    }    public boolean offerFirst(E e,long timeout,TimeUnit unit)
            throws InterruptedException{
        if(e==null) throw new NullPointerException();
        Node<E> node=new Node<E>(e);
        long nanos=unit.toNanos(timeout);
        final ReentrantLock lock=this.lock;
        lock.lockInterruptibly();
        try{
            while(!linkFirst(node)){
                if(nanos<=0)
                    return false;
                nanos=notFull.awaitNanos(nanos);
            }
            return true;
        }finally{
            lock.unlock();
        }
    }

    public E pollLast(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            return unlinkLast();
        }finally{
            lock.unlock();
        }
    }

    public E getFirst(){
        E x=peekFirst();
        if(x==null) throw new NoSuchElementException();
        return x;
    }    public boolean offerLast(E e,long timeout,TimeUnit unit)
            throws InterruptedException{
        if(e==null) throw new NullPointerException();
        Node<E> node=new Node<E>(e);
        long nanos=unit.toNanos(timeout);
        final ReentrantLock lock=this.lock;
        lock.lockInterruptibly();
        try{
            while(!linkLast(node)){
                if(nanos<=0)
                    return false;
                nanos=notFull.awaitNanos(nanos);
            }
            return true;
        }finally{
            lock.unlock();
        }
    }

    public E getLast(){
        E x=peekLast();
        if(x==null) throw new NoSuchElementException();
        return x;
    }

    public E peekFirst(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            return (first==null)?null:first.item;
        }finally{
            lock.unlock();
        }
    }

    public E peekLast(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            return (last==null)?null:last.item;
        }finally{
            lock.unlock();
        }
    }

    public E pop(){
        return removeFirst();
    }

    public Iterator<E> descendingIterator(){
        return new DescendingItr();
    }

    public E element(){
        return getFirst();
    }    public E takeFirst() throws InterruptedException{
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            E x;
            while((x=unlinkFirst())==null)
                notEmpty.await();
            return x;
        }finally{
            lock.unlock();
        }
    }

    public void clear(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            for(Node<E> f=first;f!=null;){
                f.item=null;
                Node<E> n=f.next;
                f.prev=null;
                f.next=null;
                f=n;
            }
            first=last=null;
            count=0;
            notFull.signalAll();
        }finally{
            lock.unlock();
        }
    }

    static final class Node<E>{
        E item;
        Node<E> prev;
        Node<E> next;

        Node(E x){
            item=x;
        }
    }    public E takeLast() throws InterruptedException{
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            E x;
            while((x=unlinkLast())==null)
                notEmpty.await();
            return x;
        }finally{
            lock.unlock();
        }
    }

    static final class LBDSpliterator<E> implements Spliterator<E>{
        static final int MAX_BATCH=1<<25;  // max batch array size;
        final LinkedBlockingDeque<E> queue;
        Node<E> current;    // current node; null until initialized
        int batch;          // batch size for splits
        boolean exhausted;  // true when no more nodes
        long est;           // size estimate

        LBDSpliterator(LinkedBlockingDeque<E> queue){
            this.queue=queue;
            this.est=queue.size();
        }

        public boolean tryAdvance(Consumer<? super E> action){
            if(action==null) throw new NullPointerException();
            final LinkedBlockingDeque<E> q=this.queue;
            final ReentrantLock lock=q.lock;
            if(!exhausted){
                E e=null;
                lock.lock();
                try{
                    if(current==null)
                        current=q.first;
                    while(current!=null){
                        e=current.item;
                        current=current.next;
                        if(e!=null)
                            break;
                    }
                }finally{
                    lock.unlock();
                }
                if(current==null)
                    exhausted=true;
                if(e!=null){
                    action.accept(e);
                    return true;
                }
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super E> action){
            if(action==null) throw new NullPointerException();
            final LinkedBlockingDeque<E> q=this.queue;
            final ReentrantLock lock=q.lock;
            if(!exhausted){
                exhausted=true;
                Node<E> p=current;
                do{
                    E e=null;
                    lock.lock();
                    try{
                        if(p==null)
                            p=q.first;
                        while(p!=null){
                            e=p.item;
                            p=p.next;
                            if(e!=null)
                                break;
                        }
                    }finally{
                        lock.unlock();
                    }
                    if(e!=null)
                        action.accept(e);
                }while(p!=null);
            }
        }

        public Spliterator<E> trySplit(){
            Node<E> h;
            final LinkedBlockingDeque<E> q=this.queue;
            int b=batch;
            int n=(b<=0)?1:(b>=MAX_BATCH)?MAX_BATCH:b+1;
            if(!exhausted&&
                    ((h=current)!=null||(h=q.first)!=null)&&
                    h.next!=null){
                Object[] a=new Object[n];
                final ReentrantLock lock=q.lock;
                int i=0;
                Node<E> p=current;
                lock.lock();
                try{
                    if(p!=null||(p=q.first)!=null){
                        do{
                            if((a[i]=p.item)!=null)
                                ++i;
                        }while((p=p.next)!=null&&i<n);
                    }
                }finally{
                    lock.unlock();
                }
                if((current=p)==null){
                    est=0L;
                    exhausted=true;
                }else if((est-=i)<0L)
                    est=0L;
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
            return est;
        }

        public int characteristics(){
            return Spliterator.ORDERED|Spliterator.NONNULL|
                    Spliterator.CONCURRENT;
        }
    }

    private abstract class AbstractItr implements Iterator<E>{
        Node<E> next;
        E nextItem;
        private Node<E> lastRet;

        AbstractItr(){
            // set to initial position
            final ReentrantLock lock=LinkedBlockingDeque.this.lock;
            lock.lock();
            try{
                next=firstNode();
                nextItem=(next==null)?null:next.item;
            }finally{
                lock.unlock();
            }
        }

        abstract Node<E> firstNode();

        public boolean hasNext(){
            return next!=null;
        }

        public E next(){
            if(next==null)
                throw new NoSuchElementException();
            lastRet=next;
            E x=nextItem;
            advance();
            return x;
        }

        void advance(){
            final ReentrantLock lock=LinkedBlockingDeque.this.lock;
            lock.lock();
            try{
                // assert next != null;
                next=succ(next);
                nextItem=(next==null)?null:next.item;
            }finally{
                lock.unlock();
            }
        }

        private Node<E> succ(Node<E> n){
            // Chains of deleted nodes ending in null or self-links
            // are possible if multiple interior nodes are removed.
            for(;;){
                Node<E> s=nextNode(n);
                if(s==null)
                    return null;
                else if(s.item!=null)
                    return s;
                else if(s==n)
                    return firstNode();
                else
                    n=s;
            }
        }

        abstract Node<E> nextNode(Node<E> n);

        public void remove(){
            Node<E> n=lastRet;
            if(n==null)
                throw new IllegalStateException();
            lastRet=null;
            final ReentrantLock lock=LinkedBlockingDeque.this.lock;
            lock.lock();
            try{
                if(n.item!=null)
                    unlink(n);
            }finally{
                lock.unlock();
            }
        }
    }    public E pollFirst(long timeout,TimeUnit unit)
            throws InterruptedException{
        long nanos=unit.toNanos(timeout);
        final ReentrantLock lock=this.lock;
        lock.lockInterruptibly();
        try{
            E x;
            while((x=unlinkFirst())==null){
                if(nanos<=0)
                    return null;
                nanos=notEmpty.awaitNanos(nanos);
            }
            return x;
        }finally{
            lock.unlock();
        }
    }

    private class Itr extends AbstractItr{
        Node<E> firstNode(){
            return first;
        }

        Node<E> nextNode(Node<E> n){
            return n.next;
        }
    }

    private class DescendingItr extends AbstractItr{
        Node<E> firstNode(){
            return last;
        }

        Node<E> nextNode(Node<E> n){
            return n.prev;
        }
    }    public E pollLast(long timeout,TimeUnit unit)
            throws InterruptedException{
        long nanos=unit.toNanos(timeout);
        final ReentrantLock lock=this.lock;
        lock.lockInterruptibly();
        try{
            E x;
            while((x=unlinkLast())==null){
                if(nanos<=0)
                    return null;
                nanos=notEmpty.awaitNanos(nanos);
            }
            return x;
        }finally{
            lock.unlock();
        }
    }



    private E unlinkLast(){
        // assert lock.isHeldByCurrentThread();
        Node<E> l=last;
        if(l==null)
            return null;
        Node<E> p=l.prev;
        E item=l.item;
        l.item=null;
        l.prev=l; // help GC
        last=p;
        if(p==null)
            first=null;
        else
            p.next=null;
        --count;
        notFull.signal();
        return item;
    }







    public boolean removeFirstOccurrence(Object o){
        if(o==null) return false;
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            for(Node<E> p=first;p!=null;p=p.next){
                if(o.equals(p.item)){
                    unlink(p);
                    return true;
                }
            }
            return false;
        }finally{
            lock.unlock();
        }
    }



    public boolean removeLastOccurrence(Object o){
        if(o==null) return false;
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            for(Node<E> p=last;p!=null;p=p.prev){
                if(o.equals(p.item)){
                    unlink(p);
                    return true;
                }
            }
            return false;
        }finally{
            lock.unlock();
        }
    }
    // BlockingQueue methods







    public void put(E e) throws InterruptedException{
        putLast(e);
    }



    public boolean offer(E e,long timeout,TimeUnit unit)
            throws InterruptedException{
        return offerLast(e,timeout,unit);
    }







    public E take() throws InterruptedException{
        return takeFirst();
    }



    public E poll(long timeout,TimeUnit unit) throws InterruptedException{
        return pollFirst(timeout,unit);
    }


    // Stack methods

    public void push(E e){
        addFirst(e);
    }
    // Collection methods
    /**
     * TODO: Add support for more efficient bulk operations.
     *
     * We don't want to acquire the lock for every iteration, but we
     * also want other threads a chance to interact with the
     * collection, especially when count is close to capacity.
     */
//     /**
//      * Adds all of the elements in the specified collection to this
//      * queue.  Attempts to addAll of a queue to itself result in
//      * {@code IllegalArgumentException}. Further, the behavior of
//      * this operation is undefined if the specified collection is
//      * modified while the operation is in progress.
//      *
//      * @param c collection containing elements to be added to this queue
//      * @return {@code true} if this queue changed as a result of the call
//      * @throws ClassCastException            {@inheritDoc}
//      * @throws NullPointerException          {@inheritDoc}
//      * @throws IllegalArgumentException      {@inheritDoc}
//      * @throws IllegalStateException if this deque is full
//      * @see #add(Object)
//      */
//     public boolean addAll(Collection<? extends E> c) {
//         if (c == null)
//             throw new NullPointerException();
//         if (c == this)
//             throw new IllegalArgumentException();
//         final ReentrantLock lock = this.lock;
//         lock.lock();
//         try {
//             boolean modified = false;
//             for (E e : c)
//                 if (linkLast(e))
//                     modified = true;
//             return modified;
//         } finally {
//             lock.unlock();
//         }
//     }
}
