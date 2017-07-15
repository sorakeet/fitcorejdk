/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.classfile;

public class ClassFormatException extends RuntimeException{
    public ClassFormatException(){
        super();
    }

    public ClassFormatException(String s){
        super(s);
    }
}
