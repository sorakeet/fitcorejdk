/**
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.daemon;
// java import
//

public class CommunicationException extends javax.management.JMRuntimeException{
    private static final long serialVersionUID=-2499186113233316177L;

    public CommunicationException(Throwable target){
        super(target.getMessage());
        initCause(target);
    }

    public CommunicationException(Throwable target,String msg){
        super(msg);
        initCause(target);
    }

    public CommunicationException(String msg){
        super(msg);
    }

    public Throwable getTargetException(){
        return getCause();
    }
}
