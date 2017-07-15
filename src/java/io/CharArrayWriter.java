/**
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

import java.util.Arrays;

public class CharArrayWriter extends Writer{
    protected char buf[];
    protected int count;

    public CharArrayWriter(){
        this(32);
    }

    public CharArrayWriter(int initialSize){
        if(initialSize<0){
            throw new IllegalArgumentException("Negative initial size: "
                    +initialSize);
        }
        buf=new char[initialSize];
    }

    public void writeTo(Writer out) throws IOException{
        synchronized(lock){
            out.write(buf,0,count);
        }
    }

    public void reset(){
        count=0;
    }    public void write(int c){
        synchronized(lock){
            int newcount=count+1;
            if(newcount>buf.length){
                buf=Arrays.copyOf(buf,Math.max(buf.length<<1,newcount));
            }
            buf[count]=(char)c;
            count=newcount;
        }
    }

    public char toCharArray()[]{
        synchronized(lock){
            return Arrays.copyOf(buf,count);
        }
    }

    public int size(){
        return count;
    }    public void write(char c[],int off,int len){
        if((off<0)||(off>c.length)||(len<0)||
                ((off+len)>c.length)||((off+len)<0)){
            throw new IndexOutOfBoundsException();
        }else if(len==0){
            return;
        }
        synchronized(lock){
            int newcount=count+len;
            if(newcount>buf.length){
                buf=Arrays.copyOf(buf,Math.max(buf.length<<1,newcount));
            }
            System.arraycopy(c,off,buf,count,len);
            count=newcount;
        }
    }

    public String toString(){
        synchronized(lock){
            return new String(buf,0,count);
        }
    }

    public void write(String str,int off,int len){
        synchronized(lock){
            int newcount=count+len;
            if(newcount>buf.length){
                buf=Arrays.copyOf(buf,Math.max(buf.length<<1,newcount));
            }
            str.getChars(off,off+len,buf,count);
            count=newcount;
        }
    }





    public CharArrayWriter append(CharSequence csq){
        String s=(csq==null?"null":csq.toString());
        write(s,0,s.length());
        return this;
    }

    public CharArrayWriter append(CharSequence csq,int start,int end){
        String s=(csq==null?"null":csq).subSequence(start,end).toString();
        write(s,0,s.length());
        return this;
    }

    public CharArrayWriter append(char c){
        write(c);
        return this;
    }

    public void flush(){
    }

    public void close(){
    }
}
