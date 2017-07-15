/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface LongUnaryOperator{
    static LongUnaryOperator identity(){
        return t->t;
    }

    default LongUnaryOperator compose(LongUnaryOperator before){
        Objects.requireNonNull(before);
        return (long v)->applyAsLong(before.applyAsLong(v));
    }

    long applyAsLong(long operand);

    default LongUnaryOperator andThen(LongUnaryOperator after){
        Objects.requireNonNull(after);
        return (long t)->after.applyAsLong(applyAsLong(t));
    }
}
