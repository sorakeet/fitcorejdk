/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.helpers;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import java.io.File;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class AbstractUnmarshallerImpl implements Unmarshaller{
    protected boolean validating=false;
    private ValidationEventHandler eventHandler=
            new DefaultValidationEventHandler();
    private XMLReader reader=null;

    public final Object unmarshal(File f) throws JAXBException{
        if(f==null){
            throw new IllegalArgumentException(
                    Messages.format(Messages.MUST_NOT_BE_NULL,"file"));
        }
        try{
            // copied from JAXP
            String path=f.getAbsolutePath();
            if(File.separatorChar!='/')
                path=path.replace(File.separatorChar,'/');
            if(!path.startsWith("/"))
                path="/"+path;
            if(!path.endsWith("/")&&f.isDirectory())
                path=path+"/";
            return unmarshal(new URL("file","",path));
        }catch(MalformedURLException e){
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public final Object unmarshal(java.io.InputStream is)
            throws JAXBException{
        if(is==null){
            throw new IllegalArgumentException(
                    Messages.format(Messages.MUST_NOT_BE_NULL,"is"));
        }
        InputSource isrc=new InputSource(is);
        return unmarshal(isrc);
    }

    public final Object unmarshal(Reader reader) throws JAXBException{
        if(reader==null){
            throw new IllegalArgumentException(
                    Messages.format(Messages.MUST_NOT_BE_NULL,"reader"));
        }
        InputSource isrc=new InputSource(reader);
        return unmarshal(isrc);
    }

    public final Object unmarshal(URL url) throws JAXBException{
        if(url==null){
            throw new IllegalArgumentException(
                    Messages.format(Messages.MUST_NOT_BE_NULL,"url"));
        }
        return unmarshal(url.toExternalForm());
    }

    public final Object unmarshal(InputSource source) throws JAXBException{
        if(source==null){
            throw new IllegalArgumentException(
                    Messages.format(Messages.MUST_NOT_BE_NULL,"source"));
        }
        return unmarshal(getXMLReader(),source);
    }

    public <T> JAXBElement<T> unmarshal(Node node,Class<T> expectedType) throws JAXBException{
        throw new UnsupportedOperationException();
    }

    public Object unmarshal(Source source) throws JAXBException{
        if(source==null){
            throw new IllegalArgumentException(
                    Messages.format(Messages.MUST_NOT_BE_NULL,"source"));
        }
        if(source instanceof SAXSource)
            return unmarshal((SAXSource)source);
        if(source instanceof StreamSource)
            return unmarshal(streamSourceToInputSource((StreamSource)source));
        if(source instanceof DOMSource)
            return unmarshal(((DOMSource)source).getNode());
        // we don't handle other types of Source
        throw new IllegalArgumentException();
    }

    // use the client specified XMLReader contained in the SAXSource.
    private Object unmarshal(SAXSource source) throws JAXBException{
        XMLReader r=source.getXMLReader();
        if(r==null)
            r=getXMLReader();
        return unmarshal(r,source.getInputSource());
    }

    protected XMLReader getXMLReader() throws JAXBException{
        if(reader==null){
            try{
                SAXParserFactory parserFactory;
                parserFactory=SAXParserFactory.newInstance();
                parserFactory.setNamespaceAware(true);
                // there is no point in asking a validation because
                // there is no guarantee that the document will come with
                // a proper schemaLocation.
                parserFactory.setValidating(false);
                reader=parserFactory.newSAXParser().getXMLReader();
            }catch(ParserConfigurationException e){
                throw new JAXBException(e);
            }catch(SAXException e){
                throw new JAXBException(e);
            }
        }
        return reader;
    }

    protected abstract Object unmarshal(XMLReader reader,InputSource source) throws JAXBException;

    private static InputSource streamSourceToInputSource(StreamSource ss){
        InputSource is=new InputSource();
        is.setSystemId(ss.getSystemId());
        is.setByteStream(ss.getInputStream());
        is.setCharacterStream(ss.getReader());
        return is;
    }

    public <T> JAXBElement<T> unmarshal(Source source,Class<T> expectedType) throws JAXBException{
        throw new UnsupportedOperationException();
    }    public boolean isValidating() throws JAXBException{
        return validating;
    }

    public Object unmarshal(XMLStreamReader reader) throws JAXBException{
        throw new UnsupportedOperationException();
    }    public void setEventHandler(ValidationEventHandler handler)
            throws JAXBException{
        if(handler==null){
            eventHandler=new DefaultValidationEventHandler();
        }else{
            eventHandler=handler;
        }
    }

    public <T> JAXBElement<T> unmarshal(XMLStreamReader reader,Class<T> expectedType) throws JAXBException{
        throw new UnsupportedOperationException();
    }    public void setValidating(boolean validating) throws JAXBException{
        this.validating=validating;
    }

    public Object unmarshal(XMLEventReader reader) throws JAXBException{
        throw new UnsupportedOperationException();
    }    public ValidationEventHandler getEventHandler() throws JAXBException{
        return eventHandler;
    }

    public <T> JAXBElement<T> unmarshal(XMLEventReader reader,Class<T> expectedType) throws JAXBException{
        throw new UnsupportedOperationException();
    }

    private Object unmarshal(String url) throws JAXBException{
        return unmarshal(new InputSource(url));
    }    public void setProperty(String name,Object value)
            throws PropertyException{
        if(name==null){
            throw new IllegalArgumentException(
                    Messages.format(Messages.MUST_NOT_BE_NULL,"name"));
        }
        throw new PropertyException(name,value);
    }

    protected UnmarshalException createUnmarshalException(SAXException e){
        // check the nested exception to see if it's an UnmarshalException
        Exception nested=e.getException();
        if(nested instanceof UnmarshalException)
            return (UnmarshalException)nested;
        if(nested instanceof RuntimeException)
            // typically this is an unexpected exception,
            // just throw it rather than wrap it, so that the full stack
            // trace can be displayed.
            throw (RuntimeException)nested;
        // otherwise simply wrap it
        if(nested!=null)
            return new UnmarshalException(nested);
        else
            return new UnmarshalException(e);
    }    public Object getProperty(String name)
            throws PropertyException{
        if(name==null){
            throw new IllegalArgumentException(
                    Messages.format(Messages.MUST_NOT_BE_NULL,"name"));
        }
        throw new PropertyException(name);
    }













    public void setSchema(Schema schema){
        throw new UnsupportedOperationException();
    }

    public Schema getSchema(){
        throw new UnsupportedOperationException();
    }

    public void setAdapter(XmlAdapter adapter){
        if(adapter==null)
            throw new IllegalArgumentException();
        setAdapter((Class)adapter.getClass(),adapter);
    }

    public <A extends XmlAdapter> void setAdapter(Class<A> type,A adapter){
        throw new UnsupportedOperationException();
    }

    public <A extends XmlAdapter> A getAdapter(Class<A> type){
        throw new UnsupportedOperationException();
    }

    public void setAttachmentUnmarshaller(AttachmentUnmarshaller au){
        throw new UnsupportedOperationException();
    }

    public AttachmentUnmarshaller getAttachmentUnmarshaller(){
        throw new UnsupportedOperationException();
    }

    public void setListener(Listener listener){
        throw new UnsupportedOperationException();
    }

    public Listener getListener(){
        throw new UnsupportedOperationException();
    }
}
