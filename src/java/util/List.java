/**
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

import java.util.function.UnaryOperator;

public interface List<E> extends Collection<E>{
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
    // Bulk Modification Operations

    boolean containsAll(Collection<?> c);

    boolean addAll(Collection<? extends E> c);

    boolean removeAll(Collection<?> c);

    boolean retainAll(Collection<?> c);

    void clear();

    boolean equals(Object o);

    int hashCode();

    @Override
    default Spliterator<E> spliterator(){
        return Spliterators.spliterator(this,Spliterator.ORDERED);
    }
    // Comparison and hashing

    boolean addAll(int index,Collection<? extends E> c);

    default void replaceAll(UnaryOperator<E> operator){
        Objects.requireNonNull(operator);
        final ListIterator<E> li=this.listIterator();
        while(li.hasNext()){
            li.set(operator.apply(li.next()));
        }
    }
    // Positional Access Operations

    ListIterator<E> listIterator();

    @SuppressWarnings({"unchecked","rawtypes"})
    default void sort(Comparator<? super E> c){
        Object[] a=this.toArray();
        Arrays.sort(a,(Comparator)c);
        ListIterator<E> i=this.listIterator();
        for(Object e : a){
            i.next();
            i.set((E)e);
        }
    }

    E get(int index);

    E set(int index,E element);
    // Search Operations

    void add(int index,E element);

    E remove(int index);
    // List Iterators

    int indexOf(Object o);

    int lastIndexOf(Object o);
    // View

    ListIterator<E> listIterator(int index);

    List<E> subList(int fromIndex,int toIndex);
}
