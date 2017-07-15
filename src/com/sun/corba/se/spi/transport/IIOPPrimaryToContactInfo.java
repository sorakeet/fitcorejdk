/**
 * Copyright (c) 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.transport;

import com.sun.corba.se.pept.transport.ContactInfo;

import java.util.List;

public interface IIOPPrimaryToContactInfo{
    public void reset(ContactInfo primary);

    public boolean hasNext(ContactInfo primary,
                           ContactInfo previous,
                           List contactInfos);

    public ContactInfo next(ContactInfo primary,
                            ContactInfo previous,
                            List contactInfos);
}
// End of file.
