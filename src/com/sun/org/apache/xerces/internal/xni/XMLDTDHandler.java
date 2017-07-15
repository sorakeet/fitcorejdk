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

import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource;

public interface XMLDTDHandler{
    //
    // Constants
    //
    public static final short CONDITIONAL_INCLUDE=0;
    public static final short CONDITIONAL_IGNORE=1;
    //
    // XMLDTDHandler methods
    //

    public void startDTD(XMLLocator locator,Augmentations augmentations)
            throws XNIException;

    public void startParameterEntity(String name,
                                     XMLResourceIdentifier identifier,
                                     String encoding,
                                     Augmentations augmentations) throws XNIException;

    public void textDecl(String version,String encoding,
                         Augmentations augmentations) throws XNIException;

    public void endParameterEntity(String name,Augmentations augmentations)
            throws XNIException;

    public void startExternalSubset(XMLResourceIdentifier identifier,
                                    Augmentations augmentations)
            throws XNIException;

    public void endExternalSubset(Augmentations augmentations)
            throws XNIException;

    public void comment(XMLString text,Augmentations augmentations)
            throws XNIException;

    public void processingInstruction(String target,XMLString data,
                                      Augmentations augmentations)
            throws XNIException;

    public void elementDecl(String name,String contentModel,
                            Augmentations augmentations)
            throws XNIException;

    public void startAttlist(String elementName,
                             Augmentations augmentations) throws XNIException;

    public void attributeDecl(String elementName,String attributeName,
                              String type,String[] enumeration,
                              String defaultType,XMLString defaultValue,
                              XMLString nonNormalizedDefaultValue,Augmentations augmentations)
            throws XNIException;

    public void endAttlist(Augmentations augmentations) throws XNIException;

    public void internalEntityDecl(String name,XMLString text,
                                   XMLString nonNormalizedText,
                                   Augmentations augmentations)
            throws XNIException;

    public void externalEntityDecl(String name,
                                   XMLResourceIdentifier identifier,
                                   Augmentations augmentations)
            throws XNIException;

    public void unparsedEntityDecl(String name,
                                   XMLResourceIdentifier identifier,
                                   String notation,Augmentations augmentations)
            throws XNIException;

    public void notationDecl(String name,XMLResourceIdentifier identifier,
                             Augmentations augmentations) throws XNIException;

    public void startConditional(short type,Augmentations augmentations)
            throws XNIException;

    public void ignoredCharacters(XMLString text,Augmentations augmentations)
            throws XNIException;

    public void endConditional(Augmentations augmentations) throws XNIException;

    public void endDTD(Augmentations augmentations) throws XNIException;

    // return the source from which this handler derives its events
    public XMLDTDSource getDTDSource();

    // set the source of this handler
    public void setDTDSource(XMLDTDSource source);
} // interface XMLDTDHandler
