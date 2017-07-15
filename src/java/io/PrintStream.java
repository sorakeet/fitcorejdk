/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Formatter;
import java.util.Locale;

public class PrintStream extends FilterOutputStream
        implements Appendable, Closeable{
    private final boolean autoFlush;
    private boolean trouble=false;
    private Formatter formatter;
    private BufferedWriter textOut;
    private OutputStreamWriter charOut;
    private boolean closing=false;

    public PrintStream(OutputStream out){
        this(out,false);
    }

    public PrintStream(OutputStream out,boolean autoFlush){
        this(autoFlush,requireNonNull(out,"Null output stream"));
    }

    private static <T> T requireNonNull(T obj,String message){
        if(obj==null)
            throw new NullPointerException(message);
        return obj;
    }

    private PrintStream(boolean autoFlush,OutputStream out){
        super(out);
        this.autoFlush=autoFlush;
        this.charOut=new OutputStreamWriter(this);
        this.textOut=new BufferedWriter(charOut);
    }

    public PrintStream(OutputStream out,boolean autoFlush,String encoding)
            throws UnsupportedEncodingException{
        this(autoFlush,
                requireNonNull(out,"Null output stream"),
                toCharset(encoding));
    }

    private static Charset toCharset(String csn)
            throws UnsupportedEncodingException{
        requireNonNull(csn,"charsetName");
        try{
            return Charset.forName(csn);
        }catch(IllegalCharsetNameException|UnsupportedCharsetException unused){
            // UnsupportedEncodingException should be thrown
            throw new UnsupportedEncodingException(csn);
        }
    }

    private PrintStream(boolean autoFlush,OutputStream out,Charset charset){
        super(out);
        this.autoFlush=autoFlush;
        this.charOut=new OutputStreamWriter(this,charset);
        this.textOut=new BufferedWriter(charOut);
    }

    public PrintStream(String fileName) throws FileNotFoundException{
        this(false,new FileOutputStream(fileName));
    }

    public PrintStream(String fileName,String csn)
            throws FileNotFoundException, UnsupportedEncodingException{
        // ensure charset is checked before the file is opened
        this(false,toCharset(csn),new FileOutputStream(fileName));
    }

    private PrintStream(boolean autoFlush,Charset charset,OutputStream out)
            throws UnsupportedEncodingException{
        this(autoFlush,out,charset);
    }

    public PrintStream(File file) throws FileNotFoundException{
        this(false,new FileOutputStream(file));
    }

    public PrintStream(File file,String csn)
            throws FileNotFoundException, UnsupportedEncodingException{
        // ensure charset is checked before the file is opened
        this(false,toCharset(csn),new FileOutputStream(file));
    }

    public boolean checkError(){
        if(out!=null)
            flush();
        if(out instanceof PrintStream){
            PrintStream ps=(PrintStream)out;
            return ps.checkError();
        }
        return trouble;
    }

    protected void setError(){
        trouble=true;
    }

    protected void clearError(){
        trouble=false;
    }

    public void write(int b){
        try{
            synchronized(this){
                ensureOpen();
                out.write(b);
                if((b=='\n')&&autoFlush)
                    out.flush();
            }
        }catch(InterruptedIOException x){
            Thread.currentThread().interrupt();
        }catch(IOException x){
            trouble=true;
        }
    }

    public void write(byte buf[],int off,int len){
        try{
            synchronized(this){
                ensureOpen();
                out.write(buf,off,len);
                if(autoFlush)
                    out.flush();
            }
        }catch(InterruptedIOException x){
            Thread.currentThread().interrupt();
        }catch(IOException x){
            trouble=true;
        }
    }

    public void flush(){
        synchronized(this){
            try{
                ensureOpen();
                out.flush();
            }catch(IOException x){
                trouble=true;
            }
        }
    }

    private void ensureOpen() throws IOException{
        if(out==null)
            throw new IOException("Stream closed");
    }

    public void close(){
        synchronized(this){
            if(!closing){
                closing=true;
                try{
                    textOut.close();
                    out.close();
                }catch(IOException x){
                    trouble=true;
                }
                textOut=null;
                charOut=null;
                out=null;
            }
        }
    }

    public void print(Object obj){
        write(String.valueOf(obj));
    }

    private void write(String s){
        try{
            synchronized(this){
                ensureOpen();
                textOut.write(s);
                textOut.flushBuffer();
                charOut.flushBuffer();
                if(autoFlush&&(s.indexOf('\n')>=0))
                    out.flush();
            }
        }catch(InterruptedIOException x){
            Thread.currentThread().interrupt();
        }catch(IOException x){
            trouble=true;
        }
    }

    public void println(){
        newLine();
    }

    private void newLine(){
        try{
            synchronized(this){
                ensureOpen();
                textOut.newLine();
                textOut.flushBuffer();
                charOut.flushBuffer();
                if(autoFlush)
                    out.flush();
            }
        }catch(InterruptedIOException x){
            Thread.currentThread().interrupt();
        }catch(IOException x){
            trouble=true;
        }
    }

    public void println(boolean x){
        synchronized(this){
            print(x);
            newLine();
        }
    }

    public void print(boolean b){
        write(b?"true":"false");
    }

    public void println(char x){
        synchronized(this){
            print(x);
            newLine();
        }
    }

    public void print(char c){
        write(String.valueOf(c));
    }

    public void println(int x){
        synchronized(this){
            print(x);
            newLine();
        }
    }

    public void print(int i){
        write(String.valueOf(i));
    }

    public void println(long x){
        synchronized(this){
            print(x);
            newLine();
        }
    }

    public void print(long l){
        write(String.valueOf(l));
    }

    public void println(float x){
        synchronized(this){
            print(x);
            newLine();
        }
    }

    public void print(float f){
        write(String.valueOf(f));
    }

    public void println(double x){
        synchronized(this){
            print(x);
            newLine();
        }
    }

    public void print(double d){
        write(String.valueOf(d));
    }

    public void println(char x[]){
        synchronized(this){
            print(x);
            newLine();
        }
    }

    public void print(char s[]){
        write(s);
    }

    private void write(char buf[]){
        try{
            synchronized(this){
                ensureOpen();
                textOut.write(buf);
                textOut.flushBuffer();
                charOut.flushBuffer();
                if(autoFlush){
                    for(int i=0;i<buf.length;i++)
                        if(buf[i]=='\n')
                            out.flush();
                }
            }
        }catch(InterruptedIOException x){
            Thread.currentThread().interrupt();
        }catch(IOException x){
            trouble=true;
        }
    }

    public void println(String x){
        synchronized(this){
            print(x);
            newLine();
        }
    }

    public void print(String s){
        if(s==null){
            s="null";
        }
        write(s);
    }

    public void println(Object x){
        String s=String.valueOf(x);
        synchronized(this){
            print(s);
            newLine();
        }
    }

    public PrintStream printf(String format,Object... args){
        return format(format,args);
    }

    public PrintStream format(String format,Object... args){
        try{
            synchronized(this){
                ensureOpen();
                if((formatter==null)
                        ||(formatter.locale()!=Locale.getDefault()))
                    formatter=new Formatter((Appendable)this);
                formatter.format(Locale.getDefault(),format,args);
            }
        }catch(InterruptedIOException x){
            Thread.currentThread().interrupt();
        }catch(IOException x){
            trouble=true;
        }
        return this;
    }

    public PrintStream printf(Locale l,String format,Object... args){
        return format(l,format,args);
    }

    public PrintStream format(Locale l,String format,Object... args){
        try{
            synchronized(this){
                ensureOpen();
                if((formatter==null)
                        ||(formatter.locale()!=l))
                    formatter=new Formatter(this,l);
                formatter.format(l,format,args);
            }
        }catch(InterruptedIOException x){
            Thread.currentThread().interrupt();
        }catch(IOException x){
            trouble=true;
        }
        return this;
    }

    public PrintStream append(CharSequence csq){
        if(csq==null)
            print("null");
        else
            print(csq.toString());
        return this;
    }

    public PrintStream append(CharSequence csq,int start,int end){
        CharSequence cs=(csq==null?"null":csq);
        write(cs.subSequence(start,end).toString());
        return this;
    }

    public PrintStream append(char c){
        print(c);
        return this;
    }
}
