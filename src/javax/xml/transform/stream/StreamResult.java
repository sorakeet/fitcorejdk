/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform.stream;

import javax.xml.transform.Result;
import java.io.File;
import java.io.OutputStream;
import java.io.Writer;

public class StreamResult implements Result{
    public static final String FEATURE=
            "http://javax.xml.transform.stream.StreamResult/feature";
    //////////////////////////////////////////////////////////////////////
    // Internal state.
    //////////////////////////////////////////////////////////////////////
    private String systemId;
    private OutputStream outputStream;
    private Writer writer;

    public StreamResult(){
    }

    public StreamResult(OutputStream outputStream){
        setOutputStream(outputStream);
    }

    public StreamResult(Writer writer){
        setWriter(writer);
    }

    public StreamResult(String systemId){
        this.systemId=systemId;
    }

    public StreamResult(File f){
        //convert file to appropriate URI, f.toURI().toASCIIString()
        //converts the URI to string as per rule specified in
        //RFC 2396,
        setSystemId(f.toURI().toASCIIString());
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

    public OutputStream getOutputStream(){
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream){
        this.outputStream=outputStream;
    }

    public Writer getWriter(){
        return writer;
    }

    public void setWriter(Writer writer){
        this.writer=writer;
    }
}
