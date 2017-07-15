/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

public class PipedReader extends Reader{
    private static final int DEFAULT_PIPE_SIZE=1024;
    boolean closedByWriter=false;
    boolean closedByReader=false;
    boolean connected=false;
    Thread readSide;
    Thread writeSide;
    char buffer[];
    int in=-1;
    int out=0;

    public PipedReader(PipedWriter src) throws IOException{
        this(src,DEFAULT_PIPE_SIZE);
    }

    public PipedReader(PipedWriter src,int pipeSize) throws IOException{
        initPipe(pipeSize);
        connect(src);
    }

    private void initPipe(int pipeSize){
        if(pipeSize<=0){
            throw new IllegalArgumentException("Pipe size <= 0");
        }
        buffer=new char[pipeSize];
    }

    public void connect(PipedWriter src) throws IOException{
        src.connect(this);
    }

    public PipedReader(){
        initPipe(DEFAULT_PIPE_SIZE);
    }

    public PipedReader(int pipeSize){
        initPipe(pipeSize);
    }

    synchronized void receive(char c[],int off,int len) throws IOException{
        while(--len>=0){
            receive(c[off++]);
        }
    }

    synchronized void receive(int c) throws IOException{
        if(!connected){
            throw new IOException("Pipe not connected");
        }else if(closedByWriter||closedByReader){
            throw new IOException("Pipe closed");
        }else if(readSide!=null&&!readSide.isAlive()){
            throw new IOException("Read end dead");
        }
        writeSide=Thread.currentThread();
        while(in==out){
            if((readSide!=null)&&!readSide.isAlive()){
                throw new IOException("Pipe broken");
            }
            /** full: kick any waiting readers */
            notifyAll();
            try{
                wait(1000);
            }catch(InterruptedException ex){
                throw new InterruptedIOException();
            }
        }
        if(in<0){
            in=0;
            out=0;
        }
        buffer[in++]=(char)c;
        if(in>=buffer.length){
            in=0;
        }
    }

    synchronized void receivedLast(){
        closedByWriter=true;
        notifyAll();
    }

    public synchronized int read() throws IOException{
        if(!connected){
            throw new IOException("Pipe not connected");
        }else if(closedByReader){
            throw new IOException("Pipe closed");
        }else if(writeSide!=null&&!writeSide.isAlive()
                &&!closedByWriter&&(in<0)){
            throw new IOException("Write end dead");
        }
        readSide=Thread.currentThread();
        int trials=2;
        while(in<0){
            if(closedByWriter){
                /** closed by writer, return EOF */
                return -1;
            }
            if((writeSide!=null)&&(!writeSide.isAlive())&&(--trials<0)){
                throw new IOException("Pipe broken");
            }
            /** might be a writer waiting */
            notifyAll();
            try{
                wait(1000);
            }catch(InterruptedException ex){
                throw new InterruptedIOException();
            }
        }
        int ret=buffer[out++];
        if(out>=buffer.length){
            out=0;
        }
        if(in==out){
            /** now empty */
            in=-1;
        }
        return ret;
    }

    public synchronized int read(char cbuf[],int off,int len) throws IOException{
        if(!connected){
            throw new IOException("Pipe not connected");
        }else if(closedByReader){
            throw new IOException("Pipe closed");
        }else if(writeSide!=null&&!writeSide.isAlive()
                &&!closedByWriter&&(in<0)){
            throw new IOException("Write end dead");
        }
        if((off<0)||(off>cbuf.length)||(len<0)||
                ((off+len)>cbuf.length)||((off+len)<0)){
            throw new IndexOutOfBoundsException();
        }else if(len==0){
            return 0;
        }
        /** possibly wait on the first character */
        int c=read();
        if(c<0){
            return -1;
        }
        cbuf[off]=(char)c;
        int rlen=1;
        while((in>=0)&&(--len>0)){
            cbuf[off+rlen]=buffer[out++];
            rlen++;
            if(out>=buffer.length){
                out=0;
            }
            if(in==out){
                /** now empty */
                in=-1;
            }
        }
        return rlen;
    }

    public synchronized boolean ready() throws IOException{
        if(!connected){
            throw new IOException("Pipe not connected");
        }else if(closedByReader){
            throw new IOException("Pipe closed");
        }else if(writeSide!=null&&!writeSide.isAlive()
                &&!closedByWriter&&(in<0)){
            throw new IOException("Write end dead");
        }
        if(in<0){
            return false;
        }else{
            return true;
        }
    }

    public void close() throws IOException{
        in=-1;
        closedByReader=true;
    }
}
