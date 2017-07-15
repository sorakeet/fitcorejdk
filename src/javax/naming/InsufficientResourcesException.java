/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class InsufficientResourcesException extends NamingException{
    private static final long serialVersionUID=6227672693037844532L;

    public InsufficientResourcesException(String explanation){
        super(explanation);
    }

    public InsufficientResourcesException(){
        super();
    }
}
