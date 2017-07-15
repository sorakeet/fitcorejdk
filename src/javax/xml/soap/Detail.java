/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

import javax.xml.namespace.QName;
import java.util.Iterator;

public interface Detail extends SOAPFaultElement{
    public DetailEntry addDetailEntry(Name name) throws SOAPException;

    public DetailEntry addDetailEntry(QName qname) throws SOAPException;

    public Iterator getDetailEntries();
}
