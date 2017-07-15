/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.function;

@FunctionalInterface
public interface UnaryOperator<T> extends Function<T,T>{
    static <T> UnaryOperator<T> identity(){
        return t->t;
    }
}
