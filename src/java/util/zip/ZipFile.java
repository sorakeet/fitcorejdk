/**
 * Copyright (c) 1995, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.zip;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.zip.ZipConstants64.EFS;

public class ZipFile implements ZipConstants, Closeable{
    public static final int OPEN_READ=0x1;
    public static final int OPEN_DELETE=0x4;
    private static final int STORED=ZipEntry.STORED;
    private static final int DEFLATED=ZipEntry.DEFLATED;
    private static final boolean usemmap;
    private static final int JZENTRY_NAME=0;
    private static final int JZENTRY_EXTRA=1;
    private static final int JZENTRY_COMMENT=2;

    static{
        /** Zip library is loaded from System.initializeSystemClass */
        initIDs();
    }

    static{
        // A system prpperty to disable mmap use to avoid vm crash when
        // in-use zip file is accidently overwritten by others.
        String prop=sun.misc.VM.getSavedProperty("sun.zip.disableMemoryMapping");
        usemmap=(prop==null||
                !(prop.length()==0||prop.equalsIgnoreCase("true")));
    }

    static{
        sun.misc.SharedSecrets.setJavaUtilZipFileAccess(
                new sun.misc.JavaUtilZipFileAccess(){
                    public boolean startsWithLocHeader(ZipFile zip){
                        return zip.startsWithLocHeader();
                    }
                }
        );
    }

    private final String name;     // zip file name
    private final int total;       // total number of entries
    private final boolean locsig;  // if zip file starts with LOCSIG (usually true)
    // the outstanding inputstreams that need to be closed,
    // mapped to the inflater objects they use.
    private final Map<InputStream,Inflater> streams=new WeakHashMap<>();
    private long jzfile;  // address of jzfile data
    private volatile boolean closeRequested=false;
    private ZipCoder zc;
    // List of available Inflater objects for decompression
    private Deque<Inflater> inflaterCache=new ArrayDeque<>();

    public ZipFile(String name) throws IOException{
        this(new File(name),OPEN_READ);
    }

    public ZipFile(File file,int mode) throws IOException{
        this(file,mode,StandardCharsets.UTF_8);
    }

    public ZipFile(File file,int mode,Charset charset) throws IOException{
        if(((mode&OPEN_READ)==0)||
                ((mode&~(OPEN_READ|OPEN_DELETE))!=0)){
            throw new IllegalArgumentException("Illegal mode: 0x"+
                    Integer.toHexString(mode));
        }
        String name=file.getPath();
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkRead(name);
            if((mode&OPEN_DELETE)!=0){
                sm.checkDelete(name);
            }
        }
        if(charset==null)
            throw new NullPointerException("charset is null");
        this.zc=ZipCoder.get(charset);
        long t0=System.nanoTime();
        jzfile=open(name,mode,file.lastModified(),usemmap);
        sun.misc.PerfCounter.getZipFileOpenTime().addElapsedTimeFrom(t0);
        sun.misc.PerfCounter.getZipFileCount().increment();
        this.name=name;
        this.total=getTotal(jzfile);
        this.locsig=startsWithLOC(jzfile);
    }

    private static native long open(String name,int mode,long lastModified,
                                    boolean usemmap) throws IOException;

    private static native int getTotal(long jzfile);

    private static native boolean startsWithLOC(long jzfile);

    public ZipFile(File file) throws ZipException, IOException{
        this(file,OPEN_READ);
    }

    public ZipFile(String name,Charset charset) throws IOException{
        this(new File(name),OPEN_READ,charset);
    }

    public ZipFile(File file,Charset charset) throws IOException{
        this(file,OPEN_READ,charset);
    }

    private static native void initIDs();

    private static native long getNextEntry(long jzfile,int i);

    private static native int read(long jzfile,long jzentry,
                                   long pos,byte[] b,int off,int len);

    private static native String getZipMessage(long jzfile);

    public String getComment(){
        synchronized(this){
            ensureOpen();
            byte[] bcomm=getCommentBytes(jzfile);
            if(bcomm==null)
                return null;
            return zc.toString(bcomm,bcomm.length);
        }
    }

    private void ensureOpen(){
        if(closeRequested){
            throw new IllegalStateException("zip file closed");
        }
        if(jzfile==0){
            throw new IllegalStateException("The object is not initialized.");
        }
    }

    private static native byte[] getCommentBytes(long jzfile);

    public ZipEntry getEntry(String name){
        if(name==null){
            throw new NullPointerException("name");
        }
        long jzentry=0;
        synchronized(this){
            ensureOpen();
            jzentry=getEntry(jzfile,zc.getBytes(name),true);
            if(jzentry!=0){
                ZipEntry ze=getZipEntry(name,jzentry);
                freeEntry(jzfile,jzentry);
                return ze;
            }
        }
        return null;
    }

    private static native long getEntry(long jzfile,byte[] name,
                                        boolean addSlash);

    // freeEntry releases the C jzentry struct.
    private static native void freeEntry(long jzfile,long jzentry);

    private ZipEntry getZipEntry(String name,long jzentry){
        ZipEntry e=new ZipEntry();
        e.flag=getEntryFlag(jzentry);  // get the flag first
        if(name!=null){
            e.name=name;
        }else{
            byte[] bname=getEntryBytes(jzentry,JZENTRY_NAME);
            if(!zc.isUTF8()&&(e.flag&EFS)!=0){
                e.name=zc.toStringUTF8(bname,bname.length);
            }else{
                e.name=zc.toString(bname,bname.length);
            }
        }
        e.xdostime=getEntryTime(jzentry);
        e.crc=getEntryCrc(jzentry);
        e.size=getEntrySize(jzentry);
        e.csize=getEntryCSize(jzentry);
        e.method=getEntryMethod(jzentry);
        e.setExtra0(getEntryBytes(jzentry,JZENTRY_EXTRA),false);
        byte[] bcomm=getEntryBytes(jzentry,JZENTRY_COMMENT);
        if(bcomm==null){
            e.comment=null;
        }else{
            if(!zc.isUTF8()&&(e.flag&EFS)!=0){
                e.comment=zc.toStringUTF8(bcomm,bcomm.length);
            }else{
                e.comment=zc.toString(bcomm,bcomm.length);
            }
        }
        return e;
    }

    // access to the native zentry object
    private static native long getEntryTime(long jzentry);

    private static native long getEntryCrc(long jzentry);

    private static native long getEntryCSize(long jzentry);

    private static native long getEntrySize(long jzentry);

    private static native int getEntryMethod(long jzentry);

    private static native int getEntryFlag(long jzentry);

    private static native byte[] getEntryBytes(long jzentry,int type);

    protected void finalize() throws IOException{
        close();
    }

    public void close() throws IOException{
        if(closeRequested)
            return;
        closeRequested=true;
        synchronized(this){
            // Close streams, release their inflaters
            synchronized(streams){
                if(false==streams.isEmpty()){
                    Map<InputStream,Inflater> copy=new HashMap<>(streams);
                    streams.clear();
                    for(Map.Entry<InputStream,Inflater> e : copy.entrySet()){
                        e.getKey().close();
                        Inflater inf=e.getValue();
                        if(inf!=null){
                            inf.end();
                        }
                    }
                }
            }
            // Release cached inflaters
            Inflater inf;
            synchronized(inflaterCache){
                while(null!=(inf=inflaterCache.poll())){
                    inf.end();
                }
            }
            if(jzfile!=0){
                // Close the zip file
                long zf=this.jzfile;
                jzfile=0;
                close(zf);
            }
        }
    }

    private static native void close(long jzfile);

    public InputStream getInputStream(ZipEntry entry) throws IOException{
        if(entry==null){
            throw new NullPointerException("entry");
        }
        long jzentry=0;
        ZipFileInputStream in=null;
        synchronized(this){
            ensureOpen();
            if(!zc.isUTF8()&&(entry.flag&EFS)!=0){
                jzentry=getEntry(jzfile,zc.getBytesUTF8(entry.name),false);
            }else{
                jzentry=getEntry(jzfile,zc.getBytes(entry.name),false);
            }
            if(jzentry==0){
                return null;
            }
            in=new ZipFileInputStream(jzentry);
            switch(getEntryMethod(jzentry)){
                case STORED:
                    synchronized(streams){
                        streams.put(in,null);
                    }
                    return in;
                case DEFLATED:
                    // MORE: Compute good size for inflater stream:
                    long size=getEntrySize(jzentry)+2; // Inflater likes a bit of slack
                    if(size>65536) size=8192;
                    if(size<=0) size=4096;
                    Inflater inf=getInflater();
                    InputStream is=
                            new ZipFileInflaterInputStream(in,inf,(int)size);
                    synchronized(streams){
                        streams.put(is,inf);
                    }
                    return is;
                default:
                    throw new ZipException("invalid compression method");
            }
        }
    }

    private Inflater getInflater(){
        Inflater inf;
        synchronized(inflaterCache){
            while(null!=(inf=inflaterCache.poll())){
                if(false==inf.ended()){
                    return inf;
                }
            }
        }
        return new Inflater(true);
    }

    private void releaseInflater(Inflater inf){
        if(false==inf.ended()){
            inf.reset();
            synchronized(inflaterCache){
                inflaterCache.add(inf);
            }
        }
    }

    public String getName(){
        return name;
    }

    public Enumeration<? extends ZipEntry> entries(){
        return new ZipEntryIterator();
    }

    public Stream<? extends ZipEntry> stream(){
        return StreamSupport.stream(Spliterators.spliterator(
                new ZipEntryIterator(),size(),
                Spliterator.ORDERED|Spliterator.DISTINCT|
                        Spliterator.IMMUTABLE|Spliterator.NONNULL),false);
    }

    public int size(){
        ensureOpen();
        return total;
    }

    private void ensureOpenOrZipException() throws IOException{
        if(closeRequested){
            throw new ZipException("ZipFile closed");
        }
    }

    private boolean startsWithLocHeader(){
        return locsig;
    }

    private class ZipFileInflaterInputStream extends InflaterInputStream{
        private final ZipFileInputStream zfin;
        private volatile boolean closeRequested=false;
        private boolean eof=false;

        ZipFileInflaterInputStream(ZipFileInputStream zfin,Inflater inf,
                                   int size){
            super(zfin,inf,size);
            this.zfin=zfin;
        }

        public int available() throws IOException{
            if(closeRequested)
                return 0;
            long avail=zfin.size()-inf.getBytesWritten();
            return (avail>(long)Integer.MAX_VALUE?
                    Integer.MAX_VALUE:(int)avail);
        }

        public void close() throws IOException{
            if(closeRequested)
                return;
            closeRequested=true;
            super.close();
            Inflater inf;
            synchronized(streams){
                inf=streams.remove(this);
            }
            if(inf!=null){
                releaseInflater(inf);
            }
        }

        // Override fill() method to provide an extra "dummy" byte
        // at the end of the input stream. This is required when
        // using the "nowrap" Inflater option.
        protected void fill() throws IOException{
            if(eof){
                throw new EOFException("Unexpected end of ZLIB input stream");
            }
            len=in.read(buf,0,buf.length);
            if(len==-1){
                buf[0]=0;
                len=1;
                eof=true;
            }
            inf.setInput(buf,0,len);
        }

        protected void finalize() throws Throwable{
            close();
        }
    }

    private class ZipEntryIterator implements Enumeration<ZipEntry>, Iterator<ZipEntry>{
        private int i=0;

        public ZipEntryIterator(){
            ensureOpen();
        }

        public boolean hasMoreElements(){
            return hasNext();
        }

        public boolean hasNext(){
            synchronized(ZipFile.this){
                ensureOpen();
                return i<total;
            }
        }

        public ZipEntry next(){
            synchronized(ZipFile.this){
                ensureOpen();
                if(i>=total){
                    throw new NoSuchElementException();
                }
                long jzentry=getNextEntry(jzfile,i++);
                if(jzentry==0){
                    String message;
                    if(closeRequested){
                        message="ZipFile concurrently closed";
                    }else{
                        message=getZipMessage(ZipFile.this.jzfile);
                    }
                    throw new ZipError("jzentry == 0"+
                            ",\n jzfile = "+ZipFile.this.jzfile+
                            ",\n total = "+ZipFile.this.total+
                            ",\n name = "+ZipFile.this.name+
                            ",\n i = "+i+
                            ",\n message = "+message
                    );
                }
                ZipEntry ze=getZipEntry(null,jzentry);
                freeEntry(jzfile,jzentry);
                return ze;
            }
        }

        public ZipEntry nextElement(){
            return next();
        }
    }

    private class ZipFileInputStream extends InputStream{
        protected long jzentry; // address of jzentry data
        protected long rem;     // number of remaining bytes within entry
        protected long size;    // uncompressed size of this entry
        private volatile boolean zfisCloseRequested=false;
        private long pos;     // current position within entry data

        ZipFileInputStream(long jzentry){
            pos=0;
            rem=getEntryCSize(jzentry);
            size=getEntrySize(jzentry);
            this.jzentry=jzentry;
        }

        public int read() throws IOException{
            byte[] b=new byte[1];
            if(read(b,0,1)==1){
                return b[0]&0xff;
            }else{
                return -1;
            }
        }

        public int read(byte b[],int off,int len) throws IOException{
            synchronized(ZipFile.this){
                long rem=this.rem;
                long pos=this.pos;
                if(rem==0){
                    return -1;
                }
                if(len<=0){
                    return 0;
                }
                if(len>rem){
                    len=(int)rem;
                }
                // Check if ZipFile open
                ensureOpenOrZipException();
                len=ZipFile.read(ZipFile.this.jzfile,jzentry,pos,b,
                        off,len);
                if(len>0){
                    this.pos=(pos+len);
                    this.rem=(rem-len);
                }
            }
            if(rem==0){
                close();
            }
            return len;
        }

        public long skip(long n){
            if(n>rem)
                n=rem;
            pos+=n;
            rem-=n;
            if(rem==0){
                close();
            }
            return n;
        }

        public int available(){
            return rem>Integer.MAX_VALUE?Integer.MAX_VALUE:(int)rem;
        }

        public void close(){
            if(zfisCloseRequested)
                return;
            zfisCloseRequested=true;
            rem=0;
            synchronized(ZipFile.this){
                if(jzentry!=0&&ZipFile.this.jzfile!=0){
                    freeEntry(ZipFile.this.jzfile,jzentry);
                    jzentry=0;
                }
            }
            synchronized(streams){
                streams.remove(this);
            }
        }

        public long size(){
            return size;
        }

        protected void finalize(){
            close();
        }
    }
}