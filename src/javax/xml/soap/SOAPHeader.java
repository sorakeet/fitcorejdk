/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

import javax.xml.namespace.QName;
import java.util.Iterator;

public interface SOAPHeader extends SOAPElement{
    public SOAPHeaderElement addHeaderElement(Name name)
            throws SOAPException;

    public SOAPHeaderElement addHeaderElement(QName qname)
            throws SOAPException;

    public Iterator examineMustUnderstandHeaderElements(String actor);

    public Iterator examineHeaderElements(String actor);

    public Iterator extractHeaderElements(String actor);

    public SOAPHeaderElement addNotUnderstoodHeaderElement(QName name)
            throws SOAPException;

    public SOAPHeaderElement addUpgradeHeaderElement(Iterator supportedSOAPURIs)
            throws SOAPException;

    public SOAPHeaderElement addUpgradeHeaderElement(String[] supportedSoapUris)
            throws SOAPException;

    public SOAPHeaderElement addUpgradeHeaderElement(String supportedSoapUri)
            throws SOAPException;

    public Iterator examineAllHeaderElements();

    public Iterator extractAllHeaderElements();
}
