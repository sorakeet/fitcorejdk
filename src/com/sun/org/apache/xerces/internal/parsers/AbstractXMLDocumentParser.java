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
package com.sun.org.apache.xerces.internal.parsers;

import com.sun.org.apache.xerces.internal.xni.*;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;

public abstract class AbstractXMLDocumentParser
        extends XMLParser
        implements XMLDocumentHandler, XMLDTDHandler, XMLDTDContentModelHandler{
    //
    // Data
    //
    // state
    protected boolean fInDTD;
    protected XMLDocumentSource fDocumentSource;
    protected XMLDTDSource fDTDSource;
    protected XMLDTDContentModelSource fDTDContentModelSource;
    //
    // Constructors
    //

    protected AbstractXMLDocumentParser(XMLParserConfiguration config){
        super(config);
        // set handlers
        config.setDocumentHandler(this);
        config.setDTDHandler(this);
        config.setDTDContentModelHandler(this);
    } // <init>(XMLParserConfiguration)
    //
    // XMLDocumentHandler methods
    //

    public void startDocument(XMLLocator locator,String encoding,
                              NamespaceContext namespaceContext,Augmentations augs)
            throws XNIException{
    } // startDocument(XMLLocator,String)

    public void xmlDecl(String version,String encoding,String standalone,Augmentations augs)
            throws XNIException{
    } // xmlDecl(String,String,String)

    public void doctypeDecl(String rootElement,String publicId,String systemId,Augmentations augs)
            throws XNIException{
    } // doctypeDecl(String,String,String)

    public void comment(XMLString text,Augmentations augs) throws XNIException{
    } // comment (XMLString, Augmentations)

    public void processingInstruction(String target,XMLString data,Augmentations augs)
            throws XNIException{
    } // processingInstruction(String, XMLString, Augmentations)

    public void startElement(QName element,XMLAttributes attributes,Augmentations augs)
            throws XNIException{
    } // startElement(QName,XMLAttributes)

    public void emptyElement(QName element,XMLAttributes attributes,Augmentations augs)
            throws XNIException{
        startElement(element,attributes,augs);
        endElement(element,augs);
    } // emptyElement(QName,XMLAttributes)

    public void startGeneralEntity(String name,
                                   XMLResourceIdentifier identifier,
                                   String encoding,
                                   Augmentations augs) throws XNIException{
    } // startGeneralEntity(String,XMLResourceIdentifier,String,Augmentations)

    public void textDecl(String version,String encoding,Augmentations augs) throws XNIException{
    } // textDecl(String, String, Augmentations)

    public void endGeneralEntity(String name,Augmentations augs)
            throws XNIException{
    } // endGeneralEntity(String,Augmentations)

    public void characters(XMLString text,Augmentations augs) throws XNIException{
    } // characters(XMLString)

    public void ignorableWhitespace(XMLString text,Augmentations augs) throws XNIException{
    } // ignorableWhitespace(XMLString)

    public void endElement(QName element,Augmentations augs) throws XNIException{
    } // endElement(QName)

    public void startCDATA(Augmentations augs) throws XNIException{
    } // startCDATA()

    public void endCDATA(Augmentations augs) throws XNIException{
    } // endCDATA()

    public void endDocument(Augmentations augs) throws XNIException{
    } // endDocument()

    public void startDTD(XMLLocator locator,Augmentations augs) throws XNIException{
        fInDTD=true;
    } // startDTD(XMLLocator)    public void setDocumentSource(XMLDocumentSource source){
        fDocumentSource=source;
    } // setDocumentSource

    public void startParameterEntity(String name,
                                     XMLResourceIdentifier identifier,
                                     String encoding,
                                     Augmentations augs) throws XNIException{
    } // startParameterEntity(String,XMLResourceIdentifier,String,Augmentations)    public XMLDocumentSource getDocumentSource(){
        return fDocumentSource;
    } // getDocumentSource
    //
    // XMLDTDHandler methods
    //

    public void endParameterEntity(String name,Augmentations augs)
            throws XNIException{
    } // endParameterEntity(String,Augmentations)

    public void startExternalSubset(XMLResourceIdentifier identifier,Augmentations augmentations)
            throws XNIException{
    } // startExternalSubset(Augmentations)

    public void endExternalSubset(Augmentations augmentations)
            throws XNIException{
    } // endExternalSubset(Augmentations)

    public void elementDecl(String name,String contentModel,Augmentations augs)
            throws XNIException{
    } // elementDecl(String,String)

    public void startAttlist(String elementName,Augmentations augs) throws XNIException{
    } // startAttlist(String)

    public void attributeDecl(String elementName,String attributeName,
                              String type,String[] enumeration,
                              String defaultType,XMLString defaultValue,
                              XMLString nonNormalizedDefaultValue,Augmentations augs)
            throws XNIException{
    } // attributeDecl(String,String,String,String[],String,XMLString, XMLString, Augmentations)

    public void endAttlist(Augmentations augs) throws XNIException{
    } // endAttlist()

    public void internalEntityDecl(String name,XMLString text,
                                   XMLString nonNormalizedText,Augmentations augs)
            throws XNIException{
    } // internalEntityDecl(String,XMLString,XMLString)

    public void externalEntityDecl(String name,XMLResourceIdentifier identifier,
                                   Augmentations augs) throws XNIException{
    } // externalEntityDecl(String,XMLResourceIdentifier, Augmentations)

    public void unparsedEntityDecl(String name,XMLResourceIdentifier identifier,
                                   String notation,Augmentations augs) throws XNIException{
    } // unparsedEntityDecl(String,XMLResourceIdentifier, String, Augmentations)

    public void notationDecl(String name,XMLResourceIdentifier identifier,
                             Augmentations augs)
            throws XNIException{
    } // notationDecl(String,XMLResourceIdentifier, Augmentations)

    public void startConditional(short type,Augmentations augs) throws XNIException{
    } // startConditional(short)

    public void ignoredCharacters(XMLString text,Augmentations augs) throws XNIException{
    } // ignoredCharacters(XMLString, Augmentations)

    public void endConditional(Augmentations augs) throws XNIException{
    } // endConditional()

    public void endDTD(Augmentations augs) throws XNIException{
        fInDTD=false;
    } // endDTD()

    public void startContentModel(String elementName,Augmentations augs) throws XNIException{
    } // startContentModel(String, Augmentations)

    public void any(Augmentations augs) throws XNIException{
    } // any(Augmentations)

    public void empty(Augmentations augs) throws XNIException{
    } // empty(Augmentations)    // set the source of this handler
    public void setDTDSource(XMLDTDSource source){
        fDTDSource=source;
    }

    public void startGroup(Augmentations augs) throws XNIException{
    } // stargGroup(Augmentations)    // return the source from which this handler derives its events
    public XMLDTDSource getDTDSource(){
        return fDTDSource;
    }
    //
    // XMLDTDContentModelHandler methods
    //

    public void pcdata(Augmentations augs) throws XNIException{
    } // pcdata(Augmentations)

    public void element(String elementName,Augmentations augs) throws XNIException{
    } // element(String, Augmentations)

    public void separator(short separator,Augmentations augs) throws XNIException{
    } // separator(short, Augmentations)

    public void occurrence(short occurrence,Augmentations augs) throws XNIException{
    } // occurence(short, Augmentations)

    public void endGroup(Augmentations augs) throws XNIException{
    } // endGroup(Augmentations)

    public void endContentModel(Augmentations augs) throws XNIException{
    } // endContentModel(Augmentations)

    protected void reset() throws XNIException{
        super.reset();
        fInDTD=false;
    } // reset()







    // set content model source
    public void setDTDContentModelSource(XMLDTDContentModelSource source){
        fDTDContentModelSource=source;
    }

    // get content model source
    public XMLDTDContentModelSource getDTDContentModelSource(){
        return fDTDContentModelSource;
    }
    //
    // Protected methods
    //


} // class AbstractXMLDocumentParser
