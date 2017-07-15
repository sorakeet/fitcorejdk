/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class BerDecoder{
    //
    // Some standard tags
    //
    public final static int BooleanTag=1;
    public final static int IntegerTag=2;
    public final static int OctetStringTag=4;
    public final static int NullTag=5;
    public final static int OidTag=6;
    public final static int SequenceTag=0x30;
    // private static final void debug(String str) {
    //   System.out.println(str);
    // }
    //
    // This is the byte array containing the encoding.
    //
    private final byte bytes[];
    //
    // This is the stack where end of sequences are kept.
    // A value is computed and pushed in it each time openSequence()
    // is invoked.
    // A value is pulled and checked each time closeSequence() is called.
    //
    private final int stackBuf[]=new int[200];
    //
    // This is the current location. It is the next byte
    // to be decoded. It's an index in bytes[].
    //
    private int next=0;
    private int stackTop=0;

    public BerDecoder(byte b[]){
        bytes=b;
        reset();
    }

    public void reset(){
        next=0;
        stackTop=0;
    }

    public int fetchInteger() throws BerException{
        return fetchInteger(IntegerTag);
    }

    public int fetchInteger(int tag) throws BerException{
        int result=0;
        final int backup=next;
        try{
            if(fetchTag()!=tag){
                throw new BerException();
            }
            result=fetchIntegerValue();
        }catch(BerException e){
            next=backup;
            throw e;
        }
        return result;
    }

    private final int fetchTag() throws BerException{
        int result=0;
        final int backup=next;
        try{
            final byte b0=bytes[next++];
            result=(b0>=0)?b0:b0+256;
            if((result&31)==31){
                while((bytes[next]&128)!=0){
                    result=result<<7;
                    result=result|(bytes[next++]&127);
                }
            }
        }catch(IndexOutOfBoundsException e){
            next=backup;
            throw new BerException();
        }
        return result;
    }

    private int fetchIntegerValue() throws BerException{
        int result=0;
        final int backup=next;
        try{
            final int length=fetchLength();
            if(length<=0) throw new BerException();
            if(length>(bytes.length-next)) throw
                    new IndexOutOfBoundsException("Decoded length exceeds buffer");
            final int end=next+length;
            result=bytes[next++];
            while(next<end){
                final byte b=bytes[next++];
                if(b<0){
                    result=(result<<8)|(256+b);
                }else{
                    result=(result<<8)|b;
                }
            }
        }catch(BerException e){
            next=backup;
            throw e;
        }catch(IndexOutOfBoundsException e){
            next=backup;
            throw new BerException();
        }catch(ArithmeticException e){
            next=backup;
            throw new BerException();
        }
        return result;
    }

    private final int fetchLength() throws BerException{
        int result=0;
        final int backup=next;
        try{
            final byte b0=bytes[next++];
            if(b0>=0){
                result=b0;
            }else{
                for(int c=128+b0;c>0;c--){
                    final byte bX=bytes[next++];
                    result=result<<8;
                    result=result|((bX>=0)?bX:bX+256);
                }
            }
        }catch(IndexOutOfBoundsException e){
            next=backup;
            throw new BerException();
        }
        return result;
    }

    public long fetchIntegerAsLong() throws BerException{
        return fetchIntegerAsLong(IntegerTag);
    }

    public long fetchIntegerAsLong(int tag) throws BerException{
        long result=0;
        final int backup=next;
        try{
            if(fetchTag()!=tag){
                throw new BerException();
            }
            result=fetchIntegerValueAsLong();
        }catch(BerException e){
            next=backup;
            throw e;
        }
        return result;
    }

    private final long fetchIntegerValueAsLong() throws BerException{
        long result=0;
        final int backup=next;
        try{
            final int length=fetchLength();
            if(length<=0) throw new BerException();
            if(length>(bytes.length-next)) throw
                    new IndexOutOfBoundsException("Decoded length exceeds buffer");
            final int end=next+length;
            result=bytes[next++];
            while(next<end){
                final byte b=bytes[next++];
                if(b<0){
                    result=(result<<8)|(256+b);
                }else{
                    result=(result<<8)|b;
                }
            }
        }catch(BerException e){
            next=backup;
            throw e;
        }catch(IndexOutOfBoundsException e){
            next=backup;
            throw new BerException();
        }catch(ArithmeticException e){
            next=backup;
            throw new BerException();
        }
        return result;
    }

    public byte[] fetchOctetString() throws BerException{
        return fetchOctetString(OctetStringTag);
    }

    public byte[] fetchOctetString(int tag) throws BerException{
        byte[] result=null;
        final int backup=next;
        try{
            if(fetchTag()!=tag){
                throw new BerException();
            }
            result=fetchStringValue();
        }catch(BerException e){
            next=backup;
            throw e;
        }
        return result;
    }

    private byte[] fetchStringValue() throws BerException{
        byte[] result=null;
        final int backup=next;
        try{
            final int length=fetchLength();
            if(length<0) throw new BerException();
            if(length>(bytes.length-next))
                throw new IndexOutOfBoundsException("Decoded length exceeds buffer");
            final byte data[]=new byte[length];
            System.arraycopy(bytes,next,data,0,length);
            next+=length;
            //      int i = 0 ;
            //      while (i < length) {
            //          result[i++] = bytes[next++] ;
            //      }
            result=data;
        }catch(BerException e){
            next=backup;
            throw e;
        }catch(IndexOutOfBoundsException e){
            next=backup;
            throw new BerException();
        }catch(ArithmeticException e){
            next=backup;
            throw new BerException();
        }
        // catch(Error e) {
        //  debug("fetchStringValue: Error decoding BER: " + e);
        //  throw e;
        // }
        return result;
    }

    public long[] fetchOid() throws BerException{
        return fetchOid(OidTag);
    }

    public long[] fetchOid(int tag) throws BerException{
        long[] result=null;
        final int backup=next;
        try{
            if(fetchTag()!=tag){
                throw new BerException();
            }
            result=fetchOidValue();
        }catch(BerException e){
            next=backup;
            throw e;
        }
        return result;
    }

    private final long[] fetchOidValue() throws BerException{
        long[] result=null;
        final int backup=next;
        try{
            final int length=fetchLength();
            if(length<=0) throw new BerException();
            if(length>(bytes.length-next))
                throw new IndexOutOfBoundsException("Decoded length exceeds buffer");
            // Count how many bytes have their 8th bit to 0
            // -> this gives the number of components in the oid
            int subidCount=2;
            for(int i=1;i<length;i++){
                if((bytes[next+i]&0x80)==0){
                    subidCount++;
                }
            }
            final int datalen=subidCount;
            final long[] data=new long[datalen];
            final byte b0=bytes[next++];
            // bugId 4641746
            // The 8th bit of the first byte should always be set to 0
            if(b0<0) throw new BerException();
            // bugId 4641746
            // The first sub Id cannot be greater than 2
            final long lb0=b0/40;
            if(lb0>2) throw new BerException();
            final long lb1=b0%40;
            data[0]=lb0;
            data[1]=lb1;
            int i=2;
            while(i<datalen){
                long subid=0;
                byte b=bytes[next++];
                while((b&0x80)!=0){
                    subid=(subid<<7)|(b&0x7f);
                    // bugId 4654674
                    if(subid<0) throw new BerException();
                    b=bytes[next++];
                }
                subid=(subid<<7)|b;
                // bugId 4654674
                if(subid<0) throw new BerException();
                data[i++]=subid;
            }
            result=data;
        }catch(BerException e){
            next=backup;
            throw e;
        }catch(IndexOutOfBoundsException e){
            next=backup;
            throw new BerException();
        }
        // catch(Error e) {
        //  debug("fetchOidValue: Error decoding BER: " + e);
        //  throw e;
        // }
        return result;
    }
    ////////////////////////// PRIVATE ///////////////////////////////

    public void fetchNull() throws BerException{
        fetchNull(NullTag);
    }

    public void fetchNull(int tag) throws BerException{
        final int backup=next;
        try{
            if(fetchTag()!=tag){
                throw new BerException();
            }
            final int length=fetchLength();
            if(length!=0) throw new BerException();
        }catch(BerException e){
            next=backup;
            throw e;
        }
    }

    public byte[] fetchAny(int tag) throws BerException{
        if(getTag()!=tag){
            throw new BerException();
        }
        return fetchAny();
    }

    public byte[] fetchAny() throws BerException{
        byte[] result=null;
        final int backup=next;
        try{
            final int tag=fetchTag();
            final int contentLength=fetchLength();
            if(contentLength<0) throw new BerException();
            final int tlvLength=next+contentLength-backup;
            if(contentLength>(bytes.length-next))
                throw new IndexOutOfBoundsException("Decoded length exceeds buffer");
            final byte[] data=new byte[tlvLength];
            System.arraycopy(bytes,backup,data,0,tlvLength);
            // for (int i = 0 ; i < tlvLength ; i++) {
            //  data[i] = bytes[backup + i] ;
            // }
            next=next+contentLength;
            result=data;
        }catch(IndexOutOfBoundsException e){
            next=backup;
            throw new BerException();
        }
        // catch(Error e) {
        //    debug("fetchAny: Error decoding BER: " + e);
        //    throw e;
        // }
        return result;
    }

    public int getTag() throws BerException{
        int result=0;
        final int backup=next;
        try{
            result=fetchTag();
        }finally{
            next=backup;
        }
        return result;
    }

    public void openSequence() throws BerException{
        openSequence(SequenceTag);
    }

    public void openSequence(int tag) throws BerException{
        final int backup=next;
        try{
            if(fetchTag()!=tag){
                throw new BerException();
            }
            final int l=fetchLength();
            if(l<0) throw new BerException();
            if(l>(bytes.length-next)) throw new BerException();
            stackBuf[stackTop++]=next+l;
        }catch(BerException e){
            next=backup;
            throw e;
        }
    }

    public void closeSequence() throws BerException{
        if(stackBuf[stackTop-1]==next){
            stackTop--;
        }else{
            throw new BerException();
        }
    }

    public boolean cannotCloseSequence(){
        return (next<stackBuf[stackTop-1]);
    }

    public String toString(){
        final StringBuffer result=new StringBuffer(bytes.length*2);
        for(int i=0;i<bytes.length;i++){
            final int b=(bytes[i]>0)?bytes[i]:bytes[i]+256;
            if(i==next){
                result.append("(");
            }
            result.append(Character.forDigit(b/16,16));
            result.append(Character.forDigit(b%16,16));
            if(i==next){
                result.append(")");
            }
        }
        if(bytes.length==next){
            result.append("()");
        }
        return new String(result);
    }
}
