/**
 * Copyright (c) 2001, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.pept.transport;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.protocol.MessageMediator;

public interface Acceptor{
    public boolean initialize();

    public boolean initialized();

    public String getConnectionCacheType();

    public InboundConnectionCache getConnectionCache();

    public void setConnectionCache(InboundConnectionCache connectionCache);

    public boolean shouldRegisterAcceptEvent();

    public void accept();

    public void close();

    public EventHandler getEventHandler();
    //
    // Factory methods
    //
    // REVISIT: Identical to ContactInfo method.  Refactor into base interface.

    public MessageMediator createMessageMediator(Broker xbroker,
                                                 Connection xconnection);
    // REVISIT: Identical to ContactInfo method.  Refactor into base interface.

    public MessageMediator finishCreatingMessageMediator(Broker broker,
                                                         Connection xconnection,
                                                         MessageMediator messageMediator);

    public InputObject createInputObject(Broker broker,
                                         MessageMediator messageMediator);

    public OutputObject createOutputObject(Broker broker,
                                           MessageMediator messageMediator);
    //
    // Usage dictates implementation equals and hashCode.
    //
}
// End of file.
