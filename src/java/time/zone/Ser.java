/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2011-2012, Stephen Colebourne & Michael Nascimento Santos
 * <p>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * * Neither the name of JSR-310 nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 *
 *
 *
 *
 *
 * Copyright (c) 2011-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package java.time.zone;

import java.io.*;
import java.time.ZoneOffset;

final class Ser implements Externalizable{
    static final byte ZRULES=1;
    static final byte ZOT=2;
    static final byte ZOTRULE=3;
    private static final long serialVersionUID=-8885321777449118786L;
    private byte type;
    private Object object;

    public Ser(){
    }

    Ser(byte type,Object object){
        this.type=type;
        this.object=object;
    }

    static void write(Object object,DataOutput out) throws IOException{
        writeInternal(ZRULES,object,out);
    }

    private static void writeInternal(byte type,Object object,DataOutput out) throws IOException{
        out.writeByte(type);
        switch(type){
            case ZRULES:
                ((ZoneRules)object).writeExternal(out);
                break;
            case ZOT:
                ((ZoneOffsetTransition)object).writeExternal(out);
                break;
            case ZOTRULE:
                ((ZoneOffsetTransitionRule)object).writeExternal(out);
                break;
            default:
                throw new InvalidClassException("Unknown serialized type");
        }
    }

    static Object read(DataInput in) throws IOException, ClassNotFoundException{
        byte type=in.readByte();
        return readInternal(type,in);
    }

    private static Object readInternal(byte type,DataInput in) throws IOException, ClassNotFoundException{
        switch(type){
            case ZRULES:
                return ZoneRules.readExternal(in);
            case ZOT:
                return ZoneOffsetTransition.readExternal(in);
            case ZOTRULE:
                return ZoneOffsetTransitionRule.readExternal(in);
            default:
                throw new StreamCorruptedException("Unknown serialized type");
        }
    }

    //-----------------------------------------------------------------------
    static void writeOffset(ZoneOffset offset,DataOutput out) throws IOException{
        final int offsetSecs=offset.getTotalSeconds();
        int offsetByte=offsetSecs%900==0?offsetSecs/900:127;  // compress to -72 to +72
        out.writeByte(offsetByte);
        if(offsetByte==127){
            out.writeInt(offsetSecs);
        }
    }

    static ZoneOffset readOffset(DataInput in) throws IOException{
        int offsetByte=in.readByte();
        return (offsetByte==127?ZoneOffset.ofTotalSeconds(in.readInt()):ZoneOffset.ofTotalSeconds(offsetByte*900));
    }

    //-----------------------------------------------------------------------
    static void writeEpochSec(long epochSec,DataOutput out) throws IOException{
        if(epochSec>=-4575744000L&&epochSec<10413792000L&&epochSec%900==0){  // quarter hours between 1825 and 2300
            int store=(int)((epochSec+4575744000L)/900);
            out.writeByte((store>>>16)&255);
            out.writeByte((store>>>8)&255);
            out.writeByte(store&255);
        }else{
            out.writeByte(255);
            out.writeLong(epochSec);
        }
    }

    static long readEpochSec(DataInput in) throws IOException{
        int hiByte=in.readByte()&255;
        if(hiByte==255){
            return in.readLong();
        }else{
            int midByte=in.readByte()&255;
            int loByte=in.readByte()&255;
            long tot=((hiByte<<16)+(midByte<<8)+loByte);
            return (tot*900)-4575744000L;
        }
    }

    //-----------------------------------------------------------------------
    @Override
    public void writeExternal(ObjectOutput out) throws IOException{
        writeInternal(type,object,out);
    }

    //-----------------------------------------------------------------------
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
        type=in.readByte();
        object=readInternal(type,in);
    }

    private Object readResolve(){
        return object;
    }
}
