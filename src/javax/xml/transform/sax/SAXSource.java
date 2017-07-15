/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform.sax;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class SAXSource implements Source{
    public static final String FEATURE=
            "http://javax.xml.transform.sax.SAXSource/feature";
    private XMLReader reader;
    private InputSource inputSource;

    public SAXSource(){
    }

    public SAXSource(XMLReader reader,InputSource inputSource){
        this.reader=reader;
        this.inputSource=inputSource;
    }

    public SAXSource(InputSource inputSource){
        this.inputSource=inputSource;
    }

    public static InputSource sourceToInputSource(Source source){
        if(source instanceof SAXSource){
            return ((SAXSource)source).getInputSource();
        }else if(source instanceof StreamSource){
            StreamSource ss=(StreamSource)source;
            InputSource isource=new InputSource(ss.getSystemId());
            isource.setByteStream(ss.getInputStream());
            isource.setCharacterStream(ss.getReader());
            isource.setPublicId(ss.getPublicId());
            return isource;
        }else{
            return null;
        }
    }

    public InputSource getInputSource(){
        return inputSource;
    }

    public void setInputSource(InputSource inputSource){
        this.inputSource=inputSource;
    }    public void setSystemId(String systemId){
        if(null==inputSource){
            inputSource=new InputSource(systemId);
        }else{
            inputSource.setSystemId(systemId);
        }
    }

    public XMLReader getXMLReader(){
        return reader;
    }    public String getSystemId(){
        if(inputSource==null){
            return null;
        }else{
            return inputSource.getSystemId();
        }
    }

    public void setXMLReader(XMLReader reader){
        this.reader=reader;
    }



}
