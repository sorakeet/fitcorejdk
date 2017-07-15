/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.pept.transport;

import java.util.Iterator;

public interface ContactInfoListIterator
        extends
        Iterator{
    public ContactInfoList getContactInfoList();

    public void reportSuccess(ContactInfo contactInfo);

    public boolean reportException(ContactInfo contactInfo,
                                   RuntimeException exception);

    public RuntimeException getFailureException();
}
// End of file.
