/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

public interface Set<E> extends Collection<E>{
    // Query Operations

    int size();

    boolean isEmpty();

    boolean contains(Object o);

    Iterator<E> iterator();

    Object[] toArray();

    <T> T[] toArray(T[] a);
    // Modification Operations

    boolean add(E e);

    boolean remove(Object o);
    // Bulk Operations

    boolean containsAll(Collection<?> c);

    boolean addAll(Collection<? extends E> c);

    boolean removeAll(Collection<?> c);

    boolean retainAll(Collection<?> c);

    void clear();
    // Comparison and hashing

    boolean equals(Object o);

    int hashCode();

    @Override
    default Spliterator<E> spliterator(){
        return Spliterators.spliterator(this,Spliterator.DISTINCT);
    }
}
