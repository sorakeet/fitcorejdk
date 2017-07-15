/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;
//RI import

public class ServiceNotFoundException extends OperationsException{
    private static final long serialVersionUID=-3990675661956646827L;

    public ServiceNotFoundException(){
        super();
    }

    public ServiceNotFoundException(String message){
        super(message);
    }
}
