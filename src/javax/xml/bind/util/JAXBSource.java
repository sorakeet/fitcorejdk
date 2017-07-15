/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.util;

import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.sax.SAXSource;

public class JAXBSource extends SAXSource{
    private final Marshaller marshaller;
    private final Object contentObject;
    // this object will pretend as an XMLReader.
    // no matter what parameter is specified to the parse method,
    // it just parse the contentObject.
    private final XMLReader pseudoParser=new XMLReader(){
        private LexicalHandler lexicalHandler;
        // we will store this value but never use it by ourselves.
        private EntityResolver entityResolver;
        private DTDHandler dtdHandler;
        // SAX allows ContentHandler to be changed during the parsing,
        // but JAXB doesn't. So this repeater will sit between those
        // two components.
        private XMLFilter repeater=new XMLFilterImpl();
        private ErrorHandler errorHandler;

        public boolean getFeature(String name) throws SAXNotRecognizedException{
            if(name.equals("http://xml.org/sax/features/namespaces"))
                return true;
            if(name.equals("http://xml.org/sax/features/namespace-prefixes"))
                return false;
            throw new SAXNotRecognizedException(name);
        }

        public void setFeature(String name,boolean value) throws SAXNotRecognizedException{
            if(name.equals("http://xml.org/sax/features/namespaces")&&value)
                return;
            if(name.equals("http://xml.org/sax/features/namespace-prefixes")&&!value)
                return;
            throw new SAXNotRecognizedException(name);
        }        public void setEntityResolver(EntityResolver resolver){
            this.entityResolver=resolver;
        }

        public Object getProperty(String name) throws SAXNotRecognizedException{
            if("http://xml.org/sax/properties/lexical-handler".equals(name)){
                return lexicalHandler;
            }
            throw new SAXNotRecognizedException(name);
        }        public EntityResolver getEntityResolver(){
            return entityResolver;
        }

        public void setProperty(String name,Object value) throws SAXNotRecognizedException{
            if("http://xml.org/sax/properties/lexical-handler".equals(name)){
                this.lexicalHandler=(LexicalHandler)value;
                return;
            }
            throw new SAXNotRecognizedException(name);
        }

        public void setDTDHandler(DTDHandler handler){
            this.dtdHandler=handler;
        }

        public DTDHandler getDTDHandler(){
            return dtdHandler;
        }



        public void setContentHandler(ContentHandler handler){
            repeater.setContentHandler(handler);
        }

        public ContentHandler getContentHandler(){
            return repeater.getContentHandler();
        }



        public void setErrorHandler(ErrorHandler handler){
            this.errorHandler=handler;
        }

        public ErrorHandler getErrorHandler(){
            return errorHandler;
        }

        public void parse(InputSource input) throws SAXException{
            parse();
        }

        public void parse(String systemId) throws SAXException{
            parse();
        }

        public void parse() throws SAXException{
            // parses a content object by using the given marshaller
            // SAX events will be sent to the repeater, and the repeater
            // will further forward it to an appropriate component.
            try{
                marshaller.marshal(contentObject,(XMLFilterImpl)repeater);
            }catch(JAXBException e){
                // wrap it to a SAXException
                SAXParseException se=
                        new SAXParseException(e.getMessage(),
                                null,null,-1,-1,e);
                // if the consumer sets an error handler, it is our responsibility
                // to notify it.
                if(errorHandler!=null)
                    errorHandler.fatalError(se);
                // this is a fatal error. Even if the error handler
                // returns, we will abort anyway.
                throw se;
            }
        }
    };
    public JAXBSource(JAXBContext context,Object contentObject)
            throws JAXBException{
        this(
                (context==null)?
                        assertionFailed(Messages.format(Messages.SOURCE_NULL_CONTEXT)):
                        context.createMarshaller(),
                (contentObject==null)?
                        assertionFailed(Messages.format(Messages.SOURCE_NULL_CONTENT)):
                        contentObject);
    }
    public JAXBSource(Marshaller marshaller,Object contentObject)
            throws JAXBException{
        if(marshaller==null)
            throw new JAXBException(
                    Messages.format(Messages.SOURCE_NULL_MARSHALLER));
        if(contentObject==null)
            throw new JAXBException(
                    Messages.format(Messages.SOURCE_NULL_CONTENT));
        this.marshaller=marshaller;
        this.contentObject=contentObject;
        super.setXMLReader(pseudoParser);
        // pass a dummy InputSource. We don't care
        super.setInputSource(new InputSource());
    }

    private static Marshaller assertionFailed(String message)
            throws JAXBException{
        throw new JAXBException(message);
    }
}
