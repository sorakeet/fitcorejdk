/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
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

import org.omg.CORBA.SystemException;

public class IndirectionException extends SystemException{
    public int offset;

    public IndirectionException(int offset){
        super("",0,org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        this.offset=offset;
    }
}
