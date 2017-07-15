/**
 * Copyright (c) 2005, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.xalan.internal.xsltc.trax;

import com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;

public class StAXStream2SAX implements XMLReader, Locator{
    //private final static String EMPTYSTRING = "";
    //private static final String XMLNS_PREFIX = "xmlns";
    // StAX Stream source
    private final XMLStreamReader staxStreamReader;
    //private Node _dom = null;
    private ContentHandler _sax=null;
    private LexicalHandler _lex=null;
    private SAXImpl _saxImpl=null;

    public StAXStream2SAX(XMLStreamReader staxSrc){
        staxStreamReader=staxSrc;
    }

    //Main Work Starts Here.
    public void parse() throws IOException, SAXException, XMLStreamException{
        bridge();
    }    public ContentHandler getContentHandler(){
        return _sax;
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException{
        return false;
    }    public void setContentHandler(ContentHandler handler) throws
            NullPointerException{
        _sax=handler;
        if(handler instanceof LexicalHandler){
            _lex=(LexicalHandler)handler;
        }
        if(handler instanceof SAXImpl){
            _saxImpl=(SAXImpl)handler;
        }
    }

    public void setFeature(String name,boolean value) throws
            SAXNotRecognizedException, SAXNotSupportedException{
    }    public void parse(InputSource unused) throws IOException, SAXException{
        try{
            bridge();
        }catch(XMLStreamException e){
            throw new SAXException(e);
        }
    }

    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException{
        return null;
    }

    public void setProperty(String name,Object value) throws
            SAXNotRecognizedException, SAXNotSupportedException{
    }    public void parse(String sysId) throws IOException, SAXException{
        throw new IOException("This method is not yet implemented.");
    }

    public void bridge() throws XMLStreamException{
        try{
            // remembers the nest level of elements to know when we are done.
            int depth=0;
            // skip over START_DOCUMENT
            int event=staxStreamReader.getEventType();
            if(event==XMLStreamConstants.START_DOCUMENT){
                event=staxStreamReader.next();
            }
            // If not a START_ELEMENT (e.g., a DTD), skip to next tag
            if(event!=XMLStreamConstants.START_ELEMENT){
                event=staxStreamReader.nextTag();
                // An error if a START_ELEMENT isn't found now
                if(event!=XMLStreamConstants.START_ELEMENT){
                    throw new IllegalStateException("The current event is "+
                            "not START_ELEMENT\n but"+event);
                }
            }
            handleStartDocument();
            do{
                // These are all of the events listed in the javadoc for
                // XMLEvent.
                // The spec only really describes 11 of them.
                switch(event){
                    case XMLStreamConstants.START_ELEMENT:
                        depth++;
                        handleStartElement();
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        handleEndElement();
                        depth--;
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        handleCharacters();
                        break;
                    case XMLStreamConstants.ENTITY_REFERENCE:
                        handleEntityReference();
                        break;
                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
                        handlePI();
                        break;
                    case XMLStreamConstants.COMMENT:
                        handleComment();
                        break;
                    case XMLStreamConstants.DTD:
                        handleDTD();
                        break;
                    case XMLStreamConstants.ATTRIBUTE:
                        handleAttribute();
                        break;
                    case XMLStreamConstants.NAMESPACE:
                        handleNamespace();
                        break;
                    case XMLStreamConstants.CDATA:
                        handleCDATA();
                        break;
                    case XMLStreamConstants.ENTITY_DECLARATION:
                        handleEntityDecl();
                        break;
                    case XMLStreamConstants.NOTATION_DECLARATION:
                        handleNotationDecl();
                        break;
                    case XMLStreamConstants.SPACE:
                        handleSpace();
                        break;
                    default:
                        throw new InternalError("processing event: "+event);
                }
                event=staxStreamReader.next();
            }while(depth!=0);
            handleEndDocument();
        }catch(SAXException e){
            throw new XMLStreamException(e);
        }
    }

    private void handleEndDocument() throws SAXException{
        _sax.endDocument();
    }

    private void handleStartDocument() throws SAXException{
        _sax.setDocumentLocator(new Locator2(){
            public String getPublicId(){
                return staxStreamReader.getLocation().getPublicId();
            }            public int getColumnNumber(){
                return staxStreamReader.getLocation().getColumnNumber();
            }

            public String getXMLVersion(){
                return staxStreamReader.getVersion();
            }            public int getLineNumber(){
                return staxStreamReader.getLocation().getLineNumber();
            }

            public String getEncoding(){
                return staxStreamReader.getEncoding();
            }

            public String getSystemId(){
                return staxStreamReader.getLocation().getSystemId();
            }




        });
        _sax.startDocument();
    }

    private void handlePI() throws XMLStreamException{
        try{
            _sax.processingInstruction(
                    staxStreamReader.getPITarget(),
                    staxStreamReader.getPIData());
        }catch(SAXException e){
            throw new XMLStreamException(e);
        }
    }

    private void handleCharacters() throws XMLStreamException{
        // workaround for bugid 5046319 - switch over to commented section
        // below when it is fixed.
        int textLength=staxStreamReader.getTextLength();
        char[] chars=new char[textLength];
        staxStreamReader.getTextCharacters(0,chars,0,textLength);
        try{
            _sax.characters(chars,0,chars.length);
        }catch(SAXException e){
            throw new XMLStreamException(e);
        }
//        int start = 0;
//        int len;
//        do {
//            len = staxStreamReader.getTextCharacters(start, buf, 0, buf.length);
//            start += len;
//            try {
//                _sax.characters(buf, 0, len);
//            } catch (SAXException e) {
//                throw new XMLStreamException(e);
//            }
//        } while (len == buf.length);
    }

    private void handleEndElement() throws XMLStreamException{
        QName qName=staxStreamReader.getName();
        try{
            //construct prefix:localName from qName
            String qname="";
            if(qName.getPrefix()!=null&&qName.getPrefix().trim().length()!=0){
                qname=qName.getPrefix()+":";
            }
            qname+=qName.getLocalPart();
            // fire endElement
            _sax.endElement(
                    qName.getNamespaceURI(),
                    qName.getLocalPart(),
                    qname);
            // end namespace bindings
            int nsCount=staxStreamReader.getNamespaceCount();
            for(int i=nsCount-1;i>=0;i--){
                String prefix=staxStreamReader.getNamespacePrefix(i);
                if(prefix==null){ // true for default namespace
                    prefix="";
                }
                _sax.endPrefixMapping(prefix);
            }
        }catch(SAXException e){
            throw new XMLStreamException(e);
        }
    }

    private void handleStartElement() throws XMLStreamException{
        try{
            // start namespace bindings
            int nsCount=staxStreamReader.getNamespaceCount();
            for(int i=0;i<nsCount;i++){
                String prefix=staxStreamReader.getNamespacePrefix(i);
                if(prefix==null){ // true for default namespace
                    prefix="";
                }
                _sax.startPrefixMapping(
                        prefix,
                        staxStreamReader.getNamespaceURI(i));
            }
            // fire startElement
            QName qName=staxStreamReader.getName();
            String prefix=qName.getPrefix();
            String rawname;
            if(prefix==null||prefix.length()==0)
                rawname=qName.getLocalPart();
            else
                rawname=prefix+':'+qName.getLocalPart();
            Attributes attrs=getAttributes();
            _sax.startElement(
                    qName.getNamespaceURI(),
                    qName.getLocalPart(),
                    rawname,
                    attrs);
        }catch(SAXException e){
            throw new XMLStreamException(e);
        }
    }

    private Attributes getAttributes(){
        AttributesImpl attrs=new AttributesImpl();
        int eventType=staxStreamReader.getEventType();
        if(eventType!=XMLStreamConstants.ATTRIBUTE
                &&eventType!=XMLStreamConstants.START_ELEMENT){
            throw new InternalError(
                    "getAttributes() attempting to process: "+eventType);
        }
        // in SAX, namespace declarations are not part of attributes by default.
        // (there's a property to control that, but as far as we are concerned
        // we don't use it.) So don't add xmlns:* to attributes.
        // gather non-namespace attrs
        for(int i=0;i<staxStreamReader.getAttributeCount();i++){
            String uri=staxStreamReader.getAttributeNamespace(i);
            if(uri==null) uri="";
            String localName=staxStreamReader.getAttributeLocalName(i);
            String prefix=staxStreamReader.getAttributePrefix(i);
            String qName;
            if(prefix==null||prefix.length()==0)
                qName=localName;
            else
                qName=prefix+':'+localName;
            String type=staxStreamReader.getAttributeType(i);
            String value=staxStreamReader.getAttributeValue(i);
            attrs.addAttribute(uri,localName,qName,type,value);
        }
        return attrs;
    }

    private void handleNamespace(){
        // no-op ???
        // namespace events don't normally occur outside of a startElement
        // or endElement
    }

    private void handleAttribute(){
        // no-op ???
        // attribute events don't normally occur outside of a startElement
        // or endElement
    }

    private void handleDTD(){
        // no-op ???
        // it seems like we need to pass this info along, but how?
    }

    private void handleComment(){
        // no-op ???
    }

    private void handleEntityReference(){
        // no-op ???
    }

    private void handleSpace(){
        // no-op ???
        // this event is listed in the javadoc, but not in the spec.
    }

    private void handleNotationDecl(){
        // no-op ???
        // this event is listed in the javadoc, but not in the spec.
    }

    private void handleEntityDecl(){
        // no-op ???
        // this event is listed in the javadoc, but not in the spec.
    }

    private void handleCDATA(){
        // no-op ???
        // this event is listed in the javadoc, but not in the spec.
    }

    public DTDHandler getDTDHandler(){
        return null;
    }

    public ErrorHandler getErrorHandler(){
        return null;
    }





    public void setDTDHandler(DTDHandler handler) throws NullPointerException{
    }

    public void setEntityResolver(EntityResolver resolver) throws
            NullPointerException{
    }

    public EntityResolver getEntityResolver(){
        return null;
    }

    public void setErrorHandler(ErrorHandler handler) throws
            NullPointerException{
    }





    public int getColumnNumber(){
        return 0;
    }

    public int getLineNumber(){
        return 0;
    }

    public String getPublicId(){
        return null;
    }

    public String getSystemId(){
        return null;
    }
}
