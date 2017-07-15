/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.pept.protocol;

import java.util.Iterator;

public interface ClientInvocationInfo{
    public Iterator getContactInfoListIterator();

    public void setContactInfoListIterator(Iterator contactInfoListIterator);

    public boolean isRetryInvocation();

    public void setIsRetryInvocation(boolean isRetryInvocation);

    public int getEntryCount();

    public void incrementEntryCount();

    public void decrementEntryCount();

    public ClientRequestDispatcher getClientRequestDispatcher();

    public void setClientRequestDispatcher(ClientRequestDispatcher clientRequestDispatcher);

    public MessageMediator getMessageMediator();

    public void setMessageMediator(MessageMediator messageMediator);
}
// End of file.
