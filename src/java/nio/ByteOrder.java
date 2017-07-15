/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio;

public final class ByteOrder{
    public static final ByteOrder BIG_ENDIAN
            =new ByteOrder("BIG_ENDIAN");
    public static final ByteOrder LITTLE_ENDIAN
            =new ByteOrder("LITTLE_ENDIAN");
    private String name;

    private ByteOrder(String name){
        this.name=name;
    }

    public static ByteOrder nativeOrder(){
        return Bits.byteOrder();
    }

    public String toString(){
        return name;
    }
}
