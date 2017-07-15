/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.pept.encoding;

import com.sun.corba.se.pept.protocol.MessageMediator;

import java.io.IOException;

public interface InputObject{
    public MessageMediator getMessageMediator();

    public void setMessageMediator(MessageMediator messageMediator);

    public void close() throws IOException;
}
// End of file.
