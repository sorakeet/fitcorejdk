/**
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

public class PushbackInputStream extends FilterInputStream{
    protected byte[] buf;
    protected int pos;

    public PushbackInputStream(InputStream in){
        this(in,1);
    }

    public PushbackInputStream(InputStream in,int size){
        super(in);
        if(size<=0){
            throw new IllegalArgumentException("size <= 0");
        }
        this.buf=new byte[size];
        this.pos=size;
    }

    public int read() throws IOException{
        ensureOpen();
        if(pos<buf.length){
            return buf[pos++]&0xff;
        }
        return super.read();
    }

    private void ensureOpen() throws IOException{
        if(in==null)
            throw new IOException("Stream closed");
    }

    public int read(byte[] b,int off,int len) throws IOException{
        ensureOpen();
        if(b==null){
            throw new NullPointerException();
        }else if(off<0||len<0||len>b.length-off){
            throw new IndexOutOfBoundsException();
        }else if(len==0){
            return 0;
        }
        int avail=buf.length-pos;
        if(avail>0){
            if(len<avail){
                avail=len;
            }
            System.arraycopy(buf,pos,b,off,avail);
            pos+=avail;
            off+=avail;
            len-=avail;
        }
        if(len>0){
            len=super.read(b,off,len);
            if(len==-1){
                return avail==0?-1:avail;
            }
            return avail+len;
        }
        return avail;
    }

    public long skip(long n) throws IOException{
        ensureOpen();
        if(n<=0){
            return 0;
        }
        long pskip=buf.length-pos;
        if(pskip>0){
            if(n<pskip){
                pskip=n;
            }
            pos+=pskip;
            n-=pskip;
        }
        if(n>0){
            pskip+=super.skip(n);
        }
        return pskip;
    }

    public int available() throws IOException{
        ensureOpen();
        int n=buf.length-pos;
        int avail=super.available();
        return n>(Integer.MAX_VALUE-avail)
                ?Integer.MAX_VALUE
                :n+avail;
    }

    public synchronized void close() throws IOException{
        if(in==null)
            return;
        in.close();
        in=null;
        buf=null;
    }

    public synchronized void mark(int readlimit){
    }

    public synchronized void reset() throws IOException{
        throw new IOException("mark/reset not supported");
    }

    public boolean markSupported(){
        return false;
    }

    public void unread(int b) throws IOException{
        ensureOpen();
        if(pos==0){
            throw new IOException("Push back buffer is full");
        }
        buf[--pos]=(byte)b;
    }

    public void unread(byte[] b) throws IOException{
        unread(b,0,b.length);
    }

    public void unread(byte[] b,int off,int len) throws IOException{
        ensureOpen();
        if(len>pos){
            throw new IOException("Push back buffer is full");
        }
        pos-=len;
        System.arraycopy(b,off,buf,pos,len);
    }
}
