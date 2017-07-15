/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * <p>
 * $Id: SerializerTraceWriter.java,v 1.2.4.1 2005/09/15 08:15:25 suresh_emailid Exp $
 */
/**
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: SerializerTraceWriter.java,v 1.2.4.1 2005/09/15 08:15:25 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

final class SerializerTraceWriter extends Writer implements WriterChain{
    private final Writer m_writer;
    private final SerializerTrace m_tracer;
    private int buf_length;
    private byte buf[];
    private int count;

    public SerializerTraceWriter(Writer out,SerializerTrace tracer){
        m_writer=out;
        m_tracer=tracer;
        setBufferSize(1024);
    }

    private void setBufferSize(int size){
        buf=new byte[size+3];
        buf_length=size;
        count=0;
    }

    public void write(final int c) throws IOException{
        // send to the real writer
        if(m_writer!=null)
            m_writer.write(c);
        // ---------- from here on just collect for tracing purposes
        /** If we are close to the end of the buffer then flush it.
         * Remember the buffer can hold a few more characters than buf_length
         */
        if(count>=buf_length)
            flushBuffer();
        if(c<0x80){
            buf[count++]=(byte)(c);
        }else if(c<0x800){
            buf[count++]=(byte)(0xc0+(c>>6));
            buf[count++]=(byte)(0x80+(c&0x3f));
        }else{
            buf[count++]=(byte)(0xe0+(c>>12));
            buf[count++]=(byte)(0x80+((c>>6)&0x3f));
            buf[count++]=(byte)(0x80+(c&0x3f));
        }
    }

    public void write(final char chars[],final int start,final int length)
            throws IOException{
        // send to the real writer
        if(m_writer!=null)
            m_writer.write(chars,start,length);
        // from here on just collect for tracing purposes
        int lengthx3=(length<<1)+length;
        if(lengthx3>=buf_length){
            /** If the request length exceeds the size of the output buffer,
             * flush the output buffer and make the buffer bigger to handle.
             */
            flushBuffer();
            setBufferSize(2*lengthx3);
        }
        if(lengthx3>buf_length-count){
            flushBuffer();
        }
        final int n=length+start;
        for(int i=start;i<n;i++){
            final char c=chars[i];
            if(c<0x80)
                buf[count++]=(byte)(c);
            else if(c<0x800){
                buf[count++]=(byte)(0xc0+(c>>6));
                buf[count++]=(byte)(0x80+(c&0x3f));
            }else{
                buf[count++]=(byte)(0xe0+(c>>12));
                buf[count++]=(byte)(0x80+((c>>6)&0x3f));
                buf[count++]=(byte)(0x80+(c&0x3f));
            }
        }
    }

    public void write(final String s) throws IOException{
        // send to the real writer
        if(m_writer!=null)
            m_writer.write(s);
        // from here on just collect for tracing purposes
        final int length=s.length();
        // We multiply the length by three since this is the maximum length
        // of the characters that we can put into the buffer.  It is possible
        // for each Unicode character to expand to three bytes.
        int lengthx3=(length<<1)+length;
        if(lengthx3>=buf_length){
            /** If the request length exceeds the size of the output buffer,
             * flush the output buffer and make the buffer bigger to handle.
             */
            flushBuffer();
            setBufferSize(2*lengthx3);
        }
        if(lengthx3>buf_length-count){
            flushBuffer();
        }
        for(int i=0;i<length;i++){
            final char c=s.charAt(i);
            if(c<0x80)
                buf[count++]=(byte)(c);
            else if(c<0x800){
                buf[count++]=(byte)(0xc0+(c>>6));
                buf[count++]=(byte)(0x80+(c&0x3f));
            }else{
                buf[count++]=(byte)(0xe0+(c>>12));
                buf[count++]=(byte)(0x80+((c>>6)&0x3f));
                buf[count++]=(byte)(0x80+(c&0x3f));
            }
        }
    }

    public void flush() throws IOException{
        // send to the real writer
        if(m_writer!=null)
            m_writer.flush();
        // from here on just for tracing purposes
        flushBuffer();
    }

    private void flushBuffer() throws IOException{
        // Just for tracing purposes
        if(count>0){
            char[] chars=new char[count];
            for(int i=0;i<count;i++)
                chars[i]=(char)buf[i];
            if(m_tracer!=null)
                m_tracer.fireGenerateEvent(
                        SerializerTrace.EVENTTYPE_OUTPUT_CHARACTERS,
                        chars,
                        0,
                        chars.length);
            count=0;
        }
    }

    public void close() throws IOException{
        // send to the real writer
        if(m_writer!=null)
            m_writer.close();
        // from here on just for tracing purposes
        flushBuffer();
    }

    public Writer getWriter(){
        return m_writer;
    }

    public OutputStream getOutputStream(){
        OutputStream retval=null;
        if(m_writer instanceof WriterChain)
            retval=((WriterChain)m_writer).getOutputStream();
        return retval;
    }
}
