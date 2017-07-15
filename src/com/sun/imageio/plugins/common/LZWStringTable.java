/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.imageio.plugins.common;

import java.io.PrintStream;

public class LZWStringTable{
    private final static int RES_CODES=2;
    private final static short HASH_FREE=(short)0xFFFF;
    private final static short NEXT_FIRST=(short)0xFFFF;
    private final static int MAXBITS=12;
    private final static int MAXSTR=(1<<MAXBITS);
    private final static short HASHSIZE=9973;
    private final static short HASHSTEP=2039;
    byte[] strChr;  // after predecessor character
    short[] strNxt;  // predecessor string
    short[] strHsh;  // hash table to find  predecessor + char pairs
    short numStrings;  // next code if adding new prestring + char
    int[] strLen;

    public LZWStringTable(){
        strChr=new byte[MAXSTR];
        strNxt=new short[MAXSTR];
        strLen=new int[MAXSTR];
        strHsh=new short[HASHSIZE];
    }

    public short findCharString(short index,byte b){
        int hshidx, nxtidx;
        if(index==HASH_FREE){
            return (short)(b&0xFF);    // Rob fixed used to sign extend
        }
        hshidx=hash(index,b);
        while((nxtidx=strHsh[hshidx])!=HASH_FREE){ // search
            if(strNxt[nxtidx]==index&&strChr[nxtidx]==b){
                return (short)nxtidx;
            }
            hshidx=(hshidx+HASHSTEP)%HASHSIZE;
        }
        return (short)0xFFFF;
    }

    static public int hash(short index,byte lastbyte){
        return ((int)((short)(lastbyte<<8)^index)&0xFFFF)%HASHSIZE;
    }

    public void clearTable(int codesize){
        numStrings=0;
        for(int q=0;q<HASHSIZE;q++){
            strHsh[q]=HASH_FREE;
        }
        int w=(1<<codesize)+RES_CODES;
        for(int q=0;q<w;q++){
            addCharString((short)0xFFFF,(byte)q); // init with no prefix
        }
    }

    public int addCharString(short index,byte b){
        int hshidx;
        if(numStrings>=MAXSTR){ // if used up all codes
            return 0xFFFF;
        }
        hshidx=hash(index,b);
        while(strHsh[hshidx]!=HASH_FREE){
            hshidx=(hshidx+HASHSTEP)%HASHSIZE;
        }
        strHsh[hshidx]=numStrings;
        strChr[numStrings]=b;
        if(index==HASH_FREE){
            strNxt[numStrings]=NEXT_FIRST;
            strLen[numStrings]=1;
        }else{
            strNxt[numStrings]=index;
            strLen[numStrings]=strLen[index]+1;
        }
        return numStrings++; // return the code and inc for next code
    }

    public int expandCode(byte[] buf,int offset,short code,int skipHead){
        if(offset==-2){
            if(skipHead==1){
                skipHead=0;
            }
        }
        if(code==(short)0xFFFF||    // just in case
                skipHead==strLen[code])  // DONE no more unpacked
        {
            return 0;
        }
        int expandLen;  // how much data we are actually expanding
        int codeLen=strLen[code]-skipHead; // length of expanded code left
        int bufSpace=buf.length-offset;  // how much space left
        if(bufSpace>codeLen){
            expandLen=codeLen; // only got this many to unpack
        }else{
            expandLen=bufSpace;
        }
        int skipTail=codeLen-expandLen;  // only > 0 if codeLen > bufSpace [left overs]
        int idx=offset+expandLen;   // initialise to exclusive end address of buffer area
        // NOTE: data unpacks in reverse direction and we are placing the
        // unpacked data directly into the array in the correct location.
        while((idx>offset)&&(code!=(short)0xFFFF)){
            if(--skipTail<0){ // skip required of expanded data
                buf[--idx]=strChr[code];
            }
            code=strNxt[code];    // to predecessor code
        }
        if(codeLen>expandLen){
            return -expandLen; // indicate what part of codeLen used
        }else{
            return expandLen;     // indicate length of dat unpacked
        }
    }

    public void dump(PrintStream out){
        int i;
        for(i=258;i<numStrings;++i){
            out.println(" strNxt["+i+"] = "+strNxt[i]
                    +" strChr "+Integer.toHexString(strChr[i]&0xFF)
                    +" strLen "+Integer.toHexString(strLen[i]));
        }
    }
}
