/**
 * Copyright (c) 1994, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

public abstract class OutputStream implements Closeable, Flushable{
    public void write(byte b[]) throws IOException{
        write(b,0,b.length);
    }

    public void write(byte b[],int off,int len) throws IOException{
        if(b==null){
            throw new NullPointerException();
        }else if((off<0)||(off>b.length)||(len<0)||
                ((off+len)>b.length)||((off+len)<0)){
            throw new IndexOutOfBoundsException();
        }else if(len==0){
            return;
        }
        for(int i=0;i<len;i++){
            write(b[off+i]);
        }
    }

    public abstract void write(int b) throws IOException;

    public void flush() throws IOException{
    }

    public void close() throws IOException{
    }
}
