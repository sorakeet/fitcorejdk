/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;

public class InvalidOpenTypeException extends IllegalArgumentException{
    private static final long serialVersionUID=-2837312755412327534L;

    public InvalidOpenTypeException(){
        super();
    }

    public InvalidOpenTypeException(String msg){
        super(msg);
    }
}
