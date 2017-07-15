/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

class MemoryCache{
    private static final int BUFFER_LENGTH=8192;
    private ArrayList cache=new ArrayList();
    private long cacheStart=0L;
    private long length=0L;

    public long loadFromStream(InputStream stream,long pos)
            throws IOException{
        // We've already got enough data cached
        if(pos<length){
            return pos;
        }
        int offset=(int)(length%BUFFER_LENGTH);
        byte[] buf=null;
        long len=pos-length;
        if(offset!=0){
            buf=getCacheBlock(length/BUFFER_LENGTH);
        }
        while(len>0){
            if(buf==null){
                try{
                    buf=new byte[BUFFER_LENGTH];
                }catch(OutOfMemoryError e){
                    throw new IOException("No memory left for cache!");
                }
                offset=0;
            }
            int left=BUFFER_LENGTH-offset;
            int nbytes=(int)Math.min(len,(long)left);
            nbytes=stream.read(buf,offset,nbytes);
            if(nbytes==-1){
                return length; // EOF
            }
            if(offset==0){
                cache.add(buf);
            }
            len-=nbytes;
            length+=nbytes;
            offset+=nbytes;
            if(offset>=BUFFER_LENGTH){
                // we've filled the current buffer, so a new one will be
                // allocated next time around (and offset will be reset to 0)
                buf=null;
            }
        }
        return pos;
    }

    private byte[] getCacheBlock(long blockNum) throws IOException{
        long blockOffset=blockNum-cacheStart;
        if(blockOffset>Integer.MAX_VALUE){
            // This can only happen when the cache hits 16 terabytes of
            // contiguous data...
            throw new IOException("Cache addressing limit exceeded!");
        }
        return (byte[])cache.get((int)blockOffset);
    }

    public void writeToStream(OutputStream stream,long pos,long len)
            throws IOException{
        if(pos+len>length){
            throw new IndexOutOfBoundsException("Argument out of cache");
        }
        if((pos<0)||(len<0)){
            throw new IndexOutOfBoundsException("Negative pos or len");
        }
        if(len==0){
            return;
        }
        long bufIndex=pos/BUFFER_LENGTH;
        if(bufIndex<cacheStart){
            throw new IndexOutOfBoundsException("pos already disposed");
        }
        int offset=(int)(pos%BUFFER_LENGTH);
        byte[] buf=getCacheBlock(bufIndex++);
        while(len>0){
            if(buf==null){
                buf=getCacheBlock(bufIndex++);
                offset=0;
            }
            int nbytes=(int)Math.min(len,(long)(BUFFER_LENGTH-offset));
            stream.write(buf,offset,nbytes);
            buf=null;
            len-=nbytes;
        }
    }

    public void write(byte[] b,int off,int len,long pos)
            throws IOException{
        if(b==null){
            throw new NullPointerException("b == null!");
        }
        // Fix 4430357 - if off + len < 0, overflow occurred
        if((off<0)||(len<0)||(pos<0)||
                (off+len>b.length)||(off+len<0)){
            throw new IndexOutOfBoundsException();
        }
        // Ensure there is space for the incoming data
        long lastPos=pos+len-1;
        if(lastPos>=length){
            pad(lastPos);
            length=lastPos+1;
        }
        // Copy the data into the cache, block by block
        int offset=(int)(pos%BUFFER_LENGTH);
        while(len>0){
            byte[] buf=getCacheBlock(pos/BUFFER_LENGTH);
            int nbytes=Math.min(len,BUFFER_LENGTH-offset);
            System.arraycopy(b,off,buf,offset,nbytes);
            pos+=nbytes;
            off+=nbytes;
            len-=nbytes;
            offset=0; // Always after the first time
        }
    }

    private void pad(long pos) throws IOException{
        long currIndex=cacheStart+cache.size()-1;
        long lastIndex=pos/BUFFER_LENGTH;
        long numNewBuffers=lastIndex-currIndex;
        for(long i=0;i<numNewBuffers;i++){
            try{
                cache.add(new byte[BUFFER_LENGTH]);
            }catch(OutOfMemoryError e){
                throw new IOException("No memory left for cache!");
            }
        }
    }

    public void write(int b,long pos) throws IOException{
        if(pos<0){
            throw new ArrayIndexOutOfBoundsException("pos < 0");
        }
        // Ensure there is space for the incoming data
        if(pos>=length){
            pad(pos);
            length=pos+1;
        }
        // Insert the data.
        byte[] buf=getCacheBlock(pos/BUFFER_LENGTH);
        int offset=(int)(pos%BUFFER_LENGTH);
        buf[offset]=(byte)b;
    }

    public long getLength(){
        return length;
    }

    public int read(long pos) throws IOException{
        if(pos>=length){
            return -1;
        }
        byte[] buf=getCacheBlock(pos/BUFFER_LENGTH);
        if(buf==null){
            return -1;
        }
        return buf[(int)(pos%BUFFER_LENGTH)]&0xff;
    }

    public void read(byte[] b,int off,int len,long pos)
            throws IOException{
        if(b==null){
            throw new NullPointerException("b == null!");
        }
        // Fix 4430357 - if off + len < 0, overflow occurred
        if((off<0)||(len<0)||(pos<0)||
                (off+len>b.length)||(off+len<0)){
            throw new IndexOutOfBoundsException();
        }
        if(pos+len>length){
            throw new IndexOutOfBoundsException();
        }
        long index=pos/BUFFER_LENGTH;
        int offset=(int)pos%BUFFER_LENGTH;
        while(len>0){
            int nbytes=Math.min(len,BUFFER_LENGTH-offset);
            byte[] buf=getCacheBlock(index++);
            System.arraycopy(buf,offset,b,off,nbytes);
            len-=nbytes;
            off+=nbytes;
            offset=0; // Always after the first time
        }
    }

    public void disposeBefore(long pos){
        long index=pos/BUFFER_LENGTH;
        if(index<cacheStart){
            throw new IndexOutOfBoundsException("pos already disposed");
        }
        long numBlocks=Math.min(index-cacheStart,cache.size());
        for(long i=0;i<numBlocks;i++){
            cache.remove(0);
        }
        this.cacheStart=index;
    }

    public void reset(){
        cache.clear();
        cacheStart=0;
        length=0L;
    }
}
