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

import com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import com.sun.org.apache.xerces.internal.util.MessageFormatter;
import com.sun.xml.internal.stream.util.BufferAllocator;
import com.sun.xml.internal.stream.util.ThreadLocalBufferAllocator;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;

public class ASCIIReader
        extends Reader{
    //
    // Constants
    //
    public static final int DEFAULT_BUFFER_SIZE=2048;
    //
    // Data
    //
    protected InputStream fInputStream;
    protected byte[] fBuffer;
    // message formatter; used to produce localized
    // exception messages
    private MessageFormatter fFormatter=null;
    //Locale to use for messages
    private Locale fLocale=null;
    //
    // Constructors
    //

    public ASCIIReader(InputStream inputStream,MessageFormatter messageFormatter,
                       Locale locale){
        this(inputStream,DEFAULT_BUFFER_SIZE,messageFormatter,locale);
    } // <init>(InputStream, MessageFormatter, Locale)

    public ASCIIReader(InputStream inputStream,int size,
                       MessageFormatter messageFormatter,Locale locale){
        fInputStream=inputStream;
        BufferAllocator ba=ThreadLocalBufferAllocator.getBufferAllocator();
        fBuffer=ba.getByteBuffer(size);
        if(fBuffer==null){
            fBuffer=new byte[size];
        }
        fFormatter=messageFormatter;
        fLocale=locale;
    } // <init>(InputStream,int, MessageFormatter, Locale)
    //
    // Reader methods
    //

    public int read() throws IOException{
        int b0=fInputStream.read();
        if(b0>=0x80){
            throw new MalformedByteSequenceException(fFormatter,
                    fLocale,XMLMessageFormatter.XML_DOMAIN,
                    "InvalidASCII",new Object[]{Integer.toString(b0)});
        }
        return b0;
    } // read():int

    public int read(char ch[],int offset,int length) throws IOException{
        if(length>fBuffer.length){
            length=fBuffer.length;
        }
        int count=fInputStream.read(fBuffer,0,length);
        for(int i=0;i<count;i++){
            int b0=fBuffer[i];
            if(b0<0){
                throw new MalformedByteSequenceException(fFormatter,
                        fLocale,XMLMessageFormatter.XML_DOMAIN,
                        "InvalidASCII",new Object[]{Integer.toString(b0&0x0FF)});
            }
            ch[offset+i]=(char)b0;
        }
        return count;
    } // read(char[],int,int)

    public long skip(long n) throws IOException{
        return fInputStream.skip(n);
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
} // class ASCIIReader
