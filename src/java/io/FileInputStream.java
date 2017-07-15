/**
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

import sun.nio.ch.FileChannelImpl;

import java.nio.channels.FileChannel;

public class FileInputStream extends InputStream{
    static{
        initIDs();
    }

    private final FileDescriptor fd;
    private final String path;
    private final Object closeLock=new Object();
    private FileChannel channel=null;
    private volatile boolean closed=false;

    public FileInputStream(String name) throws FileNotFoundException{
        this(name!=null?new File(name):null);
    }

    public FileInputStream(File file) throws FileNotFoundException{
        String name=(file!=null?file.getPath():null);
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            security.checkRead(name);
        }
        if(name==null){
            throw new NullPointerException();
        }
        if(file.isInvalid()){
            throw new FileNotFoundException("Invalid file path");
        }
        fd=new FileDescriptor();
        fd.attach(this);
        path=name;
        open(name);
    }

    // wrap native call to allow instrumentation
    private void open(String name) throws FileNotFoundException{
        open0(name);
    }

    private native void open0(String name) throws FileNotFoundException;

    public FileInputStream(FileDescriptor fdObj){
        SecurityManager security=System.getSecurityManager();
        if(fdObj==null){
            throw new NullPointerException();
        }
        if(security!=null){
            security.checkRead(fdObj);
        }
        fd=fdObj;
        path=null;
        /**
         * FileDescriptor is being shared by streams.
         * Register this stream with FileDescriptor tracker.
         */
        fd.attach(this);
    }

    private static native void initIDs();

    public int read() throws IOException{
        return read0();
    }

    private native int read0() throws IOException;

    public int read(byte b[]) throws IOException{
        return readBytes(b,0,b.length);
    }

    private native int readBytes(byte b[],int off,int len) throws IOException;

    public int read(byte b[],int off,int len) throws IOException{
        return readBytes(b,off,len);
    }

    public native long skip(long n) throws IOException;

    public native int available() throws IOException;

    public void close() throws IOException{
        synchronized(closeLock){
            if(closed){
                return;
            }
            closed=true;
        }
        if(channel!=null){
            channel.close();
        }
        fd.closeAll(new Closeable(){
            public void close() throws IOException{
                close0();
            }
        });
    }

    private native void close0() throws IOException;

    public final FileDescriptor getFD() throws IOException{
        if(fd!=null){
            return fd;
        }
        throw new IOException();
    }

    public FileChannel getChannel(){
        synchronized(this){
            if(channel==null){
                channel=FileChannelImpl.open(fd,path,true,false,this);
            }
            return channel;
        }
    }

    protected void finalize() throws IOException{
        if((fd!=null)&&(fd!=FileDescriptor.in)){
            /** if fd is shared, the references in FileDescriptor
             * will ensure that finalizer is only called when
             * safe to do so. All references using the fd have
             * become unreachable. We can call close()
             */
            close();
        }
    }
}
