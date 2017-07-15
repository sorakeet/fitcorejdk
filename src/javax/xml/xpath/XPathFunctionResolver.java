/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.xpath;

import javax.xml.namespace.QName;

public interface XPathFunctionResolver{
    public XPathFunction resolveFunction(QName functionName,int arity);
}
