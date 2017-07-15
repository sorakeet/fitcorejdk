/**
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.function;

import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface BinaryOperator<T> extends BiFunction<T,T,T>{
    static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator){
        Objects.requireNonNull(comparator);
        return (a,b)->comparator.compare(a,b)<=0?a:b;
    }

    static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator){
        Objects.requireNonNull(comparator);
        return (a,b)->comparator.compare(a,b)>=0?a:b;
    }
}
