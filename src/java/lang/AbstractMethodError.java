/**
 * Copyright (c) 1994, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

public class AbstractMethodError extends IncompatibleClassChangeError{
    private static final long serialVersionUID=-1654391082989018462L;

    public AbstractMethodError(){
        super();
    }

    public AbstractMethodError(String s){
        super(s);
    }
}
