/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.classfile;

public interface AttributeReader{
    public Attribute createAttribute(int name_index,
                                     int length,
                                     java.io.DataInputStream file,
                                     ConstantPool constant_pool);
}
