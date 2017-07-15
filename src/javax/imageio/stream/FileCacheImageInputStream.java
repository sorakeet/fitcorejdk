/**
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.stream;

import com.sun.imageio.stream.StreamCloser;
import com.sun.imageio.stream.StreamFinalizer;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;

public class FileCacheImageInputStream extends ImageInputStreamImpl{
    private static final int BUFFER_LENGTH=1024;
    private final Object disposerReferent;
    private final DisposerRecord disposerRecord;
    private final StreamCloser.CloseAction closeAction;
    private InputStream stream;
    private File cacheFile;
    private RandomAccessFile cache;
    private byte[] buf=new byte[BUFFER_LENGTH];
    private long length=0L;
    private boolean foundEOF=false;

    public FileCacheImageInputStream(InputStream stream,File cacheDir)
            throws IOException{
        if(stream==null){
            throw new IllegalArgumentException("stream == null!");
        }
        if((cacheDir!=null)&&!(cacheDir.isDirectory())){
            throw new IllegalArgumentException("Not a directory!");
        }
        this.stream=stream;
        if(cacheDir==null)
            this.cacheFile=Files.createTempFile("imageio",".tmp").toFile();
        else
            this.cacheFile=Files.createTempFile(cacheDir.toPath(),"imageio",".tmp")
                    .toFile();
        this.cache=new RandomAccessFile(cacheFile,"rw");
        this.closeAction=StreamCloser.createCloseAction(this);
        StreamCloser.addToQueue(closeAction);
        disposerRecord=new StreamDisposerRecord(cacheFile,cache);
        if(getClass()==FileCacheImageInputStream.class){
            disposerReferent=new Object();
            Disposer.addRecord(disposerReferent,disposerRecord);
        }else{
            disposerReferent=new StreamFinalizer(this);
        }
    }

    public int read() throws IOException{
        checkClosed();
        bitOffset=0;
        long next=streamPos+1;
        long pos=readUntil(next);
        if(pos>=next){
            cache.seek(streamPos++);
            return cache.read();
        }else{
            return -1;
        }
    }

    private long readUntil(long pos) throws IOException{
        // We've already got enough data cached
        if(pos<length){
            return pos;
        }
        // pos >= length but length isn't getting any bigger, so return it
        if(foundEOF){
            return length;
        }
        long len=pos-length;
        cache.seek(length);
        while(len>0){
            // Copy a buffer's worth of data from the source to the cache
            // BUFFER_LENGTH will always fit into an int so this is safe
            int nbytes=
                    stream.read(buf,0,(int)Math.min(len,(long)BUFFER_LENGTH));
            if(nbytes==-1){
                foundEOF=true;
                return length;
            }
            cache.write(buf,0,nbytes);
            len-=nbytes;
            length+=nbytes;
        }
        return pos;
    }

    public int read(byte[] b,int off,int len) throws IOException{
        checkClosed();
        if(b==null){
            throw new NullPointerException("b == null!");
        }
        // Fix 4430357 - if off + len < 0, overflow occurred
        if(off<0||len<0||off+len>b.length||off+len<0){
            throw new IndexOutOfBoundsException
                    ("off < 0 || len < 0 || off+len > b.length || off+len < 0!");
        }
        bitOffset=0;
        if(len==0){
            return 0;
        }
        long pos=readUntil(streamPos+len);
        // len will always fit into an int so this is safe
        len=(int)Math.min((long)len,pos-streamPos);
        if(len>0){
            cache.seek(streamPos);
            cache.readFully(b,off,len);
            streamPos+=len;
            return len;
        }else{
            return -1;
        }
    }

    public boolean isCached(){
        return true;
    }

    public boolean isCachedMemory(){
        return false;
    }

    public boolean isCachedFile(){
        return true;
    }

    public void close() throws IOException{
        super.close();
        disposerRecord.dispose(); // this will close/delete the cache file
        stream=null;
        cache=null;
        cacheFile=null;
        StreamCloser.removeFromQueue(closeAction);
    }

    protected void finalize() throws Throwable{
        // Empty finalizer: for performance reasons we instead use the
        // Disposer mechanism for ensuring that the underlying
        // RandomAccessFile is closed/deleted prior to garbage collection
    }

    private static class StreamDisposerRecord implements DisposerRecord{
        private File cacheFile;
        private RandomAccessFile cache;

        public StreamDisposerRecord(File cacheFile,RandomAccessFile cache){
            this.cacheFile=cacheFile;
            this.cache=cache;
        }

        public synchronized void dispose(){
            if(cache!=null){
                try{
                    cache.close();
                }catch(IOException e){
                }finally{
                    cache=null;
                }
            }
            if(cacheFile!=null){
                cacheFile.delete();
                cacheFile=null;
            }
            // Note: Explicit removal of the stream from the StreamCloser
            // queue is not mandatory in this case, as it will be removed
            // automatically by GC shortly after this method is called.
        }
    }
}
