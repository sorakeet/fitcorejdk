/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.xpath;

public class XPathFactoryConfigurationException extends XPathException{
    private static final long serialVersionUID=-1837080260374986980L;

    public XPathFactoryConfigurationException(String message){
        super(message);
    }

    public XPathFactoryConfigurationException(Throwable cause){
        super(cause);
    }
}
