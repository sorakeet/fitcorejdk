/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.validation;

import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public abstract class ValidatorHandler implements ContentHandler{
    protected ValidatorHandler(){
    }

    public abstract ContentHandler getContentHandler();

    public abstract void setContentHandler(ContentHandler receiver);

    public abstract ErrorHandler getErrorHandler();

    public abstract void setErrorHandler(ErrorHandler errorHandler);

    public abstract LSResourceResolver getResourceResolver();

    public abstract void setResourceResolver(LSResourceResolver resourceResolver);

    public abstract TypeInfoProvider getTypeInfoProvider();

    public boolean getFeature(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException();
        }
        throw new SAXNotRecognizedException(name);
    }

    public void setFeature(String name,boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException();
        }
        throw new SAXNotRecognizedException(name);
    }

    public void setProperty(String name,Object object)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException();
        }
        throw new SAXNotRecognizedException(name);
    }

    public Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException();
        }
        throw new SAXNotRecognizedException(name);
    }
}
