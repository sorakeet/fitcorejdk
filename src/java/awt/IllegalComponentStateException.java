/**
 * Copyright (c) 1996, 1997, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public class IllegalComponentStateException extends IllegalStateException{
    private static final long serialVersionUID=-1889339587208144238L;

    public IllegalComponentStateException(){
        super();
    }

    public IllegalComponentStateException(String s){
        super(s);
    }
}
