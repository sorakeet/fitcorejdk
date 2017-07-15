/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class InvalidAttributeValueException extends OperationsException{
    private static final long serialVersionUID=2164571879317142449L;

    public InvalidAttributeValueException(){
        super();
    }

    public InvalidAttributeValueException(String message){
        super(message);
    }
}
