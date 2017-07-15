/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.pept.protocol;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.transport.ContactInfo;

public interface ClientRequestDispatcher{
    public OutputObject beginRequest(Object self,
                                     String methodName,
                                     boolean isOneWay,
                                     ContactInfo contactInfo);

    public InputObject marshalingComplete(Object self,
                                          OutputObject outputObject)
        // REVISIT EXCEPTIONS
            throws
            org.omg.CORBA.portable.ApplicationException,
            org.omg.CORBA.portable.RemarshalException;

    public void endRequest(Broker broker,
                           Object self,
                           InputObject inputObject);
}
// End of file.
