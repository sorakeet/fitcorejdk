/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2000-2002,2004 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Copyright 2000-2002,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.impl.io;

import com.sun.xml.internal.stream.util.BufferAllocator;
import com.sun.xml.internal.stream.util.ThreadLocalBufferAllocator;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class UCSReader extends Reader{
    //
    // Constants
    //
    public static final int DEFAULT_BUFFER_SIZE=8192;
    public static final short UCS2LE=1;
    public static final short UCS2BE=2;
    public static final short UCS4LE=4;
    public static final short UCS4BE=8;
    //
    // Data
    //
    protected InputStream fInputStream;
    protected byte[] fBuffer;
    // what kind of data we're dealing with
    protected short fEncoding;
    //
    // Constructors
    //

    public UCSReader(InputStream inputStream,short encoding){
        this(inputStream,DEFAULT_BUFFER_SIZE,encoding);
    } // <init>(InputStream, short)

    public UCSReader(InputStream inputStream,int size,short encoding){
        fInputStream=inputStream;
        BufferAllocator ba=ThreadLocalBufferAllocator.getBufferAllocator();
        fBuffer=ba.getByteBuffer(size);
        if(fBuffer==null){
            fBuffer=new byte[size];
        }
        fEncoding=encoding;
    } // <init>(InputStream,int,short)
    //
    // Reader methods
    //

    public int read() throws IOException{
        int b0=fInputStream.read()&0xff;
        if(b0==0xff)
            return -1;
        int b1=fInputStream.read()&0xff;
        if(b1==0xff)
            return -1;
        if(fEncoding>=4){
            int b2=fInputStream.read()&0xff;
            if(b2==0xff)
                return -1;
            int b3=fInputStream.read()&0xff;
            if(b3==0xff)
                return -1;
            System.err.println("b0 is "+(b0&0xff)+" b1 "+(b1&0xff)+" b2 "+(b2&0xff)+" b3 "+(b3&0xff));
            if(fEncoding==UCS4BE)
                return (b0<<24)+(b1<<16)+(b2<<8)+b3;
            else
                return (b3<<24)+(b2<<16)+(b1<<8)+b0;
        }else{ // UCS-2
            if(fEncoding==UCS2BE)
                return (b0<<8)+b1;
            else
                return (b1<<8)+b0;
        }
    } // read():int

    public int read(char ch[],int offset,int length) throws IOException{
        int byteLength=length<<((fEncoding>=4)?2:1);
        if(byteLength>fBuffer.length){
            byteLength=fBuffer.length;
        }
        int count=fInputStream.read(fBuffer,0,byteLength);
        if(count==-1) return -1;
        // try and make count be a multiple of the number of bytes we're looking for
        if(fEncoding>=4){ // BigEndian
            // this looks ugly, but it avoids an if at any rate...
            int numToRead=(4-(count&3)&3);
            for(int i=0;i<numToRead;i++){
                int charRead=fInputStream.read();
                if(charRead==-1){ // end of input; something likely went wrong!A  Pad buffer with nulls.
                    for(int j=i;j<numToRead;j++)
                        fBuffer[count+j]=0;
                    break;
                }else{
                    fBuffer[count+i]=(byte)charRead;
                }
            }
            count+=numToRead;
        }else{
            int numToRead=count&1;
            if(numToRead!=0){
                count++;
                int charRead=fInputStream.read();
                if(charRead==-1){ // end of input; something likely went wrong!A  Pad buffer with nulls.
                    fBuffer[count]=0;
                }else{
                    fBuffer[count]=(byte)charRead;
                }
            }
        }
        // now count is a multiple of the right number of bytes
        int numChars=count>>((fEncoding>=4)?2:1);
        int curPos=0;
        for(int i=0;i<numChars;i++){
            int b0=fBuffer[curPos++]&0xff;
            int b1=fBuffer[curPos++]&0xff;
            if(fEncoding>=4){
                int b2=fBuffer[curPos++]&0xff;
                int b3=fBuffer[curPos++]&0xff;
                if(fEncoding==UCS4BE)
                    ch[offset+i]=(char)((b0<<24)+(b1<<16)+(b2<<8)+b3);
                else
                    ch[offset+i]=(char)((b3<<24)+(b2<<16)+(b1<<8)+b0);
            }else{ // UCS-2
                if(fEncoding==UCS2BE)
                    ch[offset+i]=(char)((b0<<8)+b1);
                else
                    ch[offset+i]=(char)((b1<<8)+b0);
            }
        }
        return numChars;
    } // read(char[],int,int)

    public long skip(long n) throws IOException{
        // charWidth will represent the number of bits to move
        // n leftward to get num of bytes to skip, and then move the result rightward
        // to get num of chars effectively skipped.
        // The trick with &'ing, as with elsewhere in this dcode, is
        // intended to avoid an expensive use of / that might not be optimized
        // away.
        int charWidth=(fEncoding>=4)?2:1;
        long bytesSkipped=fInputStream.skip(n<<charWidth);
        if((bytesSkipped&(charWidth|1))==0) return bytesSkipped>>charWidth;
        return (bytesSkipped>>charWidth)+1;
    } // skip(long):long

    public boolean ready() throws IOException{
        return false;
    } // ready()

    public boolean markSupported(){
        return fInputStream.markSupported();
    } // markSupported()

    public void mark(int readAheadLimit) throws IOException{
        fInputStream.mark(readAheadLimit);
    } // mark(int)

    public void reset() throws IOException{
        fInputStream.reset();
    } // reset()

    public void close() throws IOException{
        BufferAllocator ba=ThreadLocalBufferAllocator.getBufferAllocator();
        ba.returnByteBuffer(fBuffer);
        fBuffer=null;
        fInputStream.close();
    } // close()
} // class UCSReader
