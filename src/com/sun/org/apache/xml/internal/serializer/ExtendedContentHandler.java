/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2003-2004 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: ExtendedContentHandler.java,v 1.2.4.1 2005/09/15 08:15:17 suresh_emailid Exp $
 */
/**
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: ExtendedContentHandler.java,v 1.2.4.1 2005/09/15 08:15:17 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import org.xml.sax.SAXException;

import javax.xml.transform.SourceLocator;

abstract interface ExtendedContentHandler extends org.xml.sax.ContentHandler{
    // Bit constants for addUniqueAttribute().
    // The attribute value contains no bad characters. A "bad" character is one which
    // is greater than 126 or it is one of '<', '>', '&' or '"'.
    public static final int NO_BAD_CHARS=0x1;
    // An HTML empty attribute (e.g. <OPTION selected>).
    public static final int HTML_ATTREMPTY=0x2;
    // An HTML URL attribute
    public static final int HTML_ATTRURL=0x4;

    public void addAttribute(
            String uri,
            String localName,
            String rawName,
            String type,
            String value,
            boolean XSLAttribute)
            throws SAXException;

    public void addAttributes(org.xml.sax.Attributes atts)
            throws SAXException;

    public void addAttribute(String qName,String value);

    public void characters(String chars) throws SAXException;

    public void characters(org.w3c.dom.Node node) throws SAXException;

    public void endElement(String elemName) throws SAXException;

    public void startElement(String uri,String localName,String qName)
            throws SAXException;

    public void startElement(String qName) throws SAXException;

    public void namespaceAfterStartElement(String uri,String prefix)
            throws SAXException;

    public boolean startPrefixMapping(
            String prefix,
            String uri,
            boolean shouldFlush)
            throws SAXException;

    public void entityReference(String entityName) throws SAXException;

    public NamespaceMappings getNamespaceMappings();

    public String getPrefix(String uri);

    public String getNamespaceURI(String name,boolean isElement);

    public String getNamespaceURIFromPrefix(String prefix);

    public void setSourceLocator(SourceLocator locator);

    public void addUniqueAttribute(String qName,String value,int flags)
            throws SAXException;

    public void addXSLAttribute(String qName,final String value,final String uri);

    public void addAttribute(
            String uri,
            String localName,
            String rawName,
            String type,
            String value)
            throws SAXException;
}
