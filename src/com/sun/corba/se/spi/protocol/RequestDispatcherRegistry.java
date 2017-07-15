/**
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.protocol;

import com.sun.corba.se.pept.protocol.ClientRequestDispatcher;
import com.sun.corba.se.spi.oa.ObjectAdapterFactory;

import java.util.Set;

public interface RequestDispatcherRegistry{
    void registerClientRequestDispatcher(ClientRequestDispatcher csc,int scid);

    ClientRequestDispatcher getClientRequestDispatcher(int scid);

    void registerLocalClientRequestDispatcherFactory(LocalClientRequestDispatcherFactory csc,int scid);

    LocalClientRequestDispatcherFactory getLocalClientRequestDispatcherFactory(int scid);

    void registerServerRequestDispatcher(CorbaServerRequestDispatcher ssc,int scid);

    CorbaServerRequestDispatcher getServerRequestDispatcher(int scid);

    void registerServerRequestDispatcher(CorbaServerRequestDispatcher ssc,String name);

    CorbaServerRequestDispatcher getServerRequestDispatcher(String name);

    void registerObjectAdapterFactory(ObjectAdapterFactory oaf,int scid);

    ObjectAdapterFactory getObjectAdapterFactory(int scid);

    Set<ObjectAdapterFactory> getObjectAdapterFactories();
}
