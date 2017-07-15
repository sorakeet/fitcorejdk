/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * The Apache Software License, Version 1.1
 * <p>
 * <p>
 * Copyright (c) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * <p>
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 * <p>
 * 4. The names "Xerces" and "Apache Software Foundation" must
 * not be used to endorse or promote products derived from this
 * software without prior written permission. For written
 * permission, please contact apache@apache.org.
 * <p>
 * 5. Products derived from this software may not be called "Apache",
 * nor may "Apache" appear in their name, without prior written
 * permission of the Apache Software Foundation.
 * <p>
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 * <p>
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
/**
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLSymbols;
import com.sun.org.apache.xerces.internal.xni.*;
import com.sun.org.apache.xerces.internal.xni.parser.*;

public class XMLNamespaceBinder
        implements XMLComponent, XMLDocumentFilter{
    //
    // Constants
    //
    // feature identifiers
    protected static final String NAMESPACES=
            Constants.SAX_FEATURE_PREFIX+Constants.NAMESPACES_FEATURE;
    // property identifiers
    protected static final String SYMBOL_TABLE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String ERROR_REPORTER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY;
    // recognized features and properties
    private static final String[] RECOGNIZED_FEATURES={
            NAMESPACES,
    };
    private static final Boolean[] FEATURE_DEFAULTS={
            null,
    };
    private static final String[] RECOGNIZED_PROPERTIES={
            SYMBOL_TABLE,
            ERROR_REPORTER,
    };
    private static final Object[] PROPERTY_DEFAULTS={
            null,
            null,
    };
    //
    // Data
    //
    // features
    protected boolean fNamespaces;
    // properties
    protected SymbolTable fSymbolTable;
    protected XMLErrorReporter fErrorReporter;
    // handlers
    protected XMLDocumentHandler fDocumentHandler;
    protected XMLDocumentSource fDocumentSource;
    // settings
    protected boolean fOnlyPassPrefixMappingEvents;
    // shared context
    private NamespaceContext fNamespaceContext;
    // temp vars
    private QName fAttributeQName=new QName();
    //
    // Constructors
    //

    public XMLNamespaceBinder(){
    } // <init>()
    //
    // Public methods
    //
    // settings

    public boolean getOnlyPassPrefixMappingEvents(){
        return fOnlyPassPrefixMappingEvents;
    } // getOnlyPassPrefixMappingEvents():boolean

    public void setOnlyPassPrefixMappingEvents(boolean onlyPassPrefixMappingEvents){
        fOnlyPassPrefixMappingEvents=onlyPassPrefixMappingEvents;
    } // setOnlyPassPrefixMappingEvents(boolean)
    //
    // XMLComponent methods
    //

    public void reset(XMLComponentManager componentManager)
            throws XNIException{
        // features
        fNamespaces=componentManager.getFeature(NAMESPACES,true);
        // Xerces properties
        fSymbolTable=(SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        fErrorReporter=(XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
    } // reset(XMLComponentManager)

    public String[] getRecognizedFeatures(){
        return (String[])(RECOGNIZED_FEATURES.clone());
    } // getRecognizedFeatures():String[]

    public void setFeature(String featureId,boolean state)
            throws XMLConfigurationException{
    } // setFeature(String,boolean)

    public String[] getRecognizedProperties(){
        return (String[])(RECOGNIZED_PROPERTIES.clone());
    } // getRecognizedProperties():String[]

    public void setProperty(String propertyId,Object value)
            throws XMLConfigurationException{
        // Xerces properties
        if(propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)){
            final int suffixLength=propertyId.length()-Constants.XERCES_PROPERTY_PREFIX.length();
            if(suffixLength==Constants.SYMBOL_TABLE_PROPERTY.length()&&
                    propertyId.endsWith(Constants.SYMBOL_TABLE_PROPERTY)){
                fSymbolTable=(SymbolTable)value;
            }else if(suffixLength==Constants.ERROR_REPORTER_PROPERTY.length()&&
                    propertyId.endsWith(Constants.ERROR_REPORTER_PROPERTY)){
                fErrorReporter=(XMLErrorReporter)value;
            }
            return;
        }
    } // setProperty(String,Object)

    public Boolean getFeatureDefault(String featureId){
        for(int i=0;i<RECOGNIZED_FEATURES.length;i++){
            if(RECOGNIZED_FEATURES[i].equals(featureId)){
                return FEATURE_DEFAULTS[i];
            }
        }
        return null;
    } // getFeatureDefault(String):Boolean

    public Object getPropertyDefault(String propertyId){
        for(int i=0;i<RECOGNIZED_PROPERTIES.length;i++){
            if(RECOGNIZED_PROPERTIES[i].equals(propertyId)){
                return PROPERTY_DEFAULTS[i];
            }
        }
        return null;
    } // getPropertyDefault(String):Object
    //
    // XMLDocumentSource methods
    //

    public void startDocument(XMLLocator locator,String encoding,
                              NamespaceContext namespaceContext,Augmentations augs)
            throws XNIException{
        fNamespaceContext=namespaceContext;
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            fDocumentHandler.startDocument(locator,encoding,namespaceContext,augs);
        }
    } // startDocument(XMLLocator,String)    public void setDocumentHandler(XMLDocumentHandler documentHandler){
        fDocumentHandler=documentHandler;
    } // setDocumentHandler(XMLDocumentHandler)

    public void xmlDecl(String version,String encoding,String standalone,Augmentations augs)
            throws XNIException{
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            fDocumentHandler.xmlDecl(version,encoding,standalone,augs);
        }
    } // xmlDecl(String,String,String)    public XMLDocumentHandler getDocumentHandler(){
        return fDocumentHandler;
    } // setDocumentHandler(XMLDocumentHandler)
    //
    // XMLDocumentHandler methods
    //

    public void doctypeDecl(String rootElement,
                            String publicId,String systemId,Augmentations augs)
            throws XNIException{
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            fDocumentHandler.doctypeDecl(rootElement,publicId,systemId,augs);
        }
    } // doctypeDecl(String,String,String)    public void setDocumentSource(XMLDocumentSource source){
        fDocumentSource=source;
    } // setDocumentSource

    public void comment(XMLString text,Augmentations augs) throws XNIException{
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            fDocumentHandler.comment(text,augs);
        }
    } // comment(XMLString)    public XMLDocumentSource getDocumentSource(){
        return fDocumentSource;
    } // getDocumentSource

    public void processingInstruction(String target,XMLString data,Augmentations augs)
            throws XNIException{
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            fDocumentHandler.processingInstruction(target,data,augs);
        }
    } // processingInstruction(String,XMLString)

    public void startElement(QName element,XMLAttributes attributes,Augmentations augs)
            throws XNIException{
        if(fNamespaces){
            handleStartElement(element,attributes,augs,false);
        }else if(fDocumentHandler!=null){
            fDocumentHandler.startElement(element,attributes,augs);
        }
    } // startElement(QName,XMLAttributes)

    public void emptyElement(QName element,XMLAttributes attributes,Augmentations augs)
            throws XNIException{
        if(fNamespaces){
            handleStartElement(element,attributes,augs,true);
            handleEndElement(element,augs,true);
        }else if(fDocumentHandler!=null){
            fDocumentHandler.emptyElement(element,attributes,augs);
        }
    } // emptyElement(QName,XMLAttributes)

    public void startGeneralEntity(String name,
                                   XMLResourceIdentifier identifier,
                                   String encoding,Augmentations augs)
            throws XNIException{
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            fDocumentHandler.startGeneralEntity(name,identifier,encoding,augs);
        }
    } // startEntity(String,String,String,String,String)

    public void textDecl(String version,String encoding,Augmentations augs)
            throws XNIException{
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            fDocumentHandler.textDecl(version,encoding,augs);
        }
    } // textDecl(String,String)

    public void endGeneralEntity(String name,Augmentations augs) throws XNIException{
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            fDocumentHandler.endGeneralEntity(name,augs);
        }
    } // endEntity(String)

    public void characters(XMLString text,Augmentations augs) throws XNIException{
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            fDocumentHandler.characters(text,augs);
        }
    } // characters(XMLString)

    public void ignorableWhitespace(XMLString text,Augmentations augs) throws XNIException{
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            fDocumentHandler.ignorableWhitespace(text,augs);
        }
    } // ignorableWhitespace(XMLString)

    public void endElement(QName element,Augmentations augs) throws XNIException{
        if(fNamespaces){
            handleEndElement(element,augs,false);
        }else if(fDocumentHandler!=null){
            fDocumentHandler.endElement(element,augs);
        }
    } // endElement(QName)

    public void startCDATA(Augmentations augs) throws XNIException{
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            fDocumentHandler.startCDATA(augs);
        }
    } // startCDATA()

    public void endCDATA(Augmentations augs) throws XNIException{
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            fDocumentHandler.endCDATA(augs);
        }
    } // endCDATA()

    public void endDocument(Augmentations augs) throws XNIException{
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            fDocumentHandler.endDocument(augs);
        }
    } // endDocument()

    protected void handleEndElement(QName element,Augmentations augs,boolean isEmpty)
            throws XNIException{
        // bind element
        String eprefix=element.prefix!=null?element.prefix:XMLSymbols.EMPTY_STRING;
        element.uri=fNamespaceContext.getURI(eprefix);
        if(element.uri!=null){
            element.prefix=eprefix;
        }
        // call handlers
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            if(!isEmpty){
                fDocumentHandler.endElement(element,augs);
            }
        }
        // pop context
        fNamespaceContext.popContext();
    } // handleEndElement(QName,boolean)

    protected void handleStartElement(QName element,XMLAttributes attributes,
                                      Augmentations augs,
                                      boolean isEmpty) throws XNIException{
        // add new namespace context
        fNamespaceContext.pushContext();
        if(element.prefix==XMLSymbols.PREFIX_XMLNS){
            fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                    "ElementXMLNSPrefix",
                    new Object[]{element.rawname},
                    XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }
        // search for new namespace bindings
        int length=attributes.getLength();
        for(int i=0;i<length;i++){
            String localpart=attributes.getLocalName(i);
            String prefix=attributes.getPrefix(i);
            // when it's of form xmlns="..." or xmlns:prefix="...",
            // it's a namespace declaration. but prefix:xmlns="..." isn't.
            if(prefix==XMLSymbols.PREFIX_XMLNS||
                    prefix==XMLSymbols.EMPTY_STRING&&localpart==XMLSymbols.PREFIX_XMLNS){
                // get the internalized value of this attribute
                String uri=fSymbolTable.addSymbol(attributes.getValue(i));
                // 1. "xmlns" can't be bound to any namespace
                if(prefix==XMLSymbols.PREFIX_XMLNS&&localpart==XMLSymbols.PREFIX_XMLNS){
                    fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                            "CantBindXMLNS",
                            new Object[]{attributes.getQName(i)},
                            XMLErrorReporter.SEVERITY_FATAL_ERROR);
                }
                // 2. the namespace for "xmlns" can't be bound to any prefix
                if(uri==NamespaceContext.XMLNS_URI){
                    fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                            "CantBindXMLNS",
                            new Object[]{attributes.getQName(i)},
                            XMLErrorReporter.SEVERITY_FATAL_ERROR);
                }
                // 3. "xml" can't be bound to any other namespace than it's own
                if(localpart==XMLSymbols.PREFIX_XML){
                    if(uri!=NamespaceContext.XML_URI){
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                "CantBindXML",
                                new Object[]{attributes.getQName(i)},
                                XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                }
                // 4. the namespace for "xml" can't be bound to any other prefix
                else{
                    if(uri==NamespaceContext.XML_URI){
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                "CantBindXML",
                                new Object[]{attributes.getQName(i)},
                                XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                }
                prefix=localpart!=XMLSymbols.PREFIX_XMLNS?localpart:XMLSymbols.EMPTY_STRING;
                // http://www.w3.org/TR/1999/REC-xml-names-19990114/#dt-prefix
                // We should only report an error if there is a prefix,
                // that is, the local part is not "xmlns". -SG
                // Since this is an error condition in XML 1.0,
                // and should be relatively uncommon in XML 1.1,
                // making this test into a method call to reuse code
                // should be acceptable.  - NG
                if(prefixBoundToNullURI(uri,localpart)){
                    fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                            "EmptyPrefixedAttName",
                            new Object[]{attributes.getQName(i)},
                            XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    continue;
                }
                // declare prefix in context
                fNamespaceContext.declarePrefix(prefix,uri.length()!=0?uri:null);
            }
        }
        // bind the element
        String prefix=element.prefix!=null
                ?element.prefix:XMLSymbols.EMPTY_STRING;
        element.uri=fNamespaceContext.getURI(prefix);
        if(element.prefix==null&&element.uri!=null){
            element.prefix=XMLSymbols.EMPTY_STRING;
        }
        if(element.prefix!=null&&element.uri==null){
            fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                    "ElementPrefixUnbound",
                    new Object[]{element.prefix,element.rawname},
                    XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }
        // bind the attributes
        for(int i=0;i<length;i++){
            attributes.getName(i,fAttributeQName);
            String aprefix=fAttributeQName.prefix!=null
                    ?fAttributeQName.prefix:XMLSymbols.EMPTY_STRING;
            String arawname=fAttributeQName.rawname;
            if(arawname==XMLSymbols.PREFIX_XMLNS){
                fAttributeQName.uri=fNamespaceContext.getURI(XMLSymbols.PREFIX_XMLNS);
                attributes.setName(i,fAttributeQName);
            }else if(aprefix!=XMLSymbols.EMPTY_STRING){
                fAttributeQName.uri=fNamespaceContext.getURI(aprefix);
                if(fAttributeQName.uri==null){
                    fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                            "AttributePrefixUnbound",
                            new Object[]{element.rawname,arawname,aprefix},
                            XMLErrorReporter.SEVERITY_FATAL_ERROR);
                }
                attributes.setName(i,fAttributeQName);
            }
        }
        // verify that duplicate attributes don't exist
        // Example: <foo xmlns:a='NS' xmlns:b='NS' a:attr='v1' b:attr='v2'/>
        int attrCount=attributes.getLength();
        for(int i=0;i<attrCount-1;i++){
            String auri=attributes.getURI(i);
            if(auri==null||auri==NamespaceContext.XMLNS_URI){
                continue;
            }
            String alocalpart=attributes.getLocalName(i);
            for(int j=i+1;j<attrCount;j++){
                String blocalpart=attributes.getLocalName(j);
                String buri=attributes.getURI(j);
                if(alocalpart==blocalpart&&auri==buri){
                    fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                            "AttributeNSNotUnique",
                            new Object[]{element.rawname,alocalpart,auri},
                            XMLErrorReporter.SEVERITY_FATAL_ERROR);
                }
            }
        }
        // call handler
        if(fDocumentHandler!=null&&!fOnlyPassPrefixMappingEvents){
            if(isEmpty){
                fDocumentHandler.emptyElement(element,attributes,augs);
            }else{
                fDocumentHandler.startElement(element,attributes,augs);
            }
        }
    } // handleStartElement(QName,XMLAttributes,boolean)

    // returns true iff the given prefix is bound to "" *and*
    // this is disallowed by the version of XML namespaces in use.
    protected boolean prefixBoundToNullURI(String uri,String localpart){
        return (uri==XMLSymbols.EMPTY_STRING&&localpart!=XMLSymbols.PREFIX_XMLNS);
    } // prefixBoundToNullURI(String, String):  boolean


    //
    // Protected methods
    //






} // class XMLNamespaceBinder
