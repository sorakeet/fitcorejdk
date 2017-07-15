/**
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.handler.soap;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import java.util.Set;

public interface SOAPMessageContext
        extends javax.xml.ws.handler.MessageContext{
    public SOAPMessage getMessage();

    public void setMessage(SOAPMessage message);

    public Object[] getHeaders(QName header,JAXBContext context,
                               boolean allRoles);

    public Set<String> getRoles();
}
