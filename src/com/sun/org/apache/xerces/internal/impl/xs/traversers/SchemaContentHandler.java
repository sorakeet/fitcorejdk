/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2005 The Apache Software Foundation.
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
 * Copyright 2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs.traversers;

import com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaDOMParser;
import com.sun.org.apache.xerces.internal.util.*;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import org.w3c.dom.Document;
import org.xml.sax.*;
import org.xml.sax.helpers.LocatorImpl;

final class SchemaContentHandler implements ContentHandler{
    private final SAXLocatorWrapper fSAXLocatorWrapper=new SAXLocatorWrapper();
    private final QName fElementQName=new QName();
    private final QName fAttributeQName=new QName();
    private final XMLAttributesImpl fAttributes=new XMLAttributesImpl();
    private final XMLString fTempString=new XMLString();
    private SymbolTable fSymbolTable;
    private SchemaDOMParser fSchemaDOMParser;
    private NamespaceSupport fNamespaceContext=new NamespaceSupport();
    private boolean fNeedPushNSContext;
    private boolean fNamespacePrefixes=false;
    private boolean fStringsInternalized=false;

    public SchemaContentHandler(){
    }

    public Document getDocument(){
        return fSchemaDOMParser.getDocument();
    }

    public void setDocumentLocator(Locator locator){
        fSAXLocatorWrapper.setLocator(locator);
    }

    public void startDocument() throws SAXException{
        fNeedPushNSContext=true;
        try{
            fSchemaDOMParser.startDocument(fSAXLocatorWrapper,null,fNamespaceContext,null);
        }catch(XMLParseException e){
            convertToSAXParseException(e);
        }catch(XNIException e){
            convertToSAXException(e);
        }
    }

    public void endDocument() throws SAXException{
        fSAXLocatorWrapper.setLocator(null);
        try{
            fSchemaDOMParser.endDocument(null);
        }catch(XMLParseException e){
            convertToSAXParseException(e);
        }catch(XNIException e){
            convertToSAXException(e);
        }
    }

    public void startPrefixMapping(String prefix,String uri) throws SAXException{
        if(fNeedPushNSContext){
            fNeedPushNSContext=false;
            fNamespaceContext.pushContext();
        }
        if(!fStringsInternalized){
            prefix=(prefix!=null)?fSymbolTable.addSymbol(prefix):XMLSymbols.EMPTY_STRING;
            uri=(uri!=null&&uri.length()>0)?fSymbolTable.addSymbol(uri):null;
        }else{
            if(prefix==null){
                prefix=XMLSymbols.EMPTY_STRING;
            }
            if(uri!=null&&uri.length()==0){
                uri=null;
            }
        }
        fNamespaceContext.declarePrefix(prefix,uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException{
        // do nothing
    }

    public void startElement(String uri,String localName,String qName,Attributes atts) throws SAXException{
        if(fNeedPushNSContext){
            fNamespaceContext.pushContext();
        }
        fNeedPushNSContext=true;
        // Fill element QName and XMLAttributes
        fillQName(fElementQName,uri,localName,qName);
        fillXMLAttributes(atts);
        // Add namespace declarations if necessary
        if(!fNamespacePrefixes){
            final int prefixCount=fNamespaceContext.getDeclaredPrefixCount();
            if(prefixCount>0){
                addNamespaceDeclarations(prefixCount);
            }
        }
        try{
            fSchemaDOMParser.startElement(fElementQName,fAttributes,null);
        }catch(XMLParseException e){
            convertToSAXParseException(e);
        }catch(XNIException e){
            convertToSAXException(e);
        }
    }

    public void endElement(String uri,String localName,String qName) throws SAXException{
        fillQName(fElementQName,uri,localName,qName);
        try{
            fSchemaDOMParser.endElement(fElementQName,null);
        }catch(XMLParseException e){
            convertToSAXParseException(e);
        }catch(XNIException e){
            convertToSAXException(e);
        }finally{
            fNamespaceContext.popContext();
        }
    }

    public void characters(char[] ch,int start,int length) throws SAXException{
        try{
            fTempString.setValues(ch,start,length);
            fSchemaDOMParser.characters(fTempString,null);
        }catch(XMLParseException e){
            convertToSAXParseException(e);
        }catch(XNIException e){
            convertToSAXException(e);
        }
    }

    public void ignorableWhitespace(char[] ch,int start,int length) throws SAXException{
        try{
            fTempString.setValues(ch,start,length);
            fSchemaDOMParser.ignorableWhitespace(fTempString,null);
        }catch(XMLParseException e){
            convertToSAXParseException(e);
        }catch(XNIException e){
            convertToSAXException(e);
        }
    }

    public void processingInstruction(String target,String data) throws SAXException{
        try{
            fTempString.setValues(data.toCharArray(),0,data.length());
            fSchemaDOMParser.processingInstruction(target,fTempString,null);
        }catch(XMLParseException e){
            convertToSAXParseException(e);
        }catch(XNIException e){
            convertToSAXException(e);
        }
    }

    public void skippedEntity(String arg) throws SAXException{
        // do-nothing
    }

    private void fillQName(QName toFill,String uri,String localpart,String rawname){
        if(!fStringsInternalized){
            uri=(uri!=null&&uri.length()>0)?fSymbolTable.addSymbol(uri):null;
            localpart=(localpart!=null)?fSymbolTable.addSymbol(localpart):XMLSymbols.EMPTY_STRING;
            rawname=(rawname!=null)?fSymbolTable.addSymbol(rawname):XMLSymbols.EMPTY_STRING;
        }else{
            if(uri!=null&&uri.length()==0){
                uri=null;
            }
            if(localpart==null){
                localpart=XMLSymbols.EMPTY_STRING;
            }
            if(rawname==null){
                rawname=XMLSymbols.EMPTY_STRING;
            }
        }
        String prefix=XMLSymbols.EMPTY_STRING;
        int prefixIdx=rawname.indexOf(':');
        if(prefixIdx!=-1){
            prefix=fSymbolTable.addSymbol(rawname.substring(0,prefixIdx));
            // local part may be an empty string if this is a namespace declaration
            if(localpart==XMLSymbols.EMPTY_STRING){
                localpart=fSymbolTable.addSymbol(rawname.substring(prefixIdx+1));
            }
        }
        // local part may be an empty string if this is a namespace declaration
        else if(localpart==XMLSymbols.EMPTY_STRING){
            localpart=rawname;
        }
        toFill.setValues(prefix,localpart,rawname,uri);
    }

    private void fillXMLAttributes(Attributes atts){
        fAttributes.removeAllAttributes();
        final int attrCount=atts.getLength();
        for(int i=0;i<attrCount;++i){
            fillQName(fAttributeQName,atts.getURI(i),atts.getLocalName(i),atts.getQName(i));
            String type=atts.getType(i);
            fAttributes.addAttributeNS(fAttributeQName,(type!=null)?type:XMLSymbols.fCDATASymbol,atts.getValue(i));
            fAttributes.setSpecified(i,true);
        }
    }

    private void addNamespaceDeclarations(final int prefixCount){
        String prefix=null;
        String localpart=null;
        String rawname=null;
        String nsPrefix=null;
        String nsURI=null;
        for(int i=0;i<prefixCount;++i){
            nsPrefix=fNamespaceContext.getDeclaredPrefixAt(i);
            nsURI=fNamespaceContext.getURI(nsPrefix);
            if(nsPrefix.length()>0){
                prefix=XMLSymbols.PREFIX_XMLNS;
                localpart=nsPrefix;
                rawname=fSymbolTable.addSymbol(prefix+":"+localpart);
            }else{
                prefix=XMLSymbols.EMPTY_STRING;
                localpart=XMLSymbols.PREFIX_XMLNS;
                rawname=XMLSymbols.PREFIX_XMLNS;
            }
            fAttributeQName.setValues(prefix,localpart,rawname,NamespaceContext.XMLNS_URI);
            fAttributes.addAttribute(fAttributeQName,XMLSymbols.fCDATASymbol,nsURI);
        }
    }

    static void convertToSAXParseException(XMLParseException e) throws SAXException{
        Exception ex=e.getException();
        if(ex==null){
            // must be a parser exception; mine it for locator info and throw
            // a SAXParseException
            LocatorImpl locatorImpl=new LocatorImpl();
            locatorImpl.setPublicId(e.getPublicId());
            locatorImpl.setSystemId(e.getExpandedSystemId());
            locatorImpl.setLineNumber(e.getLineNumber());
            locatorImpl.setColumnNumber(e.getColumnNumber());
            throw new SAXParseException(e.getMessage(),locatorImpl);
        }
        if(ex instanceof SAXException){
            // why did we create an XMLParseException?
            throw (SAXException)ex;
        }
        throw new SAXException(ex);
    }

    static void convertToSAXException(XNIException e) throws SAXException{
        Exception ex=e.getException();
        if(ex==null){
            throw new SAXException(e.getMessage());
        }
        if(ex instanceof SAXException){
            throw (SAXException)ex;
        }
        throw new SAXException(ex);
    }

    public void reset(SchemaDOMParser schemaDOMParser,SymbolTable symbolTable,
                      boolean namespacePrefixes,boolean stringsInternalized){
        fSchemaDOMParser=schemaDOMParser;
        fSymbolTable=symbolTable;
        fNamespacePrefixes=namespacePrefixes;
        fStringsInternalized=stringsInternalized;
    }
} // SchemaContentHandler
