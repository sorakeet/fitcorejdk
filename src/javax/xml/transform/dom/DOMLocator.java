/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform.dom;

import org.w3c.dom.Node;

import javax.xml.transform.SourceLocator;

public interface DOMLocator extends SourceLocator{
    public Node getOriginatingNode();
}
