/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.naming.cosnaming;
// Import general CORBA classes

import org.omg.CORBA.Object;
import org.omg.CosNaming.*;
import org.omg.PortableServer.POA;
// Import org.omg.CosNaming classes

public interface NamingContextDataStore{
    void Bind(NameComponent n,Object obj,BindingType bt)
            throws org.omg.CORBA.SystemException;

    Object Resolve(NameComponent n,BindingTypeHolder bth)
            throws org.omg.CORBA.SystemException;

    Object Unbind(NameComponent n)
            throws org.omg.CORBA.SystemException;

    void List(int how_many,BindingListHolder bl,BindingIteratorHolder bi)
            throws org.omg.CORBA.SystemException;

    NamingContext NewContext()
            throws org.omg.CORBA.SystemException;

    void Destroy()
            throws org.omg.CORBA.SystemException;

    boolean IsEmpty();

    POA getNSPOA();
}
