/**
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

import java.util.ArrayList;
import java.util.List;

public final class FileDescriptor{
    public static final FileDescriptor in=standardStream(0);
    public static final FileDescriptor out=standardStream(1);
    public static final FileDescriptor err=standardStream(2);

    static{
        initIDs();
    }

    // Set up JavaIOFileDescriptorAccess in SharedSecrets
    static{
        sun.misc.SharedSecrets.setJavaIOFileDescriptorAccess(
                new sun.misc.JavaIOFileDescriptorAccess(){
                    public void set(FileDescriptor obj,int fd){
                        obj.fd=fd;
                    }

                    public int get(FileDescriptor obj){
                        return obj.fd;
                    }

                    public void setHandle(FileDescriptor obj,long handle){
                        obj.handle=handle;
                    }

                    public long getHandle(FileDescriptor obj){
                        return obj.handle;
                    }
                }
        );
    }

    private int fd;
    private long handle;
    private Closeable parent;
    private List<Closeable> otherParents;
    private boolean closed;

    public /**/ FileDescriptor(){
        fd=-1;
        handle=-1;
    }

    private static native void initIDs();

    private static FileDescriptor standardStream(int fd){
        FileDescriptor desc=new FileDescriptor();
        desc.handle=set(fd);
        return desc;
    }

    private static native long set(int d);

    public boolean valid(){
        return ((handle!=-1)||(fd!=-1));
    }

    public native void sync() throws SyncFailedException;

    synchronized void attach(Closeable c){
        if(parent==null){
            // first caller gets to do this
            parent=c;
        }else if(otherParents==null){
            otherParents=new ArrayList<>();
            otherParents.add(parent);
            otherParents.add(c);
        }else{
            otherParents.add(c);
        }
    }

    @SuppressWarnings("try")
    synchronized void closeAll(Closeable releaser) throws IOException{
        if(!closed){
            closed=true;
            IOException ioe=null;
            try(Closeable c=releaser){
                if(otherParents!=null){
                    for(Closeable referent : otherParents){
                        try{
                            referent.close();
                        }catch(IOException x){
                            if(ioe==null){
                                ioe=x;
                            }else{
                                ioe.addSuppressed(x);
                            }
                        }
                    }
                }
            }catch(IOException ex){
                /**
                 * If releaser close() throws IOException
                 * add other exceptions as suppressed.
                 */
                if(ioe!=null)
                    ex.addSuppressed(ioe);
                ioe=ex;
            }finally{
                if(ioe!=null)
                    throw ioe;
            }
        }
    }
}
