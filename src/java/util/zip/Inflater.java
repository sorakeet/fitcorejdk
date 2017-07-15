/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.zip;

public class Inflater{
    private static final byte[] defaultBuf=new byte[0];

    static{
        /** Zip library is loaded from System.initializeSystemClass */
        initIDs();
    }

    private final ZStreamRef zsRef;
    private byte[] buf=defaultBuf;
    private int off, len;
    private boolean finished;
    private boolean needDict;
    private long bytesRead;
    private long bytesWritten;

    public Inflater(){
        this(false);
    }

    public Inflater(boolean nowrap){
        zsRef=new ZStreamRef(init(nowrap));
    }

    private native static long init(boolean nowrap);

    private native static void initIDs();

    public void setInput(byte[] b){
        setInput(b,0,b.length);
    }

    public void setInput(byte[] b,int off,int len){
        if(b==null){
            throw new NullPointerException();
        }
        if(off<0||len<0||off>b.length-len){
            throw new ArrayIndexOutOfBoundsException();
        }
        synchronized(zsRef){
            this.buf=b;
            this.off=off;
            this.len=len;
        }
    }

    public void setDictionary(byte[] b){
        setDictionary(b,0,b.length);
    }

    public void setDictionary(byte[] b,int off,int len){
        if(b==null){
            throw new NullPointerException();
        }
        if(off<0||len<0||off>b.length-len){
            throw new ArrayIndexOutOfBoundsException();
        }
        synchronized(zsRef){
            ensureOpen();
            setDictionary(zsRef.address(),b,off,len);
            needDict=false;
        }
    }

    private void ensureOpen(){
        assert Thread.holdsLock(zsRef);
        if(zsRef.address()==0)
            throw new NullPointerException("Inflater has been closed");
    }

    private native static void setDictionary(long addr,byte[] b,int off,
                                             int len);

    public int getRemaining(){
        synchronized(zsRef){
            return len;
        }
    }

    public boolean needsInput(){
        synchronized(zsRef){
            return len<=0;
        }
    }

    public boolean needsDictionary(){
        synchronized(zsRef){
            return needDict;
        }
    }

    public boolean finished(){
        synchronized(zsRef){
            return finished;
        }
    }

    public int inflate(byte[] b) throws DataFormatException{
        return inflate(b,0,b.length);
    }

    public int inflate(byte[] b,int off,int len)
            throws DataFormatException{
        if(b==null){
            throw new NullPointerException();
        }
        if(off<0||len<0||off>b.length-len){
            throw new ArrayIndexOutOfBoundsException();
        }
        synchronized(zsRef){
            ensureOpen();
            int thisLen=this.len;
            int n=inflateBytes(zsRef.address(),b,off,len);
            bytesWritten+=n;
            bytesRead+=(thisLen-this.len);
            return n;
        }
    }

    private native int inflateBytes(long addr,byte[] b,int off,int len)
            throws DataFormatException;

    public int getAdler(){
        synchronized(zsRef){
            ensureOpen();
            return getAdler(zsRef.address());
        }
    }

    private native static int getAdler(long addr);

    public int getTotalIn(){
        return (int)getBytesRead();
    }

    public long getBytesRead(){
        synchronized(zsRef){
            ensureOpen();
            return bytesRead;
        }
    }

    public int getTotalOut(){
        return (int)getBytesWritten();
    }

    public long getBytesWritten(){
        synchronized(zsRef){
            ensureOpen();
            return bytesWritten;
        }
    }

    public void reset(){
        synchronized(zsRef){
            ensureOpen();
            reset(zsRef.address());
            buf=defaultBuf;
            finished=false;
            needDict=false;
            off=len=0;
            bytesRead=bytesWritten=0;
        }
    }

    private native static void reset(long addr);

    protected void finalize(){
        end();
    }

    public void end(){
        synchronized(zsRef){
            long addr=zsRef.address();
            zsRef.clear();
            if(addr!=0){
                end(addr);
                buf=null;
            }
        }
    }

    private native static void end(long addr);

    boolean ended(){
        synchronized(zsRef){
            return zsRef.address()==0;
        }
    }
}
