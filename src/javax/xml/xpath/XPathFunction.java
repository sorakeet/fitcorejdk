/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.xpath;

import java.util.List;

public interface XPathFunction{
    public Object evaluate(List args)
            throws XPathFunctionException;
}
