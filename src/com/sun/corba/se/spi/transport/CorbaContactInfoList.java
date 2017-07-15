/**
 * Copyright (c) 2002, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.transport;

import com.sun.corba.se.pept.transport.ContactInfoList;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcher;

public interface CorbaContactInfoList
        extends
        ContactInfoList{
    public IOR getTargetIOR();

    public void setTargetIOR(IOR ior);

    public IOR getEffectiveTargetIOR();

    public void setEffectiveTargetIOR(IOR locatedIor);

    public LocalClientRequestDispatcher getLocalClientRequestDispatcher();

    public int hashCode();
}
// End of file.
