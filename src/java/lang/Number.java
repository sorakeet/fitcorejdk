/**
 * Copyright (c) 1994, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

public abstract class Number implements java.io.Serializable{
    private static final long serialVersionUID=-8742448824652078965L;

    public abstract long longValue();

    public abstract float floatValue();

    public abstract double doubleValue();

    public byte byteValue(){
        return (byte)intValue();
    }

    public abstract int intValue();

    public short shortValue(){
        return (short)intValue();
    }
}
