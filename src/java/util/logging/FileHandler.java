/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.logging;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.StandardOpenOption.*;

public class FileHandler extends StreamHandler{
    private static final int MAX_LOCKS=100;
    private static final Set<String> locks=new HashSet<>();
    private MeteredStream meter;
    private boolean append;
    private int limit;       // zero => no limit.
    private int count;
    private String pattern;
    private String lockFileName;
    private FileChannel lockFileChannel;
    private File files[];

    public FileHandler() throws IOException, SecurityException{
        checkPermission();
        configure();
        openFiles();
    }

    private void configure(){
        LogManager manager=LogManager.getLogManager();
        String cname=getClass().getName();
        pattern=manager.getStringProperty(cname+".pattern","%h/java%u.log");
        limit=manager.getIntProperty(cname+".limit",0);
        if(limit<0){
            limit=0;
        }
        count=manager.getIntProperty(cname+".count",1);
        if(count<=0){
            count=1;
        }
        append=manager.getBooleanProperty(cname+".append",false);
        setLevel(manager.getLevelProperty(cname+".level",Level.ALL));
        setFilter(manager.getFilterProperty(cname+".filter",null));
        setFormatter(manager.getFormatterProperty(cname+".formatter",new XMLFormatter()));
        try{
            setEncoding(manager.getStringProperty(cname+".encoding",null));
        }catch(Exception ex){
            try{
                setEncoding(null);
            }catch(Exception ex2){
                // doing a setEncoding with null should always work.
                // assert false;
            }
        }
    }

    private void openFiles() throws IOException{
        LogManager manager=LogManager.getLogManager();
        manager.checkPermission();
        if(count<1){
            throw new IllegalArgumentException("file count = "+count);
        }
        if(limit<0){
            limit=0;
        }
        // We register our own ErrorManager during initialization
        // so we can record exceptions.
        InitializationErrorManager em=new InitializationErrorManager();
        setErrorManager(em);
        // Create a lock file.  This grants us exclusive access
        // to our set of output files, as long as we are alive.
        int unique=-1;
        for(;;){
            unique++;
            if(unique>MAX_LOCKS){
                throw new IOException("Couldn't get lock for "+pattern);
            }
            // Generate a lock file name from the "unique" int.
            lockFileName=generate(pattern,0,unique).toString()+".lck";
            // Now try to lock that filename.
            // Because some systems (e.g., Solaris) can only do file locks
            // between processes (and not within a process), we first check
            // if we ourself already have the file locked.
            synchronized(locks){
                if(locks.contains(lockFileName)){
                    // We already own this lock, for a different FileHandler
                    // object.  Try again.
                    continue;
                }
                final Path lockFilePath=Paths.get(lockFileName);
                FileChannel channel=null;
                int retries=-1;
                boolean fileCreated=false;
                while(channel==null&&retries++<1){
                    try{
                        channel=FileChannel.open(lockFilePath,
                                CREATE_NEW,WRITE);
                        fileCreated=true;
                    }catch(FileAlreadyExistsException ix){
                        // This may be a zombie file left over by a previous
                        // execution. Reuse it - but only if we can actually
                        // write to its directory.
                        // Note that this is a situation that may happen,
                        // but not too frequently.
                        if(Files.isRegularFile(lockFilePath,LinkOption.NOFOLLOW_LINKS)
                                &&isParentWritable(lockFilePath)){
                            try{
                                channel=FileChannel.open(lockFilePath,
                                        WRITE,APPEND);
                            }catch(NoSuchFileException x){
                                // Race condition - retry once, and if that
                                // fails again just try the next name in
                                // the sequence.
                                continue;
                            }catch(IOException x){
                                // the file may not be writable for us.
                                // try the next name in the sequence
                                break;
                            }
                        }else{
                            // at this point channel should still be null.
                            // break and try the next name in the sequence.
                            break;
                        }
                    }
                }
                if(channel==null) continue; // try the next name;
                lockFileChannel=channel;
                boolean available;
                try{
                    available=lockFileChannel.tryLock()!=null;
                    // We got the lock OK.
                    // At this point we could call File.deleteOnExit().
                    // However, this could have undesirable side effects
                    // as indicated by JDK-4872014. So we will instead
                    // rely on the fact that close() will remove the lock
                    // file and that whoever is creating FileHandlers should
                    // be responsible for closing them.
                }catch(IOException ix){
                    // We got an IOException while trying to get the lock.
                    // This normally indicates that locking is not supported
                    // on the target directory.  We have to proceed without
                    // getting a lock.   Drop through, but only if we did
                    // create the file...
                    available=fileCreated;
                }catch(OverlappingFileLockException x){
                    // someone already locked this file in this VM, through
                    // some other channel - that is - using something else
                    // than new FileHandler(...);
                    // continue searching for an available lock.
                    available=false;
                }
                if(available){
                    // We got the lock.  Remember it.
                    locks.add(lockFileName);
                    break;
                }
                // We failed to get the lock.  Try next file.
                lockFileChannel.close();
            }
        }
        files=new File[count];
        for(int i=0;i<count;i++){
            files[i]=generate(pattern,i,unique);
        }
        // Create the initial log file.
        if(append){
            open(files[0],true);
        }else{
            rotate();
        }
        // Did we detect any exceptions during initialization?
        Exception ex=em.lastException;
        if(ex!=null){
            if(ex instanceof IOException){
                throw (IOException)ex;
            }else if(ex instanceof SecurityException){
                throw (SecurityException)ex;
            }else{
                throw new IOException("Exception: "+ex);
            }
        }
        // Install the normal default ErrorManager.
        setErrorManager(new ErrorManager());
    }

    private void open(File fname,boolean append) throws IOException{
        int len=0;
        if(append){
            len=(int)fname.length();
        }
        FileOutputStream fout=new FileOutputStream(fname.toString(),append);
        BufferedOutputStream bout=new BufferedOutputStream(fout);
        meter=new MeteredStream(bout,len);
        setOutputStream(meter);
    }

    private boolean isParentWritable(Path path){
        Path parent=path.getParent();
        if(parent==null){
            parent=path.toAbsolutePath().getParent();
        }
        return parent!=null&&Files.isWritable(parent);
    }

    private File generate(String pattern,int generation,int unique)
            throws IOException{
        File file=null;
        String word="";
        int ix=0;
        boolean sawg=false;
        boolean sawu=false;
        while(ix<pattern.length()){
            char ch=pattern.charAt(ix);
            ix++;
            char ch2=0;
            if(ix<pattern.length()){
                ch2=Character.toLowerCase(pattern.charAt(ix));
            }
            if(ch=='/'){
                if(file==null){
                    file=new File(word);
                }else{
                    file=new File(file,word);
                }
                word="";
                continue;
            }else if(ch=='%'){
                if(ch2=='t'){
                    String tmpDir=System.getProperty("java.io.tmpdir");
                    if(tmpDir==null){
                        tmpDir=System.getProperty("user.home");
                    }
                    file=new File(tmpDir);
                    ix++;
                    word="";
                    continue;
                }else if(ch2=='h'){
                    file=new File(System.getProperty("user.home"));
                    if(isSetUID()){
                        // Ok, we are in a set UID program.  For safety's sake
                        // we disallow attempts to open files relative to %h.
                        throw new IOException("can't use %h in set UID program");
                    }
                    ix++;
                    word="";
                    continue;
                }else if(ch2=='g'){
                    word=word+generation;
                    sawg=true;
                    ix++;
                    continue;
                }else if(ch2=='u'){
                    word=word+unique;
                    sawu=true;
                    ix++;
                    continue;
                }else if(ch2=='%'){
                    word=word+"%";
                    ix++;
                    continue;
                }
            }
            word=word+ch;
        }
        if(count>1&&!sawg){
            word=word+"."+generation;
        }
        if(unique>0&&!sawu){
            word=word+"."+unique;
        }
        if(word.length()>0){
            if(file==null){
                file=new File(word);
            }else{
                file=new File(file,word);
            }
        }
        return file;
    }

    private static native boolean isSetUID();

    private synchronized void rotate(){
        Level oldLevel=getLevel();
        setLevel(Level.OFF);
        super.close();
        for(int i=count-2;i>=0;i--){
            File f1=files[i];
            File f2=files[i+1];
            if(f1.exists()){
                if(f2.exists()){
                    f2.delete();
                }
                f1.renameTo(f2);
            }
        }
        try{
            open(files[0],false);
        }catch(IOException ix){
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null,ix,ErrorManager.OPEN_FAILURE);
        }
        setLevel(oldLevel);
    }

    public FileHandler(String pattern) throws IOException, SecurityException{
        if(pattern.length()<1){
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern=pattern;
        this.limit=0;
        this.count=1;
        openFiles();
    }

    public FileHandler(String pattern,boolean append) throws IOException,
            SecurityException{
        if(pattern.length()<1){
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern=pattern;
        this.limit=0;
        this.count=1;
        this.append=append;
        openFiles();
    }

    public FileHandler(String pattern,int limit,int count)
            throws IOException, SecurityException{
        if(limit<0||count<1||pattern.length()<1){
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern=pattern;
        this.limit=limit;
        this.count=count;
        openFiles();
    }

    public FileHandler(String pattern,int limit,int count,boolean append)
            throws IOException, SecurityException{
        if(limit<0||count<1||pattern.length()<1){
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern=pattern;
        this.limit=limit;
        this.count=count;
        this.append=append;
        openFiles();
    }

    @Override
    public synchronized void publish(LogRecord record){
        if(!isLoggable(record)){
            return;
        }
        super.publish(record);
        flush();
        if(limit>0&&meter.written>=limit){
            // We performed access checks in the "init" method to make sure
            // we are only initialized from trusted code.  So we assume
            // it is OK to write the target files, even if we are
            // currently being called from untrusted code.
            // So it is safe to raise privilege here.
            AccessController.doPrivileged(new PrivilegedAction<Object>(){
                @Override
                public Object run(){
                    rotate();
                    return null;
                }
            });
        }
    }

    @Override
    public synchronized void close() throws SecurityException{
        super.close();
        // Unlock any lock file.
        if(lockFileName==null){
            return;
        }
        try{
            // Close the lock file channel (which also will free any locks)
            lockFileChannel.close();
        }catch(Exception ex){
            // Problems closing the stream.  Punt.
        }
        synchronized(locks){
            locks.remove(lockFileName);
        }
        new File(lockFileName).delete();
        lockFileName=null;
        lockFileChannel=null;
    }

    private static class InitializationErrorManager extends ErrorManager{
        Exception lastException;

        @Override
        public void error(String msg,Exception ex,int code){
            lastException=ex;
        }
    }

    private class MeteredStream extends OutputStream{
        final OutputStream out;
        int written;

        MeteredStream(OutputStream out,int written){
            this.out=out;
            this.written=written;
        }

        @Override
        public void write(int b) throws IOException{
            out.write(b);
            written++;
        }

        @Override
        public void write(byte buff[]) throws IOException{
            out.write(buff);
            written+=buff.length;
        }

        @Override
        public void write(byte buff[],int off,int len) throws IOException{
            out.write(buff,off,len);
            written+=len;
        }

        @Override
        public void flush() throws IOException{
            out.flush();
        }

        @Override
        public void close() throws IOException{
            out.close();
        }
    }
}
