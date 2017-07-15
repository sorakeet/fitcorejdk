/**
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

import org.w3c.dom.Element;

import javax.xml.ws.handler.MessageContext;
import java.security.Principal;

public interface WebServiceContext{
    public MessageContext getMessageContext();

    public Principal getUserPrincipal();

    public boolean isUserInRole(String role);

    public EndpointReference getEndpointReference(Element... referenceParameters);

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz,
                                                                Element... referenceParameters);
}
