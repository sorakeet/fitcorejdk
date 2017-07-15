/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.stream;

import com.sun.imageio.stream.StreamFinalizer;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;

import java.io.IOException;
import java.io.InputStream;

public class MemoryCacheImageInputStream extends ImageInputStreamImpl{
    private final Object disposerReferent;
    private final DisposerRecord disposerRecord;
    private InputStream stream;
    private MemoryCache cache=new MemoryCache();

    public MemoryCacheImageInputStream(InputStream stream){
        if(stream==null){
            throw new IllegalArgumentException("stream == null!");
        }
        this.stream=stream;
        disposerRecord=new StreamDisposerRecord(cache);
        if(getClass()==MemoryCacheImageInputStream.class){
            disposerReferent=new Object();
            Disposer.addRecord(disposerReferent,disposerRecord);
        }else{
            disposerReferent=new StreamFinalizer(this);
        }
    }

    public int read() throws IOException{
        checkClosed();
        bitOffset=0;
        long pos=cache.loadFromStream(stream,streamPos+1);
        if(pos>=streamPos+1){
            return cache.read(streamPos++);
        }else{
            return -1;
        }
    }

    public int read(byte[] b,int off,int len) throws IOException{
        checkClosed();
        if(b==null){
            throw new NullPointerException("b == null!");
        }
        if(off<0||len<0||off+len>b.length||off+len<0){
            throw new IndexOutOfBoundsException
                    ("off < 0 || len < 0 || off+len > b.length || off+len < 0!");
        }
        bitOffset=0;
        if(len==0){
            return 0;
        }
        long pos=cache.loadFromStream(stream,streamPos+len);
        len=(int)(pos-streamPos);  // In case stream ended early
        if(len>0){
            cache.read(b,off,len,streamPos);
            streamPos+=len;
            return len;
        }else{
            return -1;
        }
    }

    public void flushBefore(long pos) throws IOException{
        super.flushBefore(pos); // this will call checkClosed() for us
        cache.disposeBefore(pos);
    }

    public boolean isCached(){
        return true;
    }

    public boolean isCachedMemory(){
        return true;
    }

    public boolean isCachedFile(){
        return false;
    }

    public void close() throws IOException{
        super.close();
        disposerRecord.dispose(); // this resets the MemoryCache
        stream=null;
        cache=null;
    }

    protected void finalize() throws Throwable{
        // Empty finalizer: for performance reasons we instead use the
        // Disposer mechanism for ensuring that the underlying
        // MemoryCache is reset prior to garbage collection
    }

    private static class StreamDisposerRecord implements DisposerRecord{
        private MemoryCache cache;

        public StreamDisposerRecord(MemoryCache cache){
            this.cache=cache;
        }

        public synchronized void dispose(){
            if(cache!=null){
                cache.reset();
                cache=null;
            }
        }
    }
}
