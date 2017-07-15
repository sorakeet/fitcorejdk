/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class NotCompliantMBeanException extends OperationsException{
    private static final long serialVersionUID=5175579583207963577L;

    public NotCompliantMBeanException(){
        super();
    }

    public NotCompliantMBeanException(String message){
        super(message);
    }
}
