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

import java.util.Deque;
import java.util.Iterator;

public interface BlockingDeque<E> extends BlockingQueue<E>, Deque<E>{
    void addFirst(E e);

    void addLast(E e);

    boolean offerFirst(E e);

    boolean offerLast(E e);

    boolean removeFirstOccurrence(Object o);

    boolean removeLastOccurrence(Object o);

    void push(E e);

    void putFirst(E e) throws InterruptedException;

    void putLast(E e) throws InterruptedException;

    boolean offerFirst(E e,long timeout,TimeUnit unit)
            throws InterruptedException;

    boolean offerLast(E e,long timeout,TimeUnit unit)
            throws InterruptedException;

    E takeFirst() throws InterruptedException;

    E takeLast() throws InterruptedException;

    E pollFirst(long timeout,TimeUnit unit)
            throws InterruptedException;
    // *** BlockingQueue methods ***

    E pollLast(long timeout,TimeUnit unit)
            throws InterruptedException;

    boolean add(E e);

    boolean offer(E e);

    void put(E e) throws InterruptedException;

    boolean offer(E e,long timeout,TimeUnit unit)
            throws InterruptedException;

    E take() throws InterruptedException;

    E poll(long timeout,TimeUnit unit)
            throws InterruptedException;

    boolean remove(Object o);

    public boolean contains(Object o);

    E remove();

    E poll();

    E element();

    E peek();

    public int size();
    // *** Stack methods ***

    Iterator<E> iterator();
}
