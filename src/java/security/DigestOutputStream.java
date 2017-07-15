/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DigestOutputStream extends FilterOutputStream{
    protected MessageDigest digest;
    private boolean on=true;

    public DigestOutputStream(OutputStream stream,MessageDigest digest){
        super(stream);
        setMessageDigest(digest);
    }

    public MessageDigest getMessageDigest(){
        return digest;
    }

    public void setMessageDigest(MessageDigest digest){
        this.digest=digest;
    }

    public void write(int b) throws IOException{
        out.write(b);
        if(on){
            digest.update((byte)b);
        }
    }

    public void write(byte[] b,int off,int len) throws IOException{
        out.write(b,off,len);
        if(on){
            digest.update(b,off,len);
        }
    }

    public void on(boolean on){
        this.on=on;
    }

    public String toString(){
        return "[Digest Output Stream] "+digest.toString();
    }
}
