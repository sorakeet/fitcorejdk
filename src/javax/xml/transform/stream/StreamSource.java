/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform.stream;

import javax.xml.transform.Source;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;

public class StreamSource implements Source{
    public static final String FEATURE=
            "http://javax.xml.transform.stream.StreamSource/feature";
    //////////////////////////////////////////////////////////////////////
    // Internal state.
    //////////////////////////////////////////////////////////////////////
    private String publicId;
    private String systemId;
    private InputStream inputStream;
    private Reader reader;

    public StreamSource(){
    }

    public StreamSource(InputStream inputStream){
        setInputStream(inputStream);
    }

    public StreamSource(InputStream inputStream,String systemId){
        setInputStream(inputStream);
        setSystemId(systemId);
    }

    public void setSystemId(String systemId){
        this.systemId=systemId;
    }

    public String getSystemId(){
        return systemId;
    }

    public void setSystemId(File f){
        //convert file to appropriate URI, f.toURI().toASCIIString()
        //converts the URI to string as per rule specified in
        //RFC 2396,
        this.systemId=f.toURI().toASCIIString();
    }

    public StreamSource(Reader reader){
        setReader(reader);
    }

    public StreamSource(Reader reader,String systemId){
        setReader(reader);
        setSystemId(systemId);
    }

    public StreamSource(String systemId){
        this.systemId=systemId;
    }

    public StreamSource(File f){
        //convert file to appropriate URI, f.toURI().toASCIIString()
        //converts the URI to string as per rule specified in
        //RFC 2396,
        setSystemId(f.toURI().toASCIIString());
    }

    public InputStream getInputStream(){
        return inputStream;
    }

    public void setInputStream(InputStream inputStream){
        this.inputStream=inputStream;
    }

    public Reader getReader(){
        return reader;
    }

    public void setReader(Reader reader){
        this.reader=reader;
    }

    public String getPublicId(){
        return publicId;
    }

    public void setPublicId(String publicId){
        this.publicId=publicId;
    }
}
