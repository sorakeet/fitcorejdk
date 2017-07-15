/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

import java.util.function.Consumer;

public interface Iterator<E>{
    default void remove(){
        throw new UnsupportedOperationException("remove");
    }

    default void forEachRemaining(Consumer<? super E> action){
        Objects.requireNonNull(action);
        while(hasNext())
            action.accept(next());
    }

    boolean hasNext();

    E next();
}
