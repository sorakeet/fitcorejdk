/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2005 The Apache Software Foundation.
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
 * $Id: WriterToUTF8Buffered.java,v 1.2.4.1 2005/09/15 08:15:31 suresh_emailid Exp $
 */
/**
 * Copyright 1999-2005 The Apache Software Foundation.
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
 * $Id: WriterToUTF8Buffered.java,v 1.2.4.1 2005/09/15 08:15:31 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

final class WriterToUTF8Buffered extends Writer implements WriterChain{
    private static final int BYTES_MAX=16*1024;
    private static final int CHARS_MAX=(BYTES_MAX/3);
    // private static final int
    private final OutputStream m_os;
    private final byte m_outputBytes[];
    private final char m_inputChars[];
    private int count;

    public WriterToUTF8Buffered(OutputStream out)
            throws UnsupportedEncodingException{
        m_os=out;
        // get 3 extra bytes to make buffer overflow checking simpler and faster
        // we won't have to keep checking for a few extra characters
        m_outputBytes=new byte[BYTES_MAX+3];
        // Big enough to hold the input chars that will be transformed
        // into output bytes in m_ouputBytes.
        m_inputChars=new char[CHARS_MAX+2];
        count=0;
//      the old body of this constructor, before the buffersize was changed to a constant
//      this(out, 8*1024);
    }
    //  public WriterToUTF8Buffered(final OutputStream out, final int size)
//  {
//
//    m_os = out;
//
//    if (size <= 0)
//    {
//      throw new IllegalArgumentException(
//        SerializerMessages.createMessage(SerializerErrorResources.ER_BUFFER_SIZE_LESSTHAN_ZERO, null)); //"Buffer size <= 0");
//    }
//
//    m_outputBytes = new byte[size];
//    count = 0;
//  }

    public void write(final int c) throws IOException{
        /** If we are close to the end of the buffer then flush it.
         * Remember the buffer can hold a few more bytes than BYTES_MAX
         */
        if(count>=BYTES_MAX)
            flushBuffer();
        if(c<0x80){
            m_outputBytes[count++]=(byte)(c);
        }else if(c<0x800){
            m_outputBytes[count++]=(byte)(0xc0+(c>>6));
            m_outputBytes[count++]=(byte)(0x80+(c&0x3f));
        }else if(c<0x10000){
            m_outputBytes[count++]=(byte)(0xe0+(c>>12));
            m_outputBytes[count++]=(byte)(0x80+((c>>6)&0x3f));
            m_outputBytes[count++]=(byte)(0x80+(c&0x3f));
        }else{
            m_outputBytes[count++]=(byte)(0xf0+(c>>18));
            m_outputBytes[count++]=(byte)(0x80+((c>>12)&0x3f));
            m_outputBytes[count++]=(byte)(0x80+((c>>6)&0x3f));
            m_outputBytes[count++]=(byte)(0x80+(c&0x3f));
        }
    }

    public void write(final char chars[],final int start,final int length)
            throws IOException{
        // We multiply the length by three since this is the maximum length
        // of the characters that we can put into the buffer.  It is possible
        // for each Unicode character to expand to three bytes.
        int lengthx3=3*length;
        if(lengthx3>=BYTES_MAX-count){
            // The requested length is greater than the unused part of the buffer
            flushBuffer();
            if(lengthx3>BYTES_MAX){
                /**
                 * The requested length exceeds the size of the buffer.
                 * Cut the buffer up into chunks, each of which will
                 * not cause an overflow to the output buffer m_outputBytes,
                 * and make multiple recursive calls.
                 * Be careful about integer overflows in multiplication.
                 */
                int split=length/CHARS_MAX;
                final int chunks;
                if(length%CHARS_MAX>0)
                    chunks=split+1;
                else
                    chunks=split;
                int end_chunk=start;
                for(int chunk=1;chunk<=chunks;chunk++){
                    int start_chunk=end_chunk;
                    end_chunk=start+(int)((((long)length)*chunk)/chunks);
                    // Adjust the end of the chunk if it ends on a high char
                    // of a Unicode surrogate pair and low char of the pair
                    // is not going to be in the same chunk
                    final char c=chars[end_chunk-1];
                    int ic=chars[end_chunk-1];
                    if(c>=0xD800&&c<=0xDBFF){
                        // The last Java char that we were going
                        // to process is the first of a
                        // Java surrogate char pair that
                        // represent a Unicode character.
                        if(end_chunk<start+length){
                            // Avoid spanning by including the low
                            // char in the current chunk of chars.
                            end_chunk++;
                        }else{
                            /** This is the last char of the last chunk,
                             * and it is the high char of a high/low pair with
                             * no low char provided.
                             * TODO: error message needed.
                             * The char array incorrectly ends in a high char
                             * of a high/low surrogate pair, but there is
                             * no corresponding low as the high is the last char
                             */
                            end_chunk--;
                        }
                    }
                    int len_chunk=(end_chunk-start_chunk);
                    this.write(chars,start_chunk,len_chunk);
                }
                return;
            }
        }
        final int n=length+start;
        final byte[] buf_loc=m_outputBytes; // local reference for faster access
        int count_loc=count;      // local integer for faster access
        int i=start;
        {
            /** This block could be omitted and the code would produce
             * the same result. But this block exists to give the JIT
             * a better chance of optimizing a tight and common loop which
             * occurs when writing out ASCII characters.
             */
            char c;
            for(;i<n&&(c=chars[i])<0x80;i++)
                buf_loc[count_loc++]=(byte)c;
        }
        for(;i<n;i++){
            final char c=chars[i];
            if(c<0x80)
                buf_loc[count_loc++]=(byte)(c);
            else if(c<0x800){
                buf_loc[count_loc++]=(byte)(0xc0+(c>>6));
                buf_loc[count_loc++]=(byte)(0x80+(c&0x3f));
            }
            /**
             * The following else if condition is added to support XML 1.1 Characters for
             * UTF-8:   [1111 0uuu] [10uu zzzz] [10yy yyyy] [10xx xxxx]*
             * Unicode: [1101 10ww] [wwzz zzyy] (high surrogate)
             *          [1101 11yy] [yyxx xxxx] (low surrogate)
             *          * uuuuu = wwww + 1
             */
            else if(c>=0xD800&&c<=0xDBFF){
                char high, low;
                high=c;
                i++;
                low=chars[i];
                buf_loc[count_loc++]=(byte)(0xF0|(((high+0x40)>>8)&0xf0));
                buf_loc[count_loc++]=(byte)(0x80|(((high+0x40)>>2)&0x3f));
                buf_loc[count_loc++]=(byte)(0x80|((low>>6)&0x0f)+((high<<4)&0x30));
                buf_loc[count_loc++]=(byte)(0x80|(low&0x3f));
            }else{
                buf_loc[count_loc++]=(byte)(0xe0+(c>>12));
                buf_loc[count_loc++]=(byte)(0x80+((c>>6)&0x3f));
                buf_loc[count_loc++]=(byte)(0x80+(c&0x3f));
            }
        }
        // Store the local integer back into the instance variable
        count=count_loc;
    }

    public void write(final String s) throws IOException{
        // We multiply the length by three since this is the maximum length
        // of the characters that we can put into the buffer.  It is possible
        // for each Unicode character to expand to three bytes.
        final int length=s.length();
        int lengthx3=3*length;
        if(lengthx3>=BYTES_MAX-count){
            // The requested length is greater than the unused part of the buffer
            flushBuffer();
            if(lengthx3>BYTES_MAX){
                /**
                 * The requested length exceeds the size of the buffer,
                 * so break it up in chunks that don't exceed the buffer size.
                 */
                final int start=0;
                int split=length/CHARS_MAX;
                final int chunks;
                if(length%CHARS_MAX>0)
                    chunks=split+1;
                else
                    chunks=split;
                int end_chunk=0;
                for(int chunk=1;chunk<=chunks;chunk++){
                    int start_chunk=end_chunk;
                    end_chunk=start+(int)((((long)length)*chunk)/chunks);
                    s.getChars(start_chunk,end_chunk,m_inputChars,0);
                    int len_chunk=(end_chunk-start_chunk);
                    // Adjust the end of the chunk if it ends on a high char
                    // of a Unicode surrogate pair and low char of the pair
                    // is not going to be in the same chunk
                    final char c=m_inputChars[len_chunk-1];
                    if(c>=0xD800&&c<=0xDBFF){
                        // Exclude char in this chunk,
                        // to avoid spanning a Unicode character
                        // that is in two Java chars as a high/low surrogate
                        end_chunk--;
                        len_chunk--;
                        if(chunk==chunks){
                            /** TODO: error message needed.
                             * The String incorrectly ends in a high char
                             * of a high/low surrogate pair, but there is
                             * no corresponding low as the high is the last char
                             * Recover by ignoring this last char.
                             */
                        }
                    }
                    this.write(m_inputChars,0,len_chunk);
                }
                return;
            }
        }
        s.getChars(0,length,m_inputChars,0);
        final char[] chars=m_inputChars;
        final int n=length;
        final byte[] buf_loc=m_outputBytes; // local reference for faster access
        int count_loc=count;      // local integer for faster access
        int i=0;
        {
            /** This block could be omitted and the code would produce
             * the same result. But this block exists to give the JIT
             * a better chance of optimizing a tight and common loop which
             * occurs when writing out ASCII characters.
             */
            char c;
            for(;i<n&&(c=chars[i])<0x80;i++)
                buf_loc[count_loc++]=(byte)c;
        }
        for(;i<n;i++){
            final char c=chars[i];
            if(c<0x80)
                buf_loc[count_loc++]=(byte)(c);
            else if(c<0x800){
                buf_loc[count_loc++]=(byte)(0xc0+(c>>6));
                buf_loc[count_loc++]=(byte)(0x80+(c&0x3f));
            }
            /**
             * The following else if condition is added to support XML 1.1 Characters for
             * UTF-8:   [1111 0uuu] [10uu zzzz] [10yy yyyy] [10xx xxxx]*
             * Unicode: [1101 10ww] [wwzz zzyy] (high surrogate)
             *          [1101 11yy] [yyxx xxxx] (low surrogate)
             *          * uuuuu = wwww + 1
             */
            else if(c>=0xD800&&c<=0xDBFF){
                char high, low;
                high=c;
                i++;
                low=chars[i];
                buf_loc[count_loc++]=(byte)(0xF0|(((high+0x40)>>8)&0xf0));
                buf_loc[count_loc++]=(byte)(0x80|(((high+0x40)>>2)&0x3f));
                buf_loc[count_loc++]=(byte)(0x80|((low>>6)&0x0f)+((high<<4)&0x30));
                buf_loc[count_loc++]=(byte)(0x80|(low&0x3f));
            }else{
                buf_loc[count_loc++]=(byte)(0xe0+(c>>12));
                buf_loc[count_loc++]=(byte)(0x80+((c>>6)&0x3f));
                buf_loc[count_loc++]=(byte)(0x80+(c&0x3f));
            }
        }
        // Store the local integer back into the instance variable
        count=count_loc;
    }

    public void flush() throws IOException{
        flushBuffer();
        m_os.flush();
    }

    public void close() throws IOException{
        flushBuffer();
        m_os.close();
    }

    public void flushBuffer() throws IOException{
        if(count>0){
            m_os.write(m_outputBytes,0,count);
            count=0;
        }
    }

    public Writer getWriter(){
        // Only one of getWriter() or getOutputStream() can return null
        // This type of writer wraps an OutputStream, not a Writer.
        return null;
    }

    public OutputStream getOutputStream(){
        return m_os;
    }
}
