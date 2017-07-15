/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.parsers;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.validation.Schema;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class DocumentBuilder{
    protected DocumentBuilder(){
    }

    public void reset(){
        // implementors should override this method
        throw new UnsupportedOperationException(
                "This DocumentBuilder, \""+this.getClass().getName()+"\", does not support the reset functionality."
                        +"  Specification \""+this.getClass().getPackage().getSpecificationTitle()+"\""
                        +" version \""+this.getClass().getPackage().getSpecificationVersion()+"\""
        );
    }

    public Document parse(InputStream is)
            throws SAXException, IOException{
        if(is==null){
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        InputSource in=new InputSource(is);
        return parse(in);
    }

    public abstract Document parse(InputSource is)
            throws SAXException, IOException;

    public Document parse(InputStream is,String systemId)
            throws SAXException, IOException{
        if(is==null){
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        InputSource in=new InputSource(is);
        in.setSystemId(systemId);
        return parse(in);
    }

    public Document parse(String uri)
            throws SAXException, IOException{
        if(uri==null){
            throw new IllegalArgumentException("URI cannot be null");
        }
        InputSource in=new InputSource(uri);
        return parse(in);
    }

    public Document parse(File f) throws SAXException, IOException{
        if(f==null){
            throw new IllegalArgumentException("File cannot be null");
        }
        //convert file to appropriate URI, f.toURI().toASCIIString()
        //converts the URI to string as per rule specified in
        //RFC 2396,
        InputSource in=new InputSource(f.toURI().toASCIIString());
        return parse(in);
    }

    public abstract boolean isNamespaceAware();

    public abstract boolean isValidating();

    public abstract void setEntityResolver(EntityResolver er);

    public abstract void setErrorHandler(ErrorHandler eh);

    public abstract Document newDocument();

    public abstract DOMImplementation getDOMImplementation();

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
