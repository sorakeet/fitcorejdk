/**
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.soap;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.Binding;
import java.util.Set;

public interface SOAPBinding extends Binding{
    public static final String SOAP11HTTP_BINDING="http://schemas.xmlsoap.org/wsdl/soap/http";
    public static final String SOAP12HTTP_BINDING="http://www.w3.org/2003/05/soap/bindings/HTTP/";
    public static final String SOAP11HTTP_MTOM_BINDING="http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true";
    public static final String SOAP12HTTP_MTOM_BINDING="http://www.w3.org/2003/05/soap/bindings/HTTP/?mtom=true";

    public Set<String> getRoles();

    public void setRoles(Set<String> roles);

    public boolean isMTOMEnabled();

    public void setMTOMEnabled(boolean flag);

    public SOAPFactory getSOAPFactory();

    public MessageFactory getMessageFactory();
}
