/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

public interface Name{
    String getLocalName();

    String getQualifiedName();

    String getPrefix();

    String getURI();
}
