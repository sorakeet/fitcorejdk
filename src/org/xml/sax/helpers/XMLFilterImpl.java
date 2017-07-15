/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// XMLFilterImpl.java - base SAX2 filter implementation.
// http://www.saxproject.org
// Written by David Megginson
// NO WARRANTY!  This class is in the Public Domain.
// $Id: XMLFilterImpl.java,v 1.3 2004/11/03 22:53:09 jsuttor Exp $
package org.xml.sax.helpers;

import org.xml.sax.*;

import java.io.IOException;

public class XMLFilterImpl
        implements XMLFilter, EntityResolver, DTDHandler, ContentHandler, ErrorHandler{
    ////////////////////////////////////////////////////////////////////
    // Constructors.
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////
    private XMLReader parent=null;
    private Locator locator=null;
    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.XMLFilter.
    ////////////////////////////////////////////////////////////////////
    private EntityResolver entityResolver=null;
    private DTDHandler dtdHandler=null;
    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.XMLReader.
    ////////////////////////////////////////////////////////////////////
    private ContentHandler contentHandler=null;    public void setParent(XMLReader parent){
        this.parent=parent;
    }
    private ErrorHandler errorHandler=null;
    public XMLFilterImpl(){
        super();
    }

    public XMLFilterImpl(XMLReader parent){
        super();
        setParent(parent);
    }    public XMLReader getParent(){
        return parent;
    }

    public boolean getFeature(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(parent!=null){
            return parent.getFeature(name);
        }else{
            throw new SAXNotRecognizedException("Feature: "+name);
        }
    }

    public void setFeature(String name,boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(parent!=null){
            parent.setFeature(name,value);
        }else{
            throw new SAXNotRecognizedException("Feature: "+name);
        }
    }

    public Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(parent!=null){
            return parent.getProperty(name);
        }else{
            throw new SAXNotRecognizedException("Property: "+name);
        }
    }

    public void setProperty(String name,Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(parent!=null){
            parent.setProperty(name,value);
        }else{
            throw new SAXNotRecognizedException("Property: "+name);
        }
    }

    public InputSource resolveEntity(String publicId,String systemId)
            throws SAXException, IOException{
        if(entityResolver!=null){
            return entityResolver.resolveEntity(publicId,systemId);
        }else{
            return null;
        }
    }

    public void notationDecl(String name,String publicId,String systemId)
            throws SAXException{
        if(dtdHandler!=null){
            dtdHandler.notationDecl(name,publicId,systemId);
        }
    }

    public void unparsedEntityDecl(String name,String publicId,
                                   String systemId,String notationName)
            throws SAXException{
        if(dtdHandler!=null){
            dtdHandler.unparsedEntityDecl(name,publicId,systemId,
                    notationName);
        }
    }    public void setEntityResolver(EntityResolver resolver){
        entityResolver=resolver;
    }

    public void setDocumentLocator(Locator locator){
        this.locator=locator;
        if(contentHandler!=null){
            contentHandler.setDocumentLocator(locator);
        }
    }

    public void startDocument()
            throws SAXException{
        if(contentHandler!=null){
            contentHandler.startDocument();
        }
    }

    public void endDocument()
            throws SAXException{
        if(contentHandler!=null){
            contentHandler.endDocument();
        }
    }    public EntityResolver getEntityResolver(){
        return entityResolver;
    }

    public void startPrefixMapping(String prefix,String uri)
            throws SAXException{
        if(contentHandler!=null){
            contentHandler.startPrefixMapping(prefix,uri);
        }
    }

    public void endPrefixMapping(String prefix)
            throws SAXException{
        if(contentHandler!=null){
            contentHandler.endPrefixMapping(prefix);
        }
    }

    public void startElement(String uri,String localName,String qName,
                             Attributes atts)
            throws SAXException{
        if(contentHandler!=null){
            contentHandler.startElement(uri,localName,qName,atts);
        }
    }    public void setDTDHandler(DTDHandler handler){
        dtdHandler=handler;
    }

    public void endElement(String uri,String localName,String qName)
            throws SAXException{
        if(contentHandler!=null){
            contentHandler.endElement(uri,localName,qName);
        }
    }

    public void characters(char ch[],int start,int length)
            throws SAXException{
        if(contentHandler!=null){
            contentHandler.characters(ch,start,length);
        }
    }

    public void ignorableWhitespace(char ch[],int start,int length)
            throws SAXException{
        if(contentHandler!=null){
            contentHandler.ignorableWhitespace(ch,start,length);
        }
    }    public DTDHandler getDTDHandler(){
        return dtdHandler;
    }

    public void processingInstruction(String target,String data)
            throws SAXException{
        if(contentHandler!=null){
            contentHandler.processingInstruction(target,data);
        }
    }

    public void skippedEntity(String name)
            throws SAXException{
        if(contentHandler!=null){
            contentHandler.skippedEntity(name);
        }
    }

    public void warning(SAXParseException e)
            throws SAXException{
        if(errorHandler!=null){
            errorHandler.warning(e);
        }
    }    public void setContentHandler(ContentHandler handler){
        contentHandler=handler;
    }

    public void error(SAXParseException e)
            throws SAXException{
        if(errorHandler!=null){
            errorHandler.error(e);
        }
    }

    public void fatalError(SAXParseException e)
            throws SAXException{
        if(errorHandler!=null){
            errorHandler.fatalError(e);
        }
    }

    public ContentHandler getContentHandler(){
        return contentHandler;
    }





    public void setErrorHandler(ErrorHandler handler){
        errorHandler=handler;
    }





    public ErrorHandler getErrorHandler(){
        return errorHandler;
    }





    public void parse(InputSource input)
            throws SAXException, IOException{
        setupParse();
        parent.parse(input);
    }



    public void parse(String systemId)
            throws SAXException, IOException{
        parse(new InputSource(systemId));
    }
    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.EntityResolver.
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.DTDHandler.
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.ContentHandler.
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.ErrorHandler.
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    // Internal methods.
    ////////////////////////////////////////////////////////////////////

    private void setupParse(){
        if(parent==null){
            throw new NullPointerException("No parent for filter");
        }
        parent.setEntityResolver(this);
        parent.setDTDHandler(this);
        parent.setContentHandler(this);
        parent.setErrorHandler(this);
    }
}
// end of XMLFilterImpl.java
