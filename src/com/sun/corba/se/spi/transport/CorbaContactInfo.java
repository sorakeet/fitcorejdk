/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.transport;

import com.sun.corba.se.pept.transport.ContactInfo;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.iiop.IIOPProfile;

public interface CorbaContactInfo
        extends
        ContactInfo{
    public IOR getTargetIOR();

    public IOR getEffectiveTargetIOR();

    public IIOPProfile getEffectiveProfile(); // REVISIT - type

    public short getAddressingDisposition();

    public void setAddressingDisposition(short addressingDisposition);

    public String getMonitoringName();
}
// End of file.
