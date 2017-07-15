/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.stream;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Collector<T,A,R>{
    public static <T,R> Collector<T,R,R> of(Supplier<R> supplier,
                                            BiConsumer<R,T> accumulator,
                                            BinaryOperator<R> combiner,
                                            Characteristics... characteristics){
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        Objects.requireNonNull(characteristics);
        Set<Characteristics> cs=(characteristics.length==0)
                ?Collectors.CH_ID
                :Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH,
                characteristics));
        return new Collectors.CollectorImpl<>(supplier,accumulator,combiner,cs);
    }

    public static <T,A,R> Collector<T,A,R> of(Supplier<A> supplier,
                                              BiConsumer<A,T> accumulator,
                                              BinaryOperator<A> combiner,
                                              Function<A,R> finisher,
                                              Characteristics... characteristics){
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        Objects.requireNonNull(finisher);
        Objects.requireNonNull(characteristics);
        Set<Characteristics> cs=Collectors.CH_NOID;
        if(characteristics.length>0){
            cs=EnumSet.noneOf(Characteristics.class);
            Collections.addAll(cs,characteristics);
            cs=Collections.unmodifiableSet(cs);
        }
        return new Collectors.CollectorImpl<>(supplier,accumulator,combiner,finisher,cs);
    }

    Supplier<A> supplier();

    BiConsumer<A,T> accumulator();

    BinaryOperator<A> combiner();

    Function<A,R> finisher();

    Set<Characteristics> characteristics();

    enum Characteristics{
        CONCURRENT,
        UNORDERED,
        IDENTITY_FINISH
    }
}
