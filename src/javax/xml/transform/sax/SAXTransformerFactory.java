/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform.sax;

import org.xml.sax.XMLFilter;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

public abstract class SAXTransformerFactory extends TransformerFactory{
    public static final String FEATURE=
            "http://javax.xml.transform.sax.SAXTransformerFactory/feature";
    public static final String FEATURE_XMLFILTER=
            "http://javax.xml.transform.sax.SAXTransformerFactory/feature/xmlfilter";

    protected SAXTransformerFactory(){
    }

    public abstract TransformerHandler newTransformerHandler(Source src)
            throws TransformerConfigurationException;

    public abstract TransformerHandler newTransformerHandler(
            Templates templates) throws TransformerConfigurationException;

    public abstract TransformerHandler newTransformerHandler()
            throws TransformerConfigurationException;

    public abstract TemplatesHandler newTemplatesHandler()
            throws TransformerConfigurationException;

    public abstract XMLFilter newXMLFilter(Source src)
            throws TransformerConfigurationException;

    public abstract XMLFilter newXMLFilter(Templates templates)
            throws TransformerConfigurationException;
}
