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
 * Written by Doug Lea and Martin Buchholz with assistance from members of
 * JCP JSR-166 Expert Group and released to the public domain, as explained
 * at http://creativecommons.org/publicdomain/zero/1.0/
 */
/**
 *
 *
 *
 *
 *
 * Written by Doug Lea and Martin Buchholz with assistance from members of
 * JCP JSR-166 Expert Group and released to the public domain, as explained
 * at http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent;

import java.util.*;
import java.util.function.Consumer;

public class ConcurrentLinkedQueue<E> extends AbstractQueue<E>
        implements Queue<E>, java.io.Serializable{
    private static final long serialVersionUID=196745693267521676L;
    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long headOffset;
    private static final long tailOffset;

    static{
        try{
            UNSAFE=sun.misc.Unsafe.getUnsafe();
            Class<?> k=ConcurrentLinkedQueue.class;
            headOffset=UNSAFE.objectFieldOffset
                    (k.getDeclaredField("head"));
            tailOffset=UNSAFE.objectFieldOffset
                    (k.getDeclaredField("tail"));
        }catch(Exception e){
            throw new Error(e);
        }
    }

    private transient volatile Node<E> head;
    // Have to override just to update the javadoc
    private transient volatile Node<E> tail;

    public ConcurrentLinkedQueue(){
        head=tail=new Node<E>(null);
    }

    public ConcurrentLinkedQueue(Collection<? extends E> c){
        Node<E> h=null, t=null;
        for(E e : c){
            checkNotNull(e);
            Node<E> newNode=new Node<E>(e);
            if(h==null)
                h=t=newNode;
            else{
                t.lazySetNext(newNode);
                t=newNode;
            }
        }
        if(h==null)
            h=t=new Node<E>(null);
        head=h;
        tail=t;
    }

    private static void checkNotNull(Object v){
        if(v==null)
            throw new NullPointerException();
    }

    public boolean add(E e){
        return offer(e);
    }

    public boolean offer(E e){
        checkNotNull(e);
        final Node<E> newNode=new Node<E>(e);
        for(Node<E> t=tail, p=t;;){
            Node<E> q=p.next;
            if(q==null){
                // p is last node
                if(p.casNext(null,newNode)){
                    // Successful CAS is the linearization point
                    // for e to become an element of this queue,
                    // and for newNode to become "live".
                    if(p!=t) // hop two nodes at a time
                        casTail(t,newNode);  // Failure is OK.
                    return true;
                }
                // Lost CAS race to another thread; re-read next
            }else if(p==q)
                // We have fallen off list.  If tail is unchanged, it
                // will also be off-list, in which case we need to
                // jump to head, from which all live nodes are always
                // reachable.  Else the new tail is a better bet.
                p=(t!=(t=tail))?t:head;
            else
                // Check for tail updates after two hops.
                p=(p!=t&&t!=(t=tail))?t:q;
        }
    }

    public E poll(){
        restartFromHead:
        for(;;){
            for(Node<E> h=head, p=h, q;;){
                E item=p.item;
                if(item!=null&&p.casItem(item,null)){
                    // Successful CAS is the linearization point
                    // for item to be removed from this queue.
                    if(p!=h) // hop two nodes at a time
                        updateHead(h,((q=p.next)!=null)?q:p);
                    return item;
                }else if((q=p.next)==null){
                    updateHead(h,p);
                    return null;
                }else if(p==q)
                    continue restartFromHead;
                else
                    p=q;
            }
        }
    }

    final void updateHead(Node<E> h,Node<E> p){
        if(h!=p&&casHead(h,p))
            h.lazySetNext(h);
    }

    private boolean casHead(Node<E> cmp,Node<E> val){
        return UNSAFE.compareAndSwapObject(this,headOffset,cmp,val);
    }

    public E peek(){
        restartFromHead:
        for(;;){
            for(Node<E> h=head, p=h, q;;){
                E item=p.item;
                if(item!=null||(q=p.next)==null){
                    updateHead(h,p);
                    return item;
                }else if(p==q)
                    continue restartFromHead;
                else
                    p=q;
            }
        }
    }

    private boolean casTail(Node<E> cmp,Node<E> val){
        return UNSAFE.compareAndSwapObject(this,tailOffset,cmp,val);
    }

    public boolean addAll(Collection<? extends E> c){
        if(c==this)
            // As historically specified in AbstractQueue#addAll
            throw new IllegalArgumentException();
        // Copy c into a private chain of Nodes
        Node<E> beginningOfTheEnd=null, last=null;
        for(E e : c){
            checkNotNull(e);
            Node<E> newNode=new Node<E>(e);
            if(beginningOfTheEnd==null)
                beginningOfTheEnd=last=newNode;
            else{
                last.lazySetNext(newNode);
                last=newNode;
            }
        }
        if(beginningOfTheEnd==null)
            return false;
        // Atomically append the chain at the tail of this collection
        for(Node<E> t=tail, p=t;;){
            Node<E> q=p.next;
            if(q==null){
                // p is last node
                if(p.casNext(null,beginningOfTheEnd)){
                    // Successful CAS is the linearization point
                    // for all elements to be added to this queue.
                    if(!casTail(t,last)){
                        // Try a little harder to update tail,
                        // since we may be adding many elements.
                        t=tail;
                        if(last.next==null)
                            casTail(t,last);
                    }
                    return true;
                }
                // Lost CAS race to another thread; re-read next
            }else if(p==q)
                // We have fallen off list.  If tail is unchanged, it
                // will also be off-list, in which case we need to
                // jump to head, from which all live nodes are always
                // reachable.  Else the new tail is a better bet.
                p=(t!=(t=tail))?t:head;
            else
                // Check for tail updates after two hops.
                p=(p!=t&&t!=(t=tail))?t:q;
        }
    }

    public Iterator<E> iterator(){
        return new Itr();
    }

    public int size(){
        int count=0;
        for(Node<E> p=first();p!=null;p=succ(p))
            if(p.item!=null)
                // Collection.size() spec says to max out
                if(++count==Integer.MAX_VALUE)
                    break;
        return count;
    }

    final Node<E> succ(Node<E> p){
        Node<E> next=p.next;
        return (p==next)?head:next;
    }

    public boolean isEmpty(){
        return first()==null;
    }

    public boolean contains(Object o){
        if(o==null) return false;
        for(Node<E> p=first();p!=null;p=succ(p)){
            E item=p.item;
            if(item!=null&&o.equals(item))
                return true;
        }
        return false;
    }

    public Object[] toArray(){
        // Use ArrayList to deal with resizing.
        ArrayList<E> al=new ArrayList<E>();
        for(Node<E> p=first();p!=null;p=succ(p)){
            E item=p.item;
            if(item!=null)
                al.add(item);
        }
        return al.toArray();
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a){
        // try to use sent-in array
        int k=0;
        Node<E> p;
        for(p=first();p!=null&&k<a.length;p=succ(p)){
            E item=p.item;
            if(item!=null)
                a[k++]=(T)item;
        }
        if(p==null){
            if(k<a.length)
                a[k]=null;
            return a;
        }
        // If won't fit, use ArrayList version
        ArrayList<E> al=new ArrayList<E>();
        for(Node<E> q=first();q!=null;q=succ(q)){
            E item=q.item;
            if(item!=null)
                al.add(item);
        }
        return al.toArray(a);
    }

    public boolean remove(Object o){
        if(o!=null){
            Node<E> next, pred=null;
            for(Node<E> p=first();p!=null;pred=p,p=next){
                boolean removed=false;
                E item=p.item;
                if(item!=null){
                    if(!o.equals(item)){
                        next=succ(p);
                        continue;
                    }
                    removed=p.casItem(item,null);
                }
                next=succ(p);
                if(pred!=null&&next!=null) // unlink
                    pred.casNext(p,next);
                if(removed)
                    return true;
            }
        }
        return false;
    }

    Node<E> first(){
        restartFromHead:
        for(;;){
            for(Node<E> h=head, p=h, q;;){
                boolean hasItem=(p.item!=null);
                if(hasItem||(q=p.next)==null){
                    updateHead(h,p);
                    return hasItem?p:null;
                }else if(p==q)
                    continue restartFromHead;
                else
                    p=q;
            }
        }
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        // Write out any hidden stuff
        s.defaultWriteObject();
        // Write out all elements in the proper order.
        for(Node<E> p=first();p!=null;p=succ(p)){
            Object item=p.item;
            if(item!=null)
                s.writeObject(item);
        }
        // Use trailing null as sentinel
        s.writeObject(null);
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException{
        s.defaultReadObject();
        // Read in elements until trailing null sentinel found
        Node<E> h=null, t=null;
        Object item;
        while((item=s.readObject())!=null){
            @SuppressWarnings("unchecked")
            Node<E> newNode=new Node<E>((E)item);
            if(h==null)
                h=t=newNode;
            else{
                t.lazySetNext(newNode);
                t=newNode;
            }
        }
        if(h==null)
            h=t=new Node<E>(null);
        head=h;
        tail=t;
    }

    @Override
    public Spliterator<E> spliterator(){
        return new CLQSpliterator<E>(this);
    }

    private static class Node<E>{
        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long itemOffset;
        private static final long nextOffset;

        static{
            try{
                UNSAFE=sun.misc.Unsafe.getUnsafe();
                Class<?> k=Node.class;
                itemOffset=UNSAFE.objectFieldOffset
                        (k.getDeclaredField("item"));
                nextOffset=UNSAFE.objectFieldOffset
                        (k.getDeclaredField("next"));
            }catch(Exception e){
                throw new Error(e);
            }
        }

        volatile E item;
        volatile Node<E> next;

        Node(E item){
            UNSAFE.putObject(this,itemOffset,item);
        }

        boolean casItem(E cmp,E val){
            return UNSAFE.compareAndSwapObject(this,itemOffset,cmp,val);
        }

        void lazySetNext(Node<E> val){
            UNSAFE.putOrderedObject(this,nextOffset,val);
        }

        boolean casNext(Node<E> cmp,Node<E> val){
            return UNSAFE.compareAndSwapObject(this,nextOffset,cmp,val);
        }
    }

    static final class CLQSpliterator<E> implements Spliterator<E>{
        static final int MAX_BATCH=1<<25;  // max batch array size;
        final ConcurrentLinkedQueue<E> queue;
        Node<E> current;    // current node; null until initialized
        int batch;          // batch size for splits
        boolean exhausted;  // true when no more nodes

        CLQSpliterator(ConcurrentLinkedQueue<E> queue){
            this.queue=queue;
        }

        public boolean tryAdvance(Consumer<? super E> action){
            Node<E> p;
            if(action==null) throw new NullPointerException();
            final ConcurrentLinkedQueue<E> q=this.queue;
            if(!exhausted&&
                    ((p=current)!=null||(p=q.first())!=null)){
                E e;
                do{
                    e=p.item;
                    if(p==(p=p.next))
                        p=q.first();
                }while(e==null&&p!=null);
                if((current=p)==null)
                    exhausted=true;
                if(e!=null){
                    action.accept(e);
                    return true;
                }
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super E> action){
            Node<E> p;
            if(action==null) throw new NullPointerException();
            final ConcurrentLinkedQueue<E> q=this.queue;
            if(!exhausted&&
                    ((p=current)!=null||(p=q.first())!=null)){
                exhausted=true;
                do{
                    E e=p.item;
                    if(p==(p=p.next))
                        p=q.first();
                    if(e!=null)
                        action.accept(e);
                }while(p!=null);
            }
        }

        public Spliterator<E> trySplit(){
            Node<E> p;
            final ConcurrentLinkedQueue<E> q=this.queue;
            int b=batch;
            int n=(b<=0)?1:(b>=MAX_BATCH)?MAX_BATCH:b+1;
            if(!exhausted&&
                    ((p=current)!=null||(p=q.first())!=null)&&
                    p.next!=null){
                Object[] a=new Object[n];
                int i=0;
                do{
                    if((a[i]=p.item)!=null)
                        ++i;
                    if(p==(p=p.next))
                        p=q.first();
                }while(p!=null&&i<n);
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

    private class Itr implements Iterator<E>{
        private Node<E> nextNode;
        private E nextItem;
        private Node<E> lastRet;

        Itr(){
            advance();
        }

        private E advance(){
            lastRet=nextNode;
            E x=nextItem;
            Node<E> pred, p;
            if(nextNode==null){
                p=first();
                pred=null;
            }else{
                pred=nextNode;
                p=succ(nextNode);
            }
            for(;;){
                if(p==null){
                    nextNode=null;
                    nextItem=null;
                    return x;
                }
                E item=p.item;
                if(item!=null){
                    nextNode=p;
                    nextItem=item;
                    return x;
                }else{
                    // skip over nulls
                    Node<E> next=succ(p);
                    if(pred!=null&&next!=null)
                        pred.casNext(p,next);
                    p=next;
                }
            }
        }

        public boolean hasNext(){
            return nextNode!=null;
        }

        public E next(){
            if(nextNode==null) throw new NoSuchElementException();
            return advance();
        }

        public void remove(){
            Node<E> l=lastRet;
            if(l==null) throw new IllegalStateException();
            // rely on a future traversal to relink.
            l.item=null;
            lastRet=null;
        }
    }
}
