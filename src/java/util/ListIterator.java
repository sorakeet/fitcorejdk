/**
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

public interface ListIterator<E> extends Iterator<E>{
    // Query Operations

    boolean hasNext();

    E next();

    void remove();

    boolean hasPrevious();

    E previous();

    int nextIndex();
    // Modification Operations

    int previousIndex();

    void set(E e);

    void add(E e);
}
