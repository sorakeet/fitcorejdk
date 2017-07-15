/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class OperationNotSupportedException extends NamingException{
    private static final long serialVersionUID=5493232822427682064L;

    public OperationNotSupportedException(){
        super();
    }

    public OperationNotSupportedException(String explanation){
        super(explanation);
    }
}
