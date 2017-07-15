/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.zip;

import java.io.*;

public class GZIPInputStream extends InflaterInputStream{
    public final static int GZIP_MAGIC=0x8b1f;
    private final static int FTEXT=1;    // Extra text
    private final static int FHCRC=2;    // Header CRC
    private final static int FEXTRA=4;    // Extra field
    private final static int FNAME=8;    // File name
    private final static int FCOMMENT=16;   // File comment
    protected CRC32 crc=new CRC32();
    protected boolean eos;
    private boolean closed=false;
    private byte[] tmpbuf=new byte[128];

    public GZIPInputStream(InputStream in) throws IOException{
        this(in,512);
    }

    public GZIPInputStream(InputStream in,int size) throws IOException{
        super(in,new Inflater(true),size);
        usesDefaultInflater=true;
        readHeader(in);
    }

    private int readHeader(InputStream this_in) throws IOException{
        CheckedInputStream in=new CheckedInputStream(this_in,crc);
        crc.reset();
        // Check header magic
        if(readUShort(in)!=GZIP_MAGIC){
            throw new ZipException("Not in GZIP format");
        }
        // Check compression method
        if(readUByte(in)!=8){
            throw new ZipException("Unsupported compression method");
        }
        // Read flags
        int flg=readUByte(in);
        // Skip MTIME, XFL, and OS fields
        skipBytes(in,6);
        int n=2+2+6;
        // Skip optional extra field
        if((flg&FEXTRA)==FEXTRA){
            int m=readUShort(in);
            skipBytes(in,m);
            n+=m+2;
        }
        // Skip optional file name
        if((flg&FNAME)==FNAME){
            do{
                n++;
            }while(readUByte(in)!=0);
        }
        // Skip optional file comment
        if((flg&FCOMMENT)==FCOMMENT){
            do{
                n++;
            }while(readUByte(in)!=0);
        }
        // Check optional header CRC
        if((flg&FHCRC)==FHCRC){
            int v=(int)crc.getValue()&0xffff;
            if(readUShort(in)!=v){
                throw new ZipException("Corrupt GZIP header");
            }
            n+=2;
        }
        crc.reset();
        return n;
    }

    private int readUShort(InputStream in) throws IOException{
        int b=readUByte(in);
        return (readUByte(in)<<8)|b;
    }

    private int readUByte(InputStream in) throws IOException{
        int b=in.read();
        if(b==-1){
            throw new EOFException();
        }
        if(b<-1||b>255){
            // Report on this.in, not argument in; see read{Header, Trailer}.
            throw new IOException(this.in.getClass().getName()
                    +".read() returned value out of range -1..255: "+b);
        }
        return b;
    }

    private void skipBytes(InputStream in,int n) throws IOException{
        while(n>0){
            int len=in.read(tmpbuf,0,n<tmpbuf.length?n:tmpbuf.length);
            if(len==-1){
                throw new EOFException();
            }
            n-=len;
        }
    }

    private void ensureOpen() throws IOException{
        if(closed){
            throw new IOException("Stream closed");
        }
    }

    public int read(byte[] buf,int off,int len) throws IOException{
        ensureOpen();
        if(eos){
            return -1;
        }
        int n=super.read(buf,off,len);
        if(n==-1){
            if(readTrailer())
                eos=true;
            else
                return this.read(buf,off,len);
        }else{
            crc.update(buf,off,n);
        }
        return n;
    }

    public void close() throws IOException{
        if(!closed){
            super.close();
            eos=true;
            closed=true;
        }
    }

    private boolean readTrailer() throws IOException{
        InputStream in=this.in;
        int n=inf.getRemaining();
        if(n>0){
            in=new SequenceInputStream(
                    new ByteArrayInputStream(buf,len-n,n),
                    new FilterInputStream(in){
                        public void close() throws IOException{
                        }
                    });
        }
        // Uses left-to-right evaluation order
        if((readUInt(in)!=crc.getValue())||
                // rfc1952; ISIZE is the input size modulo 2^32
                (readUInt(in)!=(inf.getBytesWritten()&0xffffffffL)))
            throw new ZipException("Corrupt GZIP trailer");
        // If there are more bytes available in "in" or
        // the leftover in the "inf" is > 26 bytes:
        // this.trailer(8) + next.header.min(10) + next.trailer(8)
        // try concatenated case
        if(this.in.available()>0||n>26){
            int m=8;                  // this.trailer
            try{
                m+=readHeader(in);    // next.header
            }catch(IOException ze){
                return true;  // ignore any malformed, do nothing
            }
            inf.reset();
            if(n>m)
                inf.setInput(buf,len-n+m,n-m);
            return false;
        }
        return true;
    }

    private long readUInt(InputStream in) throws IOException{
        long s=readUShort(in);
        return ((long)readUShort(in)<<16)|s;
    }
}
