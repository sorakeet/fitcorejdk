/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.oa;

import com.sun.corba.se.spi.ior.IORTemplate;
import com.sun.corba.se.spi.orb.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableInterceptor.ObjectReferenceFactory;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;
// REVISIT: What should the order be?  enter/push...pop/exit?

public interface ObjectAdapter{
    ////////////////////////////////////////////////////////////////////////////
    // Basic methods for supporting interceptors
    ////////////////////////////////////////////////////////////////////////////

    ORB getORB();

    Policy getEffectivePolicy(int type);

    IORTemplate getIORTemplate();
    ////////////////////////////////////////////////////////////////////////////
    // Methods needed to support ORT.
    ////////////////////////////////////////////////////////////////////////////

    int getManagerId();

    short getState();

    ObjectReferenceTemplate getAdapterTemplate();

    ObjectReferenceFactory getCurrentFactory();

    void setCurrentFactory(ObjectReferenceFactory factory);
    ////////////////////////////////////////////////////////////////////////////
    // Methods required for dispatching to servants
    ////////////////////////////////////////////////////////////////////////////

    org.omg.CORBA.Object getLocalServant(byte[] objectId);

    void getInvocationServant(OAInvocationInfo info);

    void enter() throws OADestroyed;

    void exit();

    public void returnServant();

    OAInvocationInfo makeInvocationInfo(byte[] objectId);

    String[] getInterfaces(Object servant,byte[] objectId);
}
