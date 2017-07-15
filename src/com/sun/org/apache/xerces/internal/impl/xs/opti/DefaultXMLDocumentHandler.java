/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs.opti;

import com.sun.org.apache.xerces.internal.xni.*;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;

public class DefaultXMLDocumentHandler implements XMLDocumentHandler,
        XMLDTDHandler,
        XMLDTDContentModelHandler{
    private XMLDocumentSource fDocumentSource;
    //
    // XMLDocumentHandler methods
    //
    private XMLDTDSource fDTDSource;
    private XMLDTDContentModelSource fCMSource;

    public DefaultXMLDocumentHandler(){
    }

    public void startDocument(XMLLocator locator,String encoding,
                              NamespaceContext context,Augmentations augs)
            throws XNIException{
    }

    public void xmlDecl(String version,String encoding,String standalone,Augmentations augs)
            throws XNIException{
    }

    public void doctypeDecl(String rootElement,String publicId,String systemId,Augmentations augs)
            throws XNIException{
    }

    public void comment(XMLString text,Augmentations augs) throws XNIException{
    }

    public void processingInstruction(String target,XMLString data,Augmentations augs)
            throws XNIException{
    }

    public void startElement(QName element,XMLAttributes attributes,Augmentations augs)
            throws XNIException{
    }

    public void emptyElement(QName element,XMLAttributes attributes,Augmentations augs)
            throws XNIException{
    }

    public void startGeneralEntity(String name,
                                   XMLResourceIdentifier identifier,
                                   String encoding,
                                   Augmentations augs) throws XNIException{
    }

    public void textDecl(String version,String encoding,Augmentations augs) throws XNIException{
    }

    public void endGeneralEntity(String name,Augmentations augs) throws XNIException{
    }

    public void characters(XMLString text,Augmentations augs) throws XNIException{
    }

    public void ignorableWhitespace(XMLString text,Augmentations augs) throws XNIException{
    }

    public void endElement(QName element,Augmentations augs) throws XNIException{
    }

    public void startCDATA(Augmentations augs) throws XNIException{
    }

    public void endCDATA(Augmentations augs) throws XNIException{
    }
    //
    // XMLDTDHandler methods
    //

    public void endDocument(Augmentations augs) throws XNIException{
    }

    public void startPrefixMapping(String prefix,String uri,Augmentations augs)
            throws XNIException{
    }

    public void endPrefixMapping(String prefix,Augmentations augs) throws XNIException{
    }

    public void startDTD(XMLLocator locator,Augmentations augmentations)
            throws XNIException{
    }

    public void startParameterEntity(String name,
                                     XMLResourceIdentifier identifier,
                                     String encoding,
                                     Augmentations augmentations) throws XNIException{
    }
    /**
     * A comment.
     *
     * @param text The text in the comment.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws XNIException Thrown by application to signal an error.
     */
/**
 public void comment(XMLString text, Augmentations augmentations)
 throws XNIException {
 }
 */

    /**
     * Notifies of the presence of a TextDecl line in an entity. If present,
     * this method will be called immediately following the startEntity call.
     * <p>
     * <strong>Note:</strong> This method is only called for external
     * parameter entities referenced in the DTD.
     *
     * @param version  The XML version, or null if not specified.
     * @param encoding The IANA encoding name of the entity.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endParameterEntity(String name,Augmentations augmentations)
            throws XNIException{
    }

    public void startExternalSubset(XMLResourceIdentifier identifier,
                                    Augmentations augmentations)
            throws XNIException{
    }

    public void endExternalSubset(Augmentations augmentations)
            throws XNIException{
    }

    /**
     * A processing instruction. Processing instructions consist of a
     * target name and, optionally, text data. The data is only meaningful
     * to the application.
     * <p>
     * Typically, a processing instruction's data will contain a series
     * of pseudo-attributes. These pseudo-attributes follow the form of
     * element attributes but are <strong>not</strong> parsed or presented
     * to the application as anything other than text. The application is
     * responsible for parsing the data.
     *
     * @param target The target.
     * @param data   The data or null if none specified.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void elementDecl(String name,String contentModel,
                            Augmentations augmentations)
            throws XNIException{
    }

    public void startAttlist(String elementName,
                             Augmentations augmentations) throws XNIException{
    }

    public void attributeDecl(String elementName,String attributeName,
                              String type,String[] enumeration,
                              String defaultType,XMLString defaultValue,
                              XMLString nonNormalizedDefaultValue,Augmentations augmentations)
            throws XNIException{
    }

    public void endAttlist(Augmentations augmentations) throws XNIException{
    }

    public void internalEntityDecl(String name,XMLString text,
                                   XMLString nonNormalizedText,
                                   Augmentations augmentations)
            throws XNIException{
    }

    public void externalEntityDecl(String name,
                                   XMLResourceIdentifier identifier,
                                   Augmentations augmentations)
            throws XNIException{
    }

    public void unparsedEntityDecl(String name,
                                   XMLResourceIdentifier identifier,
                                   String notation,Augmentations augmentations)
            throws XNIException{
    }

    public void notationDecl(String name,XMLResourceIdentifier identifier,
                             Augmentations augmentations) throws XNIException{
    }

    public void startConditional(short type,Augmentations augmentations)
            throws XNIException{
    }
    //
    // XMLDTDContentModelHandler methods
    //

    public void ignoredCharacters(XMLString text,Augmentations augmentations)
            throws XNIException{
    }

    public void endConditional(Augmentations augmentations) throws XNIException{
    }

    public void endDTD(Augmentations augmentations) throws XNIException{
    }

    public void startContentModel(String elementName,Augmentations augmentations)
            throws XNIException{
    }

    public void any(Augmentations augmentations) throws XNIException{
    }

    public void empty(Augmentations augmentations) throws XNIException{
    }

    public void startGroup(Augmentations augmentations) throws XNIException{
    }

    public void pcdata(Augmentations augmentations) throws XNIException{
    }

    public void element(String elementName,Augmentations augmentations)
            throws XNIException{
    }

    public void separator(short separator,Augmentations augmentations)
            throws XNIException{
    }

    public void occurrence(short occurrence,Augmentations augmentations)
            throws XNIException{
    }

    public void endGroup(Augmentations augmentations) throws XNIException{
    }    public void setDocumentSource(XMLDocumentSource source){
        fDocumentSource=source;
    }

    public void endContentModel(Augmentations augmentations) throws XNIException{
    }    public XMLDocumentSource getDocumentSource(){
        return fDocumentSource;
    }



    // set the source of this handler
    public void setDTDSource(XMLDTDSource source){
        fDTDSource=source;
    }

    // return the source from which this handler derives its events
    public XMLDTDSource getDTDSource(){
        return fDTDSource;
    }



    // set content model source
    public void setDTDContentModelSource(XMLDTDContentModelSource source){
        fCMSource=source;
    }

    // get content model source
    public XMLDTDContentModelSource getDTDContentModelSource(){
        return fCMSource;
    }
}
