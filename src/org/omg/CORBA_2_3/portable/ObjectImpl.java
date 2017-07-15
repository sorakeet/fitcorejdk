/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
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
package org.omg.CORBA_2_3.portable;

public abstract class ObjectImpl extends org.omg.CORBA.portable.ObjectImpl{
    public String _get_codebase(){
        org.omg.CORBA.portable.Delegate delegate=_get_delegate();
        if(delegate instanceof Delegate)
            return ((Delegate)delegate).get_codebase(this);
        return null;
    }
}
