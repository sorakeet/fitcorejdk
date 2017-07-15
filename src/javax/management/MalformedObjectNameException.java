/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class MalformedObjectNameException extends OperationsException{
    private static final long serialVersionUID=-572689714442915824L;

    public MalformedObjectNameException(){
        super();
    }

    public MalformedObjectNameException(String message){
        super(message);
    }
}
