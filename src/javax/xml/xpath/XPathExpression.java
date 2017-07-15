/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.xpath;

import org.xml.sax.InputSource;

import javax.xml.namespace.QName;

public interface XPathExpression{
    public Object evaluate(Object item,QName returnType)
            throws XPathExpressionException;

    public String evaluate(Object item)
            throws XPathExpressionException;

    public Object evaluate(InputSource source,QName returnType)
            throws XPathExpressionException;

    public String evaluate(InputSource source)
            throws XPathExpressionException;
}
