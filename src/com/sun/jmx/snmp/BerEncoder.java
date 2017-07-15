/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class BerEncoder{
    //
    // Some standard tags
    //
    public final static int BooleanTag=1;
    public final static int IntegerTag=2;
    public final static int OctetStringTag=4;
    public final static int NullTag=5;
    public final static int OidTag=6;
    public final static int SequenceTag=0x30;
    //
    // This is the byte array containing the encoding.
    //
    protected final byte bytes[];
    //
    // This is the stack where end of sequences are kept.
    // A value is computed and pushed in it each time the <CODE>openSequence</CODE> method
    // is invoked.
    // A value is pulled and checked each time the <CODE>closeSequence</CODE> method is called.
    //
    protected final int stackBuf[]=new int[200];
    //
    // This is the index of the first byte of the encoding.
    // It is initialized to <CODE>bytes.length</CODE> and decrease each time
    // an value is put in the encoder.
    //
    protected int start=-1;
    protected int stackTop=0;

    public BerEncoder(byte b[]){
        bytes=b;
        start=b.length;
        stackTop=0;
    }

    public int trim(){
        final int result=bytes.length-start;
        // for (int i = start ; i < bytes.length ; i++) {
        //  bytes[i-start] = bytes[i] ;
        // }
        if(result>0)
            System.arraycopy(bytes,start,bytes,0,result);
        start=bytes.length;
        stackTop=0;
        return result;
    }

    public void putInteger(int v){
        putInteger(v,IntegerTag);
    }

    public void putInteger(int v,int tag){
        putIntegerValue(v);
        putTag(tag);
    }

    protected final void putTag(int tag){
        if(tag<256){
            bytes[--start]=(byte)tag;
        }else{
            while(tag!=0){
                bytes[--start]=(byte)(tag&127);
                tag=tag<<7;
            }
        }
    }

    protected final void putIntegerValue(int v){
        final int end=start;
        int mask=0x7f800000;
        int byteNeeded=4;
        if(v<0){
            while(((mask&v)==mask)&&(byteNeeded>1)){
                mask=mask>>8;
                byteNeeded--;
            }
        }else{
            while(((mask&v)==0)&&(byteNeeded>1)){
                mask=mask>>8;
                byteNeeded--;
            }
        }
        for(int i=0;i<byteNeeded;i++){
            bytes[--start]=(byte)v;
            v=v>>8;
        }
        putLength(end-start);
    }

    protected final void putLength(final int length){
        if(length<0){
            throw new IllegalArgumentException();
        }else if(length<128){
            bytes[--start]=(byte)length;
        }else if(length<256){
            bytes[--start]=(byte)length;
            bytes[--start]=(byte)0x81;
        }else if(length<65536){
            bytes[--start]=(byte)(length);
            bytes[--start]=(byte)(length>>8);
            bytes[--start]=(byte)0x82;
        }else if(length<16777126){
            bytes[--start]=(byte)(length);
            bytes[--start]=(byte)(length>>8);
            bytes[--start]=(byte)(length>>16);
            bytes[--start]=(byte)0x83;
        }else{
            bytes[--start]=(byte)(length);
            bytes[--start]=(byte)(length>>8);
            bytes[--start]=(byte)(length>>16);
            bytes[--start]=(byte)(length>>24);
            bytes[--start]=(byte)0x84;
        }
    }

    public void putInteger(long v){
        putInteger(v,IntegerTag);
    }

    public void putInteger(long v,int tag){
        putIntegerValue(v);
        putTag(tag);
    }

    protected final void putIntegerValue(long v){
        final int end=start;
        long mask=0x7f80000000000000L;
        int byteNeeded=8;
        if(v<0){
            while(((mask&v)==mask)&&(byteNeeded>1)){
                mask=mask>>8;
                byteNeeded--;
            }
        }else{
            while(((mask&v)==0)&&(byteNeeded>1)){
                mask=mask>>8;
                byteNeeded--;
            }
        }
        for(int i=0;i<byteNeeded;i++){
            bytes[--start]=(byte)v;
            v=v>>8;
        }
        putLength(end-start);
    }

    public void putOctetString(byte[] s){
        putOctetString(s,OctetStringTag);
    }

    public void putOctetString(byte[] s,int tag){
        putStringValue(s);
        putTag(tag);
    }

    protected final void putStringValue(byte[] s){
        final int datalen=s.length;
        System.arraycopy(s,0,bytes,start-datalen,datalen);
        start-=datalen;
        // for (int i = s.length - 1 ; i >= 0 ; i--) {
        //   bytes[--start] = s[i] ;
        // }
        putLength(datalen);
    }
    ////////////////////////// PROTECTED ///////////////////////////////

    public void putOid(long[] s){
        putOid(s,OidTag);
    }

    public void putOid(long[] s,int tag){
        putOidValue(s);
        putTag(tag);
    }

    protected final void putOidValue(final long[] s){
        final int end=start;
        final int slength=s.length;
        // bugId 4641746: 0, 1, and 2 are legal values.
        if((slength<2)||(s[0]>2)||(s[1]>=40)){
            throw new IllegalArgumentException();
        }
        for(int i=slength-1;i>=2;i--){
            long c=s[i];
            if(c<0){
                throw new IllegalArgumentException();
            }else if(c<128){
                bytes[--start]=(byte)c;
            }else{
                bytes[--start]=(byte)(c&127);
                c=c>>7;
                while(c!=0){
                    bytes[--start]=(byte)(c|128);
                    c=c>>7;
                }
            }
        }
        bytes[--start]=(byte)(s[0]*40+s[1]);
        putLength(end-start);
    }

    public void putNull(){
        putNull(NullTag);
    }

    public void putNull(int tag){
        putLength(0);
        putTag(tag);
    }

    public void putAny(byte[] s){
        putAny(s,s.length);
    }

    public void putAny(byte[] s,int byteCount){
        System.arraycopy(s,0,bytes,start-byteCount,byteCount);
        start-=byteCount;
        //    for (int i = byteCount - 1 ; i >= 0 ; i--) {
        //      bytes[--start] = s[i] ;
        //    }
    }

    public void openSequence(){
        stackBuf[stackTop++]=start;
    }

    public void closeSequence(){
        closeSequence(SequenceTag);
    }

    public void closeSequence(int tag){
        final int end=stackBuf[--stackTop];
        putLength(end-start);
        putTag(tag);
    }
}
