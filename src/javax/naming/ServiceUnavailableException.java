/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class ServiceUnavailableException extends NamingException{
    private static final long serialVersionUID=-4996964726566773444L;

    public ServiceUnavailableException(String explanation){
        super(explanation);
    }

    public ServiceUnavailableException(){
        super();
    }
}
