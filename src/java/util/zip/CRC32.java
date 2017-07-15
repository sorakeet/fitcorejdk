/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.zip;

import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;

public class CRC32 implements Checksum{
    private int crc;

    public CRC32(){
    }

    public void update(int b){
        crc=update(crc,b);
    }

    public void update(byte[] b,int off,int len){
        if(b==null){
            throw new NullPointerException();
        }
        if(off<0||len<0||off>b.length-len){
            throw new ArrayIndexOutOfBoundsException();
        }
        crc=updateBytes(crc,b,off,len);
    }

    public long getValue(){
        return (long)crc&0xffffffffL;
    }

    public void reset(){
        crc=0;
    }

    private native static int updateBytes(int crc,byte[] b,int off,int len);

    private native static int update(int crc,int b);

    public void update(byte[] b){
        crc=updateBytes(crc,b,0,b.length);
    }

    public void update(ByteBuffer buffer){
        int pos=buffer.position();
        int limit=buffer.limit();
        assert (pos<=limit);
        int rem=limit-pos;
        if(rem<=0)
            return;
        if(buffer instanceof DirectBuffer){
            crc=updateByteBuffer(crc,((DirectBuffer)buffer).address(),pos,rem);
        }else if(buffer.hasArray()){
            crc=updateBytes(crc,buffer.array(),pos+buffer.arrayOffset(),rem);
        }else{
            byte[] b=new byte[rem];
            buffer.get(b);
            crc=updateBytes(crc,b,0,b.length);
        }
        buffer.position(limit);
    }

    private native static int updateByteBuffer(int adler,long addr,
                                               int off,int len);
}
