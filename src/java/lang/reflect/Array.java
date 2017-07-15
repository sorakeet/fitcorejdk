/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.reflect;

public final class Array{
    private Array(){
    }

    public static Object newInstance(Class<?> componentType,int length)
            throws NegativeArraySizeException{
        return newArray(componentType,length);
    }

    private static native Object newArray(Class<?> componentType,int length)
            throws NegativeArraySizeException;

    public static Object newInstance(Class<?> componentType,int... dimensions)
            throws IllegalArgumentException, NegativeArraySizeException{
        return multiNewArray(componentType,dimensions);
    }

    private static native Object multiNewArray(Class<?> componentType,
                                               int[] dimensions)
            throws IllegalArgumentException, NegativeArraySizeException;

    public static native int getLength(Object array)
            throws IllegalArgumentException;

    public static native Object get(Object array,int index)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native boolean getBoolean(Object array,int index)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native byte getByte(Object array,int index)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native char getChar(Object array,int index)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native short getShort(Object array,int index)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native int getInt(Object array,int index)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native long getLong(Object array,int index)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native float getFloat(Object array,int index)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native double getDouble(Object array,int index)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native void set(Object array,int index,Object value)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native void setBoolean(Object array,int index,boolean z)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native void setByte(Object array,int index,byte b)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native void setChar(Object array,int index,char c)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native void setShort(Object array,int index,short s)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native void setInt(Object array,int index,int i)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native void setLong(Object array,int index,long l)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native void setFloat(Object array,int index,float f)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static native void setDouble(Object array,int index,double d)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
}
