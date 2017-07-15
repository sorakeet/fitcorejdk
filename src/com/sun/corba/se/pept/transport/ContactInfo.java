/**
 * Copyright (c) 2001, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.pept.transport;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.protocol.ClientRequestDispatcher;
import com.sun.corba.se.pept.protocol.MessageMediator;

public interface ContactInfo{
    public Broker getBroker();

    public ContactInfoList getContactInfoList();

    public ClientRequestDispatcher getClientRequestDispatcher();

    public boolean isConnectionBased();

    public boolean shouldCacheConnection();

    public String getConnectionCacheType();

    public OutboundConnectionCache getConnectionCache();

    public void setConnectionCache(OutboundConnectionCache connectionCache);

    public Connection createConnection();

    public MessageMediator createMessageMediator(Broker broker,
                                                 ContactInfo contactInfo,
                                                 Connection connection,
                                                 String methodName,
                                                 boolean isOneWay);

    public MessageMediator createMessageMediator(Broker broker,
                                                 Connection connection);

    public MessageMediator finishCreatingMessageMediator(Broker broker,
                                                         Connection connection,
                                                         MessageMediator messageMediator);

    public InputObject createInputObject(Broker broker,
                                         MessageMediator messageMediator);

    public OutputObject createOutputObject(MessageMediator messageMediator);

    public int hashCode();
}
// End of file.
