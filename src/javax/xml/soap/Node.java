/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

public interface Node extends org.w3c.dom.Node{
    public String getValue();

    public void setValue(String value);

    public SOAPElement getParentElement();

    public void setParentElement(SOAPElement parent) throws SOAPException;

    public void detachNode();

    public void recycleNode();
}
