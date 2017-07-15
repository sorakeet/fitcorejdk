/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.namespace;

import java.util.Iterator;

public interface NamespaceContext{
    String getNamespaceURI(String prefix);

    String getPrefix(String namespaceURI);

    Iterator getPrefixes(String namespaceURI);
}
