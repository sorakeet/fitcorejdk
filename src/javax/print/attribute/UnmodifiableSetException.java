/**
 * Copyright (c) 2001, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute;

public class UnmodifiableSetException extends RuntimeException{
    public UnmodifiableSetException(){
    }

    public UnmodifiableSetException(String message){
        super(message);
    }
}
