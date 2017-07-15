/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DigestInputStream extends FilterInputStream{
    protected MessageDigest digest;
    /** NOTE: This should be made a generic UpdaterInputStream */
    private boolean on=true;

    public DigestInputStream(InputStream stream,MessageDigest digest){
        super(stream);
        setMessageDigest(digest);
    }

    public MessageDigest getMessageDigest(){
        return digest;
    }

    public void setMessageDigest(MessageDigest digest){
        this.digest=digest;
    }

    public int read() throws IOException{
        int ch=in.read();
        if(on&&ch!=-1){
            digest.update((byte)ch);
        }
        return ch;
    }

    public int read(byte[] b,int off,int len) throws IOException{
        int result=in.read(b,off,len);
        if(on&&result!=-1){
            digest.update(b,off,result);
        }
        return result;
    }

    public void on(boolean on){
        this.on=on;
    }

    public String toString(){
        return "[Digest Input Stream] "+digest.toString();
    }
}
