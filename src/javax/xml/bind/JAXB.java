/**
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.beans.Introspector;
import java.io.*;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public final class JAXB{
    private static volatile WeakReference<Cache> cache;

    private JAXB(){
    }

    public static <T> T unmarshal(File xml,Class<T> type){
        try{
            JAXBElement<T> item=getContext(type).createUnmarshaller().unmarshal(new StreamSource(xml),type);
            return item.getValue();
        }catch(JAXBException e){
            throw new DataBindingException(e);
        }
    }

    private static <T> JAXBContext getContext(Class<T> type) throws JAXBException{
        WeakReference<Cache> c=cache;
        if(c!=null){
            Cache d=c.get();
            if(d!=null&&d.type==type)
                return d.context;
        }
        // overwrite the cache
        Cache d=new Cache(type);
        cache=new WeakReference<Cache>(d);
        return d.context;
    }

    public static <T> T unmarshal(URL xml,Class<T> type){
        try{
            JAXBElement<T> item=getContext(type).createUnmarshaller().unmarshal(toSource(xml),type);
            return item.getValue();
        }catch(JAXBException e){
            throw new DataBindingException(e);
        }catch(IOException e){
            throw new DataBindingException(e);
        }
    }

    private static Source toSource(Object xml) throws IOException{
        if(xml==null)
            throw new IllegalArgumentException("no XML is given");
        if(xml instanceof String){
            try{
                xml=new URI((String)xml);
            }catch(URISyntaxException e){
                xml=new File((String)xml);
            }
        }
        if(xml instanceof File){
            File file=(File)xml;
            return new StreamSource(file);
        }
        if(xml instanceof URI){
            URI uri=(URI)xml;
            xml=uri.toURL();
        }
        if(xml instanceof URL){
            URL url=(URL)xml;
            return new StreamSource(url.toExternalForm());
        }
        if(xml instanceof InputStream){
            InputStream in=(InputStream)xml;
            return new StreamSource(in);
        }
        if(xml instanceof Reader){
            Reader r=(Reader)xml;
            return new StreamSource(r);
        }
        if(xml instanceof Source){
            return (Source)xml;
        }
        throw new IllegalArgumentException("I don't understand how to handle "+xml.getClass());
    }

    public static <T> T unmarshal(URI xml,Class<T> type){
        try{
            JAXBElement<T> item=getContext(type).createUnmarshaller().unmarshal(toSource(xml),type);
            return item.getValue();
        }catch(JAXBException e){
            throw new DataBindingException(e);
        }catch(IOException e){
            throw new DataBindingException(e);
        }
    }

    public static <T> T unmarshal(String xml,Class<T> type){
        try{
            JAXBElement<T> item=getContext(type).createUnmarshaller().unmarshal(toSource(xml),type);
            return item.getValue();
        }catch(JAXBException e){
            throw new DataBindingException(e);
        }catch(IOException e){
            throw new DataBindingException(e);
        }
    }

    public static <T> T unmarshal(InputStream xml,Class<T> type){
        try{
            JAXBElement<T> item=getContext(type).createUnmarshaller().unmarshal(toSource(xml),type);
            return item.getValue();
        }catch(JAXBException e){
            throw new DataBindingException(e);
        }catch(IOException e){
            throw new DataBindingException(e);
        }
    }

    public static <T> T unmarshal(Reader xml,Class<T> type){
        try{
            JAXBElement<T> item=getContext(type).createUnmarshaller().unmarshal(toSource(xml),type);
            return item.getValue();
        }catch(JAXBException e){
            throw new DataBindingException(e);
        }catch(IOException e){
            throw new DataBindingException(e);
        }
    }

    public static <T> T unmarshal(Source xml,Class<T> type){
        try{
            JAXBElement<T> item=getContext(type).createUnmarshaller().unmarshal(toSource(xml),type);
            return item.getValue();
        }catch(JAXBException e){
            throw new DataBindingException(e);
        }catch(IOException e){
            throw new DataBindingException(e);
        }
    }

    public static void marshal(Object jaxbObject,File xml){
        _marshal(jaxbObject,xml);
    }

    private static void _marshal(Object jaxbObject,Object xml){
        try{
            JAXBContext context;
            if(jaxbObject instanceof JAXBElement){
                context=getContext(((JAXBElement<?>)jaxbObject).getDeclaredType());
            }else{
                Class<?> clazz=jaxbObject.getClass();
                XmlRootElement r=clazz.getAnnotation(XmlRootElement.class);
                context=getContext(clazz);
                if(r==null){
                    // we need to infer the name
                    jaxbObject=new JAXBElement(new QName(inferName(clazz)),clazz,jaxbObject);
                }
            }
            Marshaller m=context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
            m.marshal(jaxbObject,toResult(xml));
        }catch(JAXBException e){
            throw new DataBindingException(e);
        }catch(IOException e){
            throw new DataBindingException(e);
        }
    }

    private static String inferName(Class clazz){
        return Introspector.decapitalize(clazz.getSimpleName());
    }

    private static Result toResult(Object xml) throws IOException{
        if(xml==null)
            throw new IllegalArgumentException("no XML is given");
        if(xml instanceof String){
            try{
                xml=new URI((String)xml);
            }catch(URISyntaxException e){
                xml=new File((String)xml);
            }
        }
        if(xml instanceof File){
            File file=(File)xml;
            return new StreamResult(file);
        }
        if(xml instanceof URI){
            URI uri=(URI)xml;
            xml=uri.toURL();
        }
        if(xml instanceof URL){
            URL url=(URL)xml;
            URLConnection con=url.openConnection();
            con.setDoOutput(true);
            con.setDoInput(false);
            con.connect();
            return new StreamResult(con.getOutputStream());
        }
        if(xml instanceof OutputStream){
            OutputStream os=(OutputStream)xml;
            return new StreamResult(os);
        }
        if(xml instanceof Writer){
            Writer w=(Writer)xml;
            return new StreamResult(w);
        }
        if(xml instanceof Result){
            return (Result)xml;
        }
        throw new IllegalArgumentException("I don't understand how to handle "+xml.getClass());
    }

    public static void marshal(Object jaxbObject,URL xml){
        _marshal(jaxbObject,xml);
    }

    public static void marshal(Object jaxbObject,URI xml){
        _marshal(jaxbObject,xml);
    }

    public static void marshal(Object jaxbObject,String xml){
        _marshal(jaxbObject,xml);
    }

    public static void marshal(Object jaxbObject,OutputStream xml){
        _marshal(jaxbObject,xml);
    }

    public static void marshal(Object jaxbObject,Writer xml){
        _marshal(jaxbObject,xml);
    }

    public static void marshal(Object jaxbObject,Result xml){
        _marshal(jaxbObject,xml);
    }

    private static final class Cache{
        final Class type;
        final JAXBContext context;

        public Cache(Class type) throws JAXBException{
            this.type=type;
            this.context=JAXBContext.newInstance(type);
        }
    }
}
