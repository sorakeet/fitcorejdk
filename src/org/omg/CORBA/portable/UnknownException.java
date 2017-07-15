/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */
package org.omg.CORBA.portable;

public class UnknownException extends org.omg.CORBA.SystemException{
    public Throwable originalEx;

    public UnknownException(Throwable ex){
        super("",0,org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        originalEx=ex;
    }
}
