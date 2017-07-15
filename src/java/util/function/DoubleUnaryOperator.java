/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface DoubleUnaryOperator{
    static DoubleUnaryOperator identity(){
        return t->t;
    }

    default DoubleUnaryOperator compose(DoubleUnaryOperator before){
        Objects.requireNonNull(before);
        return (double v)->applyAsDouble(before.applyAsDouble(v));
    }

    double applyAsDouble(double operand);

    default DoubleUnaryOperator andThen(DoubleUnaryOperator after){
        Objects.requireNonNull(after);
        return (double t)->after.applyAsDouble(applyAsDouble(t));
    }
}
