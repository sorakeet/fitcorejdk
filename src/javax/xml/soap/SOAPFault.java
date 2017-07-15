/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Locale;

public interface SOAPFault extends SOAPBodyElement{
    public void setFaultCode(Name faultCodeQName) throws SOAPException;

    public void setFaultCode(QName faultCodeQName) throws SOAPException;

    public Name getFaultCodeAsName();

    public QName getFaultCodeAsQName();

    public Iterator getFaultSubcodes();

    public void removeAllFaultSubcodes();

    public void appendFaultSubcode(QName subcode) throws SOAPException;

    public String getFaultCode();

    public void setFaultCode(String faultCode) throws SOAPException;

    public String getFaultActor();

    public void setFaultActor(String faultActor) throws SOAPException;

    public void setFaultString(String faultString,Locale locale)
            throws SOAPException;

    public String getFaultString();

    public void setFaultString(String faultString) throws SOAPException;

    public Locale getFaultStringLocale();

    public boolean hasDetail();

    public Detail getDetail();

    public Detail addDetail() throws SOAPException;

    public Iterator getFaultReasonLocales() throws SOAPException;

    public Iterator getFaultReasonTexts() throws SOAPException;

    public String getFaultReasonText(Locale locale) throws SOAPException;

    public void addFaultReasonText(String text,Locale locale)
            throws SOAPException;

    public String getFaultNode();

    public void setFaultNode(String uri) throws SOAPException;

    public String getFaultRole();

    public void setFaultRole(String uri) throws SOAPException;
}
