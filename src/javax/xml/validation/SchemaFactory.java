/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.validation;

import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.net.URL;

public abstract class SchemaFactory{
    private static SecuritySupport ss=new SecuritySupport();

    protected SchemaFactory(){
    }

    public static SchemaFactory newInstance(String schemaLanguage){
        ClassLoader cl;
        cl=ss.getContextClassLoader();
        if(cl==null){
            //cl = ClassLoader.getSystemClassLoader();
            //use the current class loader
            cl=SchemaFactory.class.getClassLoader();
        }
        SchemaFactory f=new SchemaFactoryFinder(cl).newFactory(schemaLanguage);
        if(f==null){
            throw new IllegalArgumentException(
                    "No SchemaFactory"
                            +" that implements the schema language specified by: "+schemaLanguage
                            +" could be loaded");
        }
        return f;
    }

    public static SchemaFactory newInstance(String schemaLanguage,String factoryClassName,ClassLoader classLoader){
        ClassLoader cl=classLoader;
        if(cl==null){
            cl=ss.getContextClassLoader();
        }
        SchemaFactory f=new SchemaFactoryFinder(cl).createInstance(factoryClassName);
        if(f==null){
            throw new IllegalArgumentException(
                    "Factory "+factoryClassName
                            +" could not be loaded to implement the schema language specified by: "+schemaLanguage);
        }
        //if this factory supports the given schemalanguage return this factory else thrown exception
        if(f.isSchemaLanguageSupported(schemaLanguage)){
            return f;
        }else{
            throw new IllegalArgumentException(
                    "Factory "+f.getClass().getName()
                            +" does not implement the schema language specified by: "+schemaLanguage);
        }
    }

    public abstract boolean isSchemaLanguageSupported(String schemaLanguage);

    public boolean getFeature(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException("the name parameter is null");
        }
        throw new SAXNotRecognizedException(name);
    }

    public void setFeature(String name,boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException("the name parameter is null");
        }
        throw new SAXNotRecognizedException(name);
    }

    public void setProperty(String name,Object object)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException("the name parameter is null");
        }
        throw new SAXNotRecognizedException(name);
    }

    public Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException("the name parameter is null");
        }
        throw new SAXNotRecognizedException(name);
    }

    public abstract ErrorHandler getErrorHandler();

    public abstract void setErrorHandler(ErrorHandler errorHandler);

    public abstract LSResourceResolver getResourceResolver();

    public abstract void setResourceResolver(LSResourceResolver resourceResolver);

    public Schema newSchema(File schema) throws SAXException{
        return newSchema(new StreamSource(schema));
    }

    public Schema newSchema(Source schema) throws SAXException{
        return newSchema(new Source[]{schema});
    }

    public abstract Schema newSchema(Source[] schemas) throws SAXException;

    public Schema newSchema(URL schema) throws SAXException{
        return newSchema(new StreamSource(schema.toExternalForm()));
    }

    public abstract Schema newSchema() throws SAXException;
}
