/**
 * Copyright (c) 1998, 2003, Oracle and/or its affiliates. All rights reserved.
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
package org.omg.CORBA_2_3;

public abstract class ORB extends org.omg.CORBA.ORB{
    public org.omg.CORBA.portable.ValueFactory register_value_factory(String id,
                                                                      org.omg.CORBA.portable.ValueFactory factory){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void unregister_value_factory(String id){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.portable.ValueFactory lookup_value_factory(String id){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    // always return a ValueDef or throw BAD_PARAM if
    // <em>repid</em> does not represent a valuetype
    public org.omg.CORBA.Object get_value_def(String repid)
            throws org.omg.CORBA.BAD_PARAM{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void set_delegate(Object wrapper){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
