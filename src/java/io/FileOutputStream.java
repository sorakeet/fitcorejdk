/**
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

import sun.nio.ch.FileChannelImpl;

import java.nio.channels.FileChannel;

public class FileOutputStream extends OutputStream{
    static{
        initIDs();
    }

    private final FileDescriptor fd;
    private final boolean append;
    private final String path;
    private final Object closeLock=new Object();
    private FileChannel channel;
    private volatile boolean closed=false;

    public FileOutputStream(String name) throws FileNotFoundException{
        this(name!=null?new File(name):null,false);
    }

    public FileOutputStream(File file,boolean append)
            throws FileNotFoundException{
        String name=(file!=null?file.getPath():null);
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            security.checkWrite(name);
        }
        if(name==null){
            throw new NullPointerException();
        }
        if(file.isInvalid()){
            throw new FileNotFoundException("Invalid file path");
        }
        this.fd=new FileDescriptor();
        fd.attach(this);
        this.append=append;
        this.path=name;
        open(name,append);
    }

    // wrap native call to allow instrumentation
    private void open(String name,boolean append)
            throws FileNotFoundException{
        open0(name,append);
    }

    private native void open0(String name,boolean append)
            throws FileNotFoundException;

    public FileOutputStream(String name,boolean append)
            throws FileNotFoundException{
        this(name!=null?new File(name):null,append);
    }

    public FileOutputStream(File file) throws FileNotFoundException{
        this(file,false);
    }

    public FileOutputStream(FileDescriptor fdObj){
        SecurityManager security=System.getSecurityManager();
        if(fdObj==null){
            throw new NullPointerException();
        }
        if(security!=null){
            security.checkWrite(fdObj);
        }
        this.fd=fdObj;
        this.append=false;
        this.path=null;
        fd.attach(this);
    }

    private static native void initIDs();

    public void write(int b) throws IOException{
        write(b,append);
    }

    private native void write(int b,boolean append) throws IOException;

    public void write(byte b[]) throws IOException{
        writeBytes(b,0,b.length,append);
    }

    private native void writeBytes(byte b[],int off,int len,boolean append)
            throws IOException;

    public void write(byte b[],int off,int len) throws IOException{
        writeBytes(b,off,len,append);
    }

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
                channel=FileChannelImpl.open(fd,path,false,true,append,this);
            }
            return channel;
        }
    }

    protected void finalize() throws IOException{
        if(fd!=null){
            if(fd==FileDescriptor.out||fd==FileDescriptor.err){
                flush();
            }else{
                /** if fd is shared, the references in FileDescriptor
                 * will ensure that finalizer is only called when
                 * safe to do so. All references using the fd have
                 * become unreachable. We can call close()
                 */
                close();
            }
        }
    }
}
