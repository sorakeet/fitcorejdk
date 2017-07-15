/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * $Id: EncodingInfo.java,v 1.2.4.2 2005/09/15 12:01:24 suresh_emailid Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * $Id: EncodingInfo.java,v 1.2.4.2 2005/09/15 12:01:24 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

public final class EncodingInfo extends Object{
    final String name;
    final String javaName;
    private InEncoding m_encoding;

    public EncodingInfo(String name,String javaName){
        this.name=name;
        this.javaName=javaName;
    }

    private static boolean inEncoding(char ch,String encoding){
        boolean isInEncoding;
        try{
            char cArray[]=new char[1];
            cArray[0]=ch;
            // Construct a String from the char
            String s=new String(cArray);
            // Encode the String into a sequence of bytes
            // using the given, named charset.
            byte[] bArray=s.getBytes(encoding);
            isInEncoding=inEncoding(ch,bArray);
        }catch(Exception e){
            isInEncoding=false;
            // If for some reason the encoding is null, e.g.
            // for a temporary result tree, we should just
            // say that every character is in the encoding.
            if(encoding==null)
                isInEncoding=true;
        }
        return isInEncoding;
    }

    private static boolean inEncoding(char ch,byte[] data){
        final boolean isInEncoding;
        // If the string written out as data is not in the encoding,
        // the output is not specified according to the documentation
        // on the String.getBytes(encoding) method,
        // but we do our best here.
        if(data==null||data.length==0){
            isInEncoding=false;
        }else{
            if(data[0]==0)
                isInEncoding=false;
            else if(data[0]=='?'&&ch!='?')
                isInEncoding=false;
            /**
             * else if (isJapanese) {
             *   // isJapanese is really
             *   //   (    "EUC-JP".equals(javaName)
             *   //    ||  "EUC_JP".equals(javaName)
             *  //     ||  "SJIS".equals(javaName)   )
             *
             *   // Work around some bugs in JRE for Japanese
             *   if(data[0] == 0x21)
             *     isInEncoding = false;
             *   else if (ch == 0xA5)
             *     isInEncoding = false;
             *   else
             *     isInEncoding = true;
             * }
             */
            else{
                // We don't know for sure, but it looks like it is in the encoding
                isInEncoding=true;
            }
        }
        return isInEncoding;
    }

    private static boolean inEncoding(char high,char low,String encoding){
        boolean isInEncoding;
        try{
            char cArray[]=new char[2];
            cArray[0]=high;
            cArray[1]=low;
            // Construct a String from the char
            String s=new String(cArray);
            // Encode the String into a sequence of bytes
            // using the given, named charset.
            byte[] bArray=s.getBytes(encoding);
            isInEncoding=inEncoding(high,bArray);
        }catch(Exception e){
            isInEncoding=false;
        }
        return isInEncoding;
    }

    public boolean isInEncoding(char ch){
        if(m_encoding==null){
            m_encoding=new EncodingImpl();
            // One could put alternate logic in here to
            // instantiate another object that implements the
            // InEncoding interface. For example if the JRE is 1.4 or up
            // we could have an object that uses JRE 1.4 methods
        }
        return m_encoding.isInEncoding(ch);
    }

    public boolean isInEncoding(char high,char low){
        if(m_encoding==null){
            m_encoding=new EncodingImpl();
            // One could put alternate logic in here to
            // instantiate another object that implements the
            // InEncoding interface. For example if the JRE is 1.4 or up
            // we could have an object that uses JRE 1.4 methods
        }
        return m_encoding.isInEncoding(high,low);
    }

    private interface InEncoding{
        public boolean isInEncoding(char ch);

        public boolean isInEncoding(char high,char low);
    }

    private class EncodingImpl implements InEncoding{
        private static final int RANGE=128;
        final private String m_encoding;
        final private int m_first;
        final private int m_explFirst;
        final private int m_explLast;
        final private int m_last;
        final private boolean m_alreadyKnown[]=new boolean[RANGE];
        final private boolean m_isInEncoding[]=new boolean[RANGE];
        private InEncoding m_before;
        private InEncoding m_after;
        private EncodingImpl(){
            // This object will answer whether any unicode value
            // is in the encoding, it handles values 0 through Integer.MAX_VALUE
            this(javaName,0,Integer.MAX_VALUE,(char)0);
        }
        private EncodingImpl(String encoding,int first,int last,int codePoint){
            // Set the range of unicode values that this object manages
            // either explicitly or implicitly.
            m_first=first;
            m_last=last;
            // Set the range of unicode values that this object
            // explicitly manages. Align the explicitly managed values
            // to RANGE so multiple EncodingImpl objects dont manage the same
            // values.
            m_explFirst=codePoint/RANGE*RANGE;
            m_explLast=m_explFirst+(RANGE-1);
            m_encoding=encoding;
            if(javaName!=null){
                // Some optimization.
                if(0<=m_explFirst&&m_explFirst<=127){
                    // This particular EncodingImpl explicitly handles
                    // characters in the low range.
                    if("UTF8".equals(javaName)
                            ||"UTF-16".equals(javaName)
                            ||"ASCII".equals(javaName)
                            ||"US-ASCII".equals(javaName)
                            ||"Unicode".equals(javaName)
                            ||"UNICODE".equals(javaName)
                            ||javaName.startsWith("ISO8859")){
                        // Not only does this EncodingImpl object explicitly
                        // handle chracters in the low range, it is
                        // also one that we know something about, without
                        // needing to call inEncoding(char ch, String encoding)
                        // for this low range
                        //
                        // By initializing the table ahead of time
                        // for these low values, we prevent the expensive
                        // inEncoding(char ch, String encoding)
                        // from being called, at least for these common
                        // encodings.
                        for(int unicode=1;unicode<127;unicode++){
                            final int idx=unicode-m_explFirst;
                            if(0<=idx&&idx<RANGE){
                                m_alreadyKnown[idx]=true;
                                m_isInEncoding[idx]=true;
                            }
                        }
                    }
                }
                /** A little bit more than optimization.
                 *
                 * We will say that any character is in the encoding if
                 * we don't have an encoding.
                 * This is meaningful when the serializer is being used
                 * in temporary output state, where we are not writing to
                 * the final output tree.  It is when writing to the
                 * final output tree that we need to worry about the output
                 * encoding
                 */
                if(javaName==null){
                    for(int idx=0;idx<m_alreadyKnown.length;idx++){
                        m_alreadyKnown[idx]=true;
                        m_isInEncoding[idx]=true;
                    }
                }
            }
        }

        public boolean isInEncoding(char ch1){
            final boolean ret;
            int codePoint=Encodings.toCodePoint(ch1);
            if(codePoint<m_explFirst){
                // The unicode value is before the range
                // that we explictly manage, so we delegate the answer.
                // If we don't have an m_before object to delegate to, make one.
                if(m_before==null)
                    m_before=
                            new EncodingImpl(
                                    m_encoding,
                                    m_first,
                                    m_explFirst-1,
                                    codePoint);
                ret=m_before.isInEncoding(ch1);
            }else if(m_explLast<codePoint){
                // The unicode value is after the range
                // that we explictly manage, so we delegate the answer.
                // If we don't have an m_after object to delegate to, make one.
                if(m_after==null)
                    m_after=
                            new EncodingImpl(
                                    m_encoding,
                                    m_explLast+1,
                                    m_last,
                                    codePoint);
                ret=m_after.isInEncoding(ch1);
            }else{
                // The unicode value is in the range we explitly handle
                final int idx=codePoint-m_explFirst;
                // If we already know the answer, just return it.
                if(m_alreadyKnown[idx])
                    ret=m_isInEncoding[idx];
                else{
                    // We don't know the answer, so find out,
                    // which may be expensive, then cache the answer
                    ret=inEncoding(ch1,m_encoding);
                    m_alreadyKnown[idx]=true;
                    m_isInEncoding[idx]=ret;
                }
            }
            return ret;
        }

        public boolean isInEncoding(char high,char low){
            final boolean ret;
            int codePoint=Encodings.toCodePoint(high,low);
            if(codePoint<m_explFirst){
                // The unicode value is before the range
                // that we explictly manage, so we delegate the answer.
                // If we don't have an m_before object to delegate to, make one.
                if(m_before==null)
                    m_before=
                            new EncodingImpl(
                                    m_encoding,
                                    m_first,
                                    m_explFirst-1,
                                    codePoint);
                ret=m_before.isInEncoding(high,low);
            }else if(m_explLast<codePoint){
                // The unicode value is after the range
                // that we explictly manage, so we delegate the answer.
                // If we don't have an m_after object to delegate to, make one.
                if(m_after==null)
                    m_after=
                            new EncodingImpl(
                                    m_encoding,
                                    m_explLast+1,
                                    m_last,
                                    codePoint);
                ret=m_after.isInEncoding(high,low);
            }else{
                // The unicode value is in the range we explitly handle
                final int idx=codePoint-m_explFirst;
                // If we already know the answer, just return it.
                if(m_alreadyKnown[idx])
                    ret=m_isInEncoding[idx];
                else{
                    // We don't know the answer, so find out,
                    // which may be expensive, then cache the answer
                    ret=inEncoding(high,low,m_encoding);
                    m_alreadyKnown[idx]=true;
                    m_isInEncoding[idx]=ret;
                }
            }
            return ret;
        }
    }
}
