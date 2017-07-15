/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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
 */
/**
 * Copyright 2000-2002,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.xni;

import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;

public interface XMLDocumentHandler{
    //
    // XMLDocumentHandler methods
    //

    public void startDocument(XMLLocator locator,String encoding,
                              NamespaceContext namespaceContext,
                              Augmentations augs)
            throws XNIException;

    public void xmlDecl(String version,String encoding,String standalone,Augmentations augs)
            throws XNIException;

    public void doctypeDecl(String rootElement,String publicId,String systemId,Augmentations augs)
            throws XNIException;

    public void comment(XMLString text,Augmentations augs) throws XNIException;

    public void processingInstruction(String target,XMLString data,Augmentations augs)
            throws XNIException;

    public void startElement(QName element,XMLAttributes attributes,Augmentations augs)
            throws XNIException;

    public void emptyElement(QName element,XMLAttributes attributes,Augmentations augs)
            throws XNIException;

    public void startGeneralEntity(String name,
                                   XMLResourceIdentifier identifier,
                                   String encoding,
                                   Augmentations augs) throws XNIException;

    public void textDecl(String version,String encoding,Augmentations augs) throws XNIException;

    public void endGeneralEntity(String name,Augmentations augs) throws XNIException;

    public void characters(XMLString text,Augmentations augs) throws XNIException;

    public void ignorableWhitespace(XMLString text,Augmentations augs) throws XNIException;

    public void endElement(QName element,Augmentations augs) throws XNIException;

    public void startCDATA(Augmentations augs) throws XNIException;

    public void endCDATA(Augmentations augs) throws XNIException;

    public void endDocument(Augmentations augs) throws XNIException;

    public XMLDocumentSource getDocumentSource();

    public void setDocumentSource(XMLDocumentSource source);
} // interface XMLDocumentHandler
