/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface IntUnaryOperator{
    static IntUnaryOperator identity(){
        return t->t;
    }

    default IntUnaryOperator compose(IntUnaryOperator before){
        Objects.requireNonNull(before);
        return (int v)->applyAsInt(before.applyAsInt(v));
    }

    int applyAsInt(int operand);

    default IntUnaryOperator andThen(IntUnaryOperator after){
        Objects.requireNonNull(after);
        return (int t)->after.applyAsInt(applyAsInt(t));
    }
}
