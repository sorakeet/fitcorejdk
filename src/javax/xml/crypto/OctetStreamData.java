/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * $Id: OctetStreamData.java,v 1.3 2005/05/10 15:47:42 mullan Exp $
 */
/**
 * $Id: OctetStreamData.java,v 1.3 2005/05/10 15:47:42 mullan Exp $
 */
package javax.xml.crypto;

import java.io.InputStream;

public class OctetStreamData implements Data{
    private InputStream octetStream;
    private String uri;
    private String mimeType;

    public OctetStreamData(InputStream octetStream){
        if(octetStream==null){
            throw new NullPointerException("octetStream is null");
        }
        this.octetStream=octetStream;
    }

    public OctetStreamData(InputStream octetStream,String uri,
                           String mimeType){
        if(octetStream==null){
            throw new NullPointerException("octetStream is null");
        }
        this.octetStream=octetStream;
        this.uri=uri;
        this.mimeType=mimeType;
    }

    public InputStream getOctetStream(){
        return octetStream;
    }

    public String getURI(){
        return uri;
    }

    public String getMimeType(){
        return mimeType;
    }
}
