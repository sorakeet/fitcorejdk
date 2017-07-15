/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.parsers;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.validation.Schema;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class SAXParser{
    protected SAXParser(){
    }

    public void reset(){
        // implementors should override this method
        throw new UnsupportedOperationException(
                "This SAXParser, \""+this.getClass().getName()+"\", does not support the reset functionality."
                        +"  Specification \""+this.getClass().getPackage().getSpecificationTitle()+"\""
                        +" version \""+this.getClass().getPackage().getSpecificationVersion()+"\""
        );
    }

    public void parse(InputStream is,HandlerBase hb)
            throws SAXException, IOException{
        if(is==null){
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        InputSource input=new InputSource(is);
        this.parse(input,hb);
    }

    public void parse(InputSource is,HandlerBase hb)
            throws SAXException, IOException{
        if(is==null){
            throw new IllegalArgumentException("InputSource cannot be null");
        }
        Parser parser=this.getParser();
        if(hb!=null){
            parser.setDocumentHandler(hb);
            parser.setEntityResolver(hb);
            parser.setErrorHandler(hb);
            parser.setDTDHandler(hb);
        }
        parser.parse(is);
    }

    public abstract Parser getParser() throws SAXException;

    public void parse(
            InputStream is,
            HandlerBase hb,
            String systemId)
            throws SAXException, IOException{
        if(is==null){
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        InputSource input=new InputSource(is);
        input.setSystemId(systemId);
        this.parse(input,hb);
    }

    public void parse(InputStream is,DefaultHandler dh)
            throws SAXException, IOException{
        if(is==null){
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        InputSource input=new InputSource(is);
        this.parse(input,dh);
    }

    public void parse(InputSource is,DefaultHandler dh)
            throws SAXException, IOException{
        if(is==null){
            throw new IllegalArgumentException("InputSource cannot be null");
        }
        XMLReader reader=this.getXMLReader();
        if(dh!=null){
            reader.setContentHandler(dh);
            reader.setEntityResolver(dh);
            reader.setErrorHandler(dh);
            reader.setDTDHandler(dh);
        }
        reader.parse(is);
    }

    public abstract XMLReader getXMLReader() throws SAXException;

    public void parse(
            InputStream is,
            DefaultHandler dh,
            String systemId)
            throws SAXException, IOException{
        if(is==null){
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        InputSource input=new InputSource(is);
        input.setSystemId(systemId);
        this.parse(input,dh);
    }

    public void parse(String uri,HandlerBase hb)
            throws SAXException, IOException{
        if(uri==null){
            throw new IllegalArgumentException("uri cannot be null");
        }
        InputSource input=new InputSource(uri);
        this.parse(input,hb);
    }

    public void parse(String uri,DefaultHandler dh)
            throws SAXException, IOException{
        if(uri==null){
            throw new IllegalArgumentException("uri cannot be null");
        }
        InputSource input=new InputSource(uri);
        this.parse(input,dh);
    }

    public void parse(File f,HandlerBase hb)
            throws SAXException, IOException{
        if(f==null){
            throw new IllegalArgumentException("File cannot be null");
        }
        //convert file to appropriate URI, f.toURI().toASCIIString()
        //converts the URI to string as per rule specified in
        //RFC 2396,
        InputSource input=new InputSource(f.toURI().toASCIIString());
        this.parse(input,hb);
    }

    public void parse(File f,DefaultHandler dh)
            throws SAXException, IOException{
        if(f==null){
            throw new IllegalArgumentException("File cannot be null");
        }
        //convert file to appropriate URI, f.toURI().toASCIIString()
        //converts the URI to string as per rule specified in
        //RFC 2396,
        InputSource input=new InputSource(f.toURI().toASCIIString());
        this.parse(input,dh);
    }

    public abstract boolean isNamespaceAware();

    public abstract boolean isValidating();

    public abstract void setProperty(String name,Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException;

    public abstract Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException;

    public Schema getSchema(){
        throw new UnsupportedOperationException(
                "This parser does not support specification \""
                        +this.getClass().getPackage().getSpecificationTitle()
                        +"\" version \""
                        +this.getClass().getPackage().getSpecificationVersion()
                        +"\""
        );
    }

    public boolean isXIncludeAware(){
        throw new UnsupportedOperationException(
                "This parser does not support specification \""
                        +this.getClass().getPackage().getSpecificationTitle()
                        +"\" version \""
                        +this.getClass().getPackage().getSpecificationVersion()
                        +"\""
        );
    }
}
