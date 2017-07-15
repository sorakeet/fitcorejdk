/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.xpath;

import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

public interface XPath{
    public void reset();

    public XPathVariableResolver getXPathVariableResolver();

    public void setXPathVariableResolver(XPathVariableResolver resolver);

    public XPathFunctionResolver getXPathFunctionResolver();

    public void setXPathFunctionResolver(XPathFunctionResolver resolver);

    public NamespaceContext getNamespaceContext();

    public void setNamespaceContext(NamespaceContext nsContext);

    public XPathExpression compile(String expression)
            throws XPathExpressionException;

    public Object evaluate(String expression,Object item,QName returnType)
            throws XPathExpressionException;

    public String evaluate(String expression,Object item)
            throws XPathExpressionException;

    public Object evaluate(
            String expression,
            InputSource source,
            QName returnType)
            throws XPathExpressionException;

    public String evaluate(String expression,InputSource source)
            throws XPathExpressionException;
}
