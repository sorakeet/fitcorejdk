/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

public interface ValidationEventLocator{
    public java.net.URL getURL();

    public int getOffset();

    public int getLineNumber();

    public int getColumnNumber();

    public Object getObject();

    public org.w3c.dom.Node getNode();
}
