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
 * $Id: CharInfo.java,v 1.2.4.1 2005/09/15 08:15:14 suresh_emailid Exp $
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
 * $Id: CharInfo.java,v 1.2.4.1 2005/09/15 08:15:14 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import com.sun.org.apache.xml.internal.serializer.utils.MsgKey;
import com.sun.org.apache.xml.internal.serializer.utils.SystemIDResolver;
import com.sun.org.apache.xml.internal.serializer.utils.Utils;
import com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException;

import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;

final class CharInfo{
    public static final String HTML_ENTITIES_RESOURCE=
            "com.sun.org.apache.xml.internal.serializer.HTMLEntities";
    public static final String XML_ENTITIES_RESOURCE=
            "com.sun.org.apache.xml.internal.serializer.XMLEntities";
    public static final char S_HORIZONAL_TAB=0x09;
    public static final char S_LINEFEED=0x0A;
    public static final char S_CARRIAGERETURN=0x0D;
    private static final int ASCII_MAX=128;
    // 5 for 32 bit words,  6 for 64 bit words ...
    private static final int SHIFT_PER_WORD=5;
    private static final int LOW_ORDER_BITMASK=0x1f;
    private static HashMap m_getCharInfoCache=new HashMap();
    final boolean onlyQuotAmpLtGt;
    private HashMap m_charToString=new HashMap();
    private boolean[] isSpecialAttrASCII=new boolean[ASCII_MAX];
    private boolean[] isSpecialTextASCII=new boolean[ASCII_MAX];
    private boolean[] isCleanTextASCII=new boolean[ASCII_MAX];
    private int firstWordNotUsed;
    private int array_of_bits[]=createEmptySetOfIntegers(65535);

    private CharInfo(String entitiesResource,String method){
        this(entitiesResource,method,false);
    }

    private CharInfo(String entitiesResource,String method,boolean internal){
        ResourceBundle entities=null;
        boolean noExtraEntities=true;
        // Make various attempts to interpret the parameter as a properties
        // file or resource file, as follows:
        //
        //   1) attempt to load .properties file using ResourceBundle
        //   2) try using the class loader to find the specified file a resource
        //      file
        //   3) try treating the resource a URI
        try{
            if(internal){
                // Load entity property files by using PropertyResourceBundle,
                // cause of security issure for applets
                entities=PropertyResourceBundle.getBundle(entitiesResource);
            }else{
                ClassLoader cl=SecuritySupport.getContextClassLoader();
                if(cl!=null){
                    entities=PropertyResourceBundle.getBundle(entitiesResource,
                            Locale.getDefault(),cl);
                }
            }
        }catch(Exception e){
        }
        if(entities!=null){
            Enumeration keys=entities.getKeys();
            while(keys.hasMoreElements()){
                String name=(String)keys.nextElement();
                String value=entities.getString(name);
                int code=Integer.parseInt(value);
                defineEntity(name,(char)code);
                if(extraEntity(code))
                    noExtraEntities=false;
            }
            set(S_LINEFEED);
            set(S_CARRIAGERETURN);
        }else{
            InputStream is=null;
            String err=null;
            // Load user specified resource file by using URL loading, it
            // requires a valid URI as parameter
            try{
                if(internal){
                    is=CharInfo.class.getResourceAsStream(entitiesResource);
                }else{
                    ClassLoader cl=SecuritySupport.getContextClassLoader();
                    if(cl!=null){
                        try{
                            is=cl.getResourceAsStream(entitiesResource);
                        }catch(Exception e){
                            err=e.getMessage();
                        }
                    }
                    if(is==null){
                        try{
                            URL url=new URL(entitiesResource);
                            is=url.openStream();
                        }catch(Exception e){
                            err=e.getMessage();
                        }
                    }
                }
                if(is==null){
                    throw new RuntimeException(
                            Utils.messages.createMessage(
                                    MsgKey.ER_RESOURCE_COULD_NOT_FIND,
                                    new Object[]{entitiesResource,err}));
                }
                // Fix Bugzilla#4000: force reading in UTF-8
                //  This creates the de facto standard that Xalan's resource
                //  files must be encoded in UTF-8. This should work in all
                // JVMs.
                //
                // %REVIEW% KNOWN ISSUE: IT FAILS IN MICROSOFT VJ++, which
                // didn't implement the UTF-8 encoding. Theoretically, we should
                // simply let it fail in that case, since the JVM is obviously
                // broken if it doesn't support such a basic standard.  But
                // since there are still some users attempting to use VJ++ for
                // development, we have dropped in a fallback which makes a
                // second attempt using the platform's default encoding. In VJ++
                // this is apparently ASCII, which is subset of UTF-8... and
                // since the strings we'll be reading here are also primarily
                // limited to the 7-bit ASCII range (at least, in English
                // versions of Xalan), this should work well enough to keep us
                // on the air until we're ready to officially decommit from
                // VJ++.
                BufferedReader reader;
                try{
                    reader=new BufferedReader(new InputStreamReader(is,"UTF-8"));
                }catch(UnsupportedEncodingException e){
                    reader=new BufferedReader(new InputStreamReader(is));
                }
                String line=reader.readLine();
                while(line!=null){
                    if(line.length()==0||line.charAt(0)=='#'){
                        line=reader.readLine();
                        continue;
                    }
                    int index=line.indexOf(' ');
                    if(index>1){
                        String name=line.substring(0,index);
                        ++index;
                        if(index<line.length()){
                            String value=line.substring(index);
                            index=value.indexOf(' ');
                            if(index>0){
                                value=value.substring(0,index);
                            }
                            int code=Integer.parseInt(value);
                            defineEntity(name,(char)code);
                            if(extraEntity(code))
                                noExtraEntities=false;
                        }
                    }
                    line=reader.readLine();
                }
                is.close();
                set(S_LINEFEED);
                set(S_CARRIAGERETURN);
            }catch(Exception e){
                throw new RuntimeException(
                        Utils.messages.createMessage(
                                MsgKey.ER_RESOURCE_COULD_NOT_LOAD,
                                new Object[]{entitiesResource,
                                        e.toString(),
                                        entitiesResource,
                                        e.toString()}));
            }finally{
                if(is!=null){
                    try{
                        is.close();
                    }catch(Exception except){
                    }
                }
            }
        }
        /** initialize the array isCleanTextASCII[] with a cache of values
         * for use by ToStream.character(char[], int , int)
         * and the array isSpecialTextASCII[] with the opposite values
         * (all in the name of performance!)
         */
        for(int ch=0;ch<ASCII_MAX;ch++)
            if((((0x20<=ch||(0x0A==ch||0x0D==ch||0x09==ch)))
                    &&(!get(ch)))||('"'==ch)){
                isCleanTextASCII[ch]=true;
                isSpecialTextASCII[ch]=false;
            }else{
                isCleanTextASCII[ch]=false;
                isSpecialTextASCII[ch]=true;
            }
        onlyQuotAmpLtGt=noExtraEntities;
        // initialize the array with a cache of the BitSet values
        for(int i=0;i<ASCII_MAX;i++)
            isSpecialAttrASCII[i]=get(i);
        /** Now that we've used get(ch) just above to initialize the
         * two arrays we will change by adding a tab to the set of
         * special chars for XML (but not HTML!).
         * We do this because a tab is always a
         * special character in an XML attribute,
         * but only a special character in XML text
         * if it has an entity defined for it.
         * This is the reason for this delay.
         */
        if(Method.XML.equals(method)){
            isSpecialAttrASCII[S_HORIZONAL_TAB]=true;
        }
    }

    static CharInfo getCharInfoInternal(String entitiesFileName,String method){
        CharInfo charInfo=(CharInfo)m_getCharInfoCache.get(entitiesFileName);
        if(charInfo!=null){
            return charInfo;
        }
        charInfo=new CharInfo(entitiesFileName,method,true);
        m_getCharInfoCache.put(entitiesFileName,charInfo);
        return charInfo;
    }

    static CharInfo getCharInfo(String entitiesFileName,String method){
        try{
            return new CharInfo(entitiesFileName,method,false);
        }catch(Exception e){
        }
        String absoluteEntitiesFileName;
        if(entitiesFileName.indexOf(':')<0){
            absoluteEntitiesFileName=
                    SystemIDResolver.getAbsoluteURIFromRelative(entitiesFileName);
        }else{
            try{
                absoluteEntitiesFileName=
                        SystemIDResolver.getAbsoluteURI(entitiesFileName,null);
            }catch(TransformerException te){
                throw new WrappedRuntimeException(te);
            }
        }
        return new CharInfo(absoluteEntitiesFileName,method,false);
    }

    private static int bit(int i){
        int ret=(1<<(i&LOW_ORDER_BITMASK));
        return ret;
    }

    private void defineEntity(String name,char value){
        StringBuilder sb=new StringBuilder("&");
        sb.append(name);
        sb.append(';');
        String entityString=sb.toString();
        defineChar2StringMapping(entityString,value);
    }

    String getOutputStringForChar(char value){
        CharKey charKey=new CharKey();
        charKey.setChar(value);
        return (String)m_charToString.get(charKey);
    }

    final boolean isSpecialAttrChar(int value){
        // for performance try the values in the boolean array first,
        // this is faster access than the BitSet for common ASCII values
        if(value<ASCII_MAX)
            return isSpecialAttrASCII[value];
        // rather than java.util.BitSet, our private
        // implementation is faster (and less general).
        return get(value);
    }

    private final boolean get(int i){
        boolean in_the_set=false;
        int j=(i>>SHIFT_PER_WORD); // wordIndex(i)
        // an optimization here, ... a quick test to see
        // if this integer is beyond any of the words in use
        if(j<firstWordNotUsed)
            in_the_set=(array_of_bits[j]&
                    (1<<(i&LOW_ORDER_BITMASK))
            )!=0;  // 0L for 64 bit words
        return in_the_set;
    }

    final boolean isSpecialTextChar(int value){
        // for performance try the values in the boolean array first,
        // this is faster access than the BitSet for common ASCII values
        if(value<ASCII_MAX)
            return isSpecialTextASCII[value];
        // rather than java.util.BitSet, our private
        // implementation is faster (and less general).
        return get(value);
    }

    final boolean isTextASCIIClean(int value){
        return isCleanTextASCII[value];
    }

    private int[] createEmptySetOfIntegers(int max){
        firstWordNotUsed=0; // an optimization
        int[] arr=new int[arrayIndex(max-1)+1];
        return arr;
    }

    private static int arrayIndex(int i){
        return (i>>SHIFT_PER_WORD);
    }

    private final void set(int i){
        setASCIIdirty(i);
        int j=(i>>SHIFT_PER_WORD); // this word is used
        int k=j+1;
        if(firstWordNotUsed<k) // for optimization purposes.
            firstWordNotUsed=k;
        array_of_bits[j]|=(1<<(i&LOW_ORDER_BITMASK));
    }

    // record if there are any entities other than
    // quot, amp, lt, gt  (probably user defined)
    private boolean extraEntity(int entityValue){
        boolean extra=false;
        if(entityValue<128){
            switch(entityValue){
                case 34: // quot
                case 38: // amp
                case 60: // lt
                case 62: // gt
                    break;
                default: // other entity in range 0 to 127
                    extra=true;
            }
        }
        return extra;
    }

    private void setASCIIdirty(int j){
        if(0<=j&&j<ASCII_MAX){
            isCleanTextASCII[j]=false;
            isSpecialTextASCII[j]=true;
        }
    }

    private void setASCIIclean(int j){
        if(0<=j&&j<ASCII_MAX){
            isCleanTextASCII[j]=true;
            isSpecialTextASCII[j]=false;
        }
    }

    private void defineChar2StringMapping(String outputString,char inputChar){
        CharKey character=new CharKey(inputChar);
        m_charToString.put(character,outputString);
        set(inputChar);
    }

    private static class CharKey extends Object{
        private char m_char;

        public CharKey(char key){
            m_char=key;
        }

        public CharKey(){
        }

        public final void setChar(char c){
            m_char=c;
        }

        public final int hashCode(){
            return (int)m_char;
        }

        public final boolean equals(Object obj){
            return ((CharKey)obj).m_char==m_char;
        }
    }
}
