/**
 * Copyright (c) 2001, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.pept.protocol;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.pept.transport.ContactInfo;

public interface MessageMediator{
    public Broker getBroker();

    public ContactInfo getContactInfo();

    public Connection getConnection();

    public void initializeMessage();

    public void finishSendingRequest();

    @Deprecated
    public InputObject waitForResponse();

    public OutputObject getOutputObject();

    public void setOutputObject(OutputObject outputObject);

    public InputObject getInputObject();

    public void setInputObject(InputObject inputObject);
}
// End of file.
