/**
 * Copyright (c) 1994, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

public class ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException{
    private static final long serialVersionUID=-5116101128118950844L;

    public ArrayIndexOutOfBoundsException(){
        super();
    }

    public ArrayIndexOutOfBoundsException(int index){
        super("Array index out of range: "+index);
    }

    public ArrayIndexOutOfBoundsException(String s){
        super(s);
    }
}
