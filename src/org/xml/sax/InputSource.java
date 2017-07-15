/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX input source.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: InputSource.java,v 1.2 2004/11/03 22:55:32 jsuttor Exp $
package org.xml.sax;

import java.io.InputStream;
import java.io.Reader;

public class InputSource{
    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////
    private String publicId;
    private String systemId;
    private InputStream byteStream;
    private String encoding;
    private Reader characterStream;

    public InputSource(){
    }

    public InputSource(String systemId){
        setSystemId(systemId);
    }

    public InputSource(InputStream byteStream){
        setByteStream(byteStream);
    }

    public InputSource(Reader characterStream){
        setCharacterStream(characterStream);
    }

    public String getPublicId(){
        return publicId;
    }

    public void setPublicId(String publicId){
        this.publicId=publicId;
    }

    public String getSystemId(){
        return systemId;
    }

    public void setSystemId(String systemId){
        this.systemId=systemId;
    }

    public InputStream getByteStream(){
        return byteStream;
    }

    public void setByteStream(InputStream byteStream){
        this.byteStream=byteStream;
    }

    public String getEncoding(){
        return encoding;
    }

    public void setEncoding(String encoding){
        this.encoding=encoding;
    }

    public Reader getCharacterStream(){
        return characterStream;
    }

    public void setCharacterStream(Reader characterStream){
        this.characterStream=characterStream;
    }
}
// end of InputSource.java
