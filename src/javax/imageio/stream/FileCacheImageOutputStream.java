/**
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.stream;

import com.sun.imageio.stream.StreamCloser;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;

public class FileCacheImageOutputStream extends ImageOutputStreamImpl{
    private final StreamCloser.CloseAction closeAction;
    private OutputStream stream;
    private File cacheFile;
    private RandomAccessFile cache;
    // Pos after last (rightmost) byte written
    private long maxStreamPos=0L;

    public FileCacheImageOutputStream(OutputStream stream,File cacheDir)
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
    }

    public int read() throws IOException{
        checkClosed();
        bitOffset=0;
        int val=cache.read();
        if(val!=-1){
            ++streamPos;
        }
        return val;
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
        int nbytes=cache.read(b,off,len);
        if(nbytes!=-1){
            streamPos+=nbytes;
        }
        return nbytes;
    }

    public long length(){
        try{
            checkClosed();
            return cache.length();
        }catch(IOException e){
            return -1L;
        }
    }

    public void seek(long pos) throws IOException{
        checkClosed();
        if(pos<flushedPos){
            throw new IndexOutOfBoundsException();
        }
        cache.seek(pos);
        this.streamPos=cache.getFilePointer();
        maxStreamPos=Math.max(maxStreamPos,streamPos);
        this.bitOffset=0;
    }

    public void flushBefore(long pos) throws IOException{
        long oFlushedPos=flushedPos;
        super.flushBefore(pos); // this will call checkClosed() for us
        long flushBytes=flushedPos-oFlushedPos;
        if(flushBytes>0){
            int bufLen=512;
            byte[] buf=new byte[bufLen];
            cache.seek(oFlushedPos);
            while(flushBytes>0){
                int len=(int)Math.min(flushBytes,bufLen);
                cache.readFully(buf,0,len);
                stream.write(buf,0,len);
                flushBytes-=len;
            }
            stream.flush();
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
        maxStreamPos=cache.length();
        seek(maxStreamPos);
        flushBefore(maxStreamPos);
        super.close();
        cache.close();
        cache=null;
        cacheFile.delete();
        cacheFile=null;
        stream.flush();
        stream=null;
        StreamCloser.removeFromQueue(closeAction);
    }

    public void write(int b) throws IOException{
        flushBits(); // this will call checkClosed() for us
        cache.write(b);
        ++streamPos;
        maxStreamPos=Math.max(maxStreamPos,streamPos);
    }

    public void write(byte[] b,int off,int len) throws IOException{
        flushBits(); // this will call checkClosed() for us
        cache.write(b,off,len);
        streamPos+=len;
        maxStreamPos=Math.max(maxStreamPos,streamPos);
    }
}
