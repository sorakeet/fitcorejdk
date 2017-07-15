/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.soap;

import javax.xml.soap.SOAPFault;

public class SOAPFaultException extends javax.xml.ws.ProtocolException{
    private SOAPFault fault;

    public SOAPFaultException(SOAPFault fault){
        super(fault.getFaultString());
        this.fault=fault;
    }

    public SOAPFault getFault(){
        return this.fault;
    }
}
