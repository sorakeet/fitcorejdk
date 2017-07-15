/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.pept.transport;

import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.protocol.MessageMediator;

public interface Connection{
    public boolean shouldRegisterReadEvent();

    public boolean shouldRegisterServerReadEvent(); // REVISIT - why special?

    public boolean read();

    public void close();
    // REVISIT: replace next two with PlugInFactory (implemented by ContactInfo
    // and Acceptor).

    public Acceptor getAcceptor();

    public ContactInfo getContactInfo();

    public EventHandler getEventHandler();

    public boolean isServer();

    public boolean isBusy();

    public long getTimeStamp();

    public void setTimeStamp(long time);

    public void setState(String state);

    public void writeLock();

    public void writeUnlock();

    public void sendWithoutLock(OutputObject outputObject);

    public void registerWaiter(MessageMediator messageMediator);

    public InputObject waitForResponse(MessageMediator messageMediator);

    public void unregisterWaiter(MessageMediator messageMediator);

    public ConnectionCache getConnectionCache();

    public void setConnectionCache(ConnectionCache connectionCache);
}
// End of file.
