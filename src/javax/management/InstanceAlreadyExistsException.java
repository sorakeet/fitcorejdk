/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class InstanceAlreadyExistsException extends OperationsException{
    private static final long serialVersionUID=8893743928912733931L;

    public InstanceAlreadyExistsException(){
        super();
    }

    public InstanceAlreadyExistsException(String message){
        super(message);
    }
}
