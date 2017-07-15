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

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class DelayQueue<E extends Delayed> extends AbstractQueue<E>
        implements BlockingQueue<E>{
    private final transient ReentrantLock lock=new ReentrantLock();
    private final PriorityQueue<E> q=new PriorityQueue<E>();
    private final Condition available=lock.newCondition();
    private Thread leader=null;

    public DelayQueue(){
    }

    public DelayQueue(Collection<? extends E> c){
        this.addAll(c);
    }

    public boolean add(E e){
        return offer(e);
    }

    public boolean offer(E e){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            q.offer(e);
            if(q.peek()==e){
                leader=null;
                available.signal();
            }
            return true;
        }finally{
            lock.unlock();
        }
    }

    public E poll(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            E first=q.peek();
            if(first==null||first.getDelay(NANOSECONDS)>0)
                return null;
            else
                return q.poll();
        }finally{
            lock.unlock();
        }
    }

    public E peek(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            return q.peek();
        }finally{
            lock.unlock();
        }
    }

    public void clear(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            q.clear();
        }finally{
            lock.unlock();
        }
    }

    public void put(E e){
        offer(e);
    }

    public boolean offer(E e,long timeout,TimeUnit unit){
        return offer(e);
    }

    public E take() throws InterruptedException{
        final ReentrantLock lock=this.lock;
        lock.lockInterruptibly();
        try{
            for(;;){
                E first=q.peek();
                if(first==null)
                    available.await();
                else{
                    long delay=first.getDelay(NANOSECONDS);
                    if(delay<=0)
                        return q.poll();
                    first=null; // don't retain ref while waiting
                    if(leader!=null)
                        available.await();
                    else{
                        Thread thisThread=Thread.currentThread();
                        leader=thisThread;
                        try{
                            available.awaitNanos(delay);
                        }finally{
                            if(leader==thisThread)
                                leader=null;
                        }
                    }
                }
            }
        }finally{
            if(leader==null&&q.peek()!=null)
                available.signal();
            lock.unlock();
        }
    }

    public E poll(long timeout,TimeUnit unit) throws InterruptedException{
        long nanos=unit.toNanos(timeout);
        final ReentrantLock lock=this.lock;
        lock.lockInterruptibly();
        try{
            for(;;){
                E first=q.peek();
                if(first==null){
                    if(nanos<=0)
                        return null;
                    else
                        nanos=available.awaitNanos(nanos);
                }else{
                    long delay=first.getDelay(NANOSECONDS);
                    if(delay<=0)
                        return q.poll();
                    if(nanos<=0)
                        return null;
                    first=null; // don't retain ref while waiting
                    if(nanos<delay||leader!=null)
                        nanos=available.awaitNanos(nanos);
                    else{
                        Thread thisThread=Thread.currentThread();
                        leader=thisThread;
                        try{
                            long timeLeft=available.awaitNanos(delay);
                            nanos-=delay-timeLeft;
                        }finally{
                            if(leader==thisThread)
                                leader=null;
                        }
                    }
                }
            }
        }finally{
            if(leader==null&&q.peek()!=null)
                available.signal();
            lock.unlock();
        }
    }

    public int remainingCapacity(){
        return Integer.MAX_VALUE;
    }

    public int drainTo(Collection<? super E> c){
        if(c==null)
            throw new NullPointerException();
        if(c==this)
            throw new IllegalArgumentException();
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            int n=0;
            for(E e;(e=peekExpired())!=null;){
                c.add(e);       // In this order, in case add() throws.
                q.poll();
                ++n;
            }
            return n;
        }finally{
            lock.unlock();
        }
    }

    private E peekExpired(){
        // assert lock.isHeldByCurrentThread();
        E first=q.peek();
        return (first==null||first.getDelay(NANOSECONDS)>0)?
                null:first;
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
            int n=0;
            for(E e;n<maxElements&&(e=peekExpired())!=null;){
                c.add(e);       // In this order, in case add() throws.
                q.poll();
                ++n;
            }
            return n;
        }finally{
            lock.unlock();
        }
    }

    void removeEQ(Object o){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            for(Iterator<E> it=q.iterator();it.hasNext();){
                if(o==it.next()){
                    it.remove();
                    break;
                }
            }
        }finally{
            lock.unlock();
        }
    }

    public Iterator<E> iterator(){
        return new Itr(toArray());
    }

    public int size(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            return q.size();
        }finally{
            lock.unlock();
        }
    }

    public Object[] toArray(){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            return q.toArray();
        }finally{
            lock.unlock();
        }
    }

    public <T> T[] toArray(T[] a){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            return q.toArray(a);
        }finally{
            lock.unlock();
        }
    }

    public boolean remove(Object o){
        final ReentrantLock lock=this.lock;
        lock.lock();
        try{
            return q.remove(o);
        }finally{
            lock.unlock();
        }
    }

    private class Itr implements Iterator<E>{
        final Object[] array; // Array of all elements
        int cursor;           // index of next element to return
        int lastRet;          // index of last element, or -1 if no such

        Itr(Object[] array){
            lastRet=-1;
            this.array=array;
        }

        public boolean hasNext(){
            return cursor<array.length;
        }

        @SuppressWarnings("unchecked")
        public E next(){
            if(cursor>=array.length)
                throw new NoSuchElementException();
            lastRet=cursor;
            return (E)array[cursor++];
        }

        public void remove(){
            if(lastRet<0)
                throw new IllegalStateException();
            removeEQ(array[lastRet]);
            lastRet=-1;
        }
    }
}
