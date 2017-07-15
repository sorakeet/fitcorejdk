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
 * $Id: Encodings.java,v 1.3 2005/09/28 13:49:04 pvedula Exp $
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
 * $Id: Encodings.java,v 1.3 2005/09/28 13:49:04 pvedula Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import com.sun.org.apache.xalan.internal.utils.SecuritySupport;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;
import java.util.Map.Entry;

public final class Encodings extends Object{
    static final String DEFAULT_MIME_ENCODING="UTF-8";
    private static final int m_defaultLastPrintable=0x7F;
    private static final String ENCODINGS_FILE="com/sun/org/apache/xml/internal/serializer/Encodings.properties";
    private static final String ENCODINGS_PROP="com.sun.org.apache.xalan.internal.serialize.encodings";
    private final static EncodingInfos _encodingInfos=new EncodingInfos();

    static Writer getWriter(OutputStream output,String encoding)
            throws UnsupportedEncodingException{
        final EncodingInfo ei=_encodingInfos.findEncoding(toUpperCaseFast(encoding));
        if(ei!=null){
            try{
                return new BufferedWriter(new OutputStreamWriter(
                        output,ei.javaName));
            }catch(UnsupportedEncodingException usee){
                // keep trying
            }
        }
        return new BufferedWriter(new OutputStreamWriter(output,encoding));
    }

    static private String toUpperCaseFast(final String s){
        boolean different=false;
        final int mx=s.length();
        char[] chars=new char[mx];
        for(int i=0;i<mx;i++){
            char ch=s.charAt(i);
            // is the character a lower case ASCII one?
            if('a'<=ch&&ch<='z'){
                // a cheap and fast way to uppercase that is good enough
                ch=(char)(ch+('A'-'a'));
                different=true; // the uppercased String is different
            }
            chars[i]=ch;
        }
        // A little optimization, don't call String.valueOf() if
        // the uppercased string is the same as the input string.
        final String upper;
        if(different)
            upper=String.valueOf(chars);
        else
            upper=s;
        return upper;
    }

    public static int getLastPrintable(){
        return m_defaultLastPrintable;
    }

    static EncodingInfo getEncodingInfo(String encoding){
        EncodingInfo ei;
        String normalizedEncoding=toUpperCaseFast(encoding);
        ei=_encodingInfos.findEncoding(normalizedEncoding);
        if(ei==null){
            // We shouldn't have to do this, but just in case.
            try{
                // This may happen if the caller tries to use
                // an encoding that wasn't registered in the
                // (java name)->(preferred mime name) mapping file.
                // In that case we attempt to load the charset for the
                // given encoding, and if that succeeds - we create a new
                // EncodingInfo instance - assuming the canonical name
                // of the charset can be used as the mime name.
                final Charset c=Charset.forName(encoding);
                final String name=c.name();
                ei=new EncodingInfo(name,name);
                _encodingInfos.putEncoding(normalizedEncoding,ei);
            }catch(IllegalCharsetNameException|UnsupportedCharsetException x){
                ei=new EncodingInfo(null,null);
            }
        }
        return ei;
    }

    static String getMimeEncoding(String encoding){
        if(null==encoding){
            try{
                // Get the default system character encoding.  This may be
                // incorrect if they passed in a writer, but right now there
                // seems to be no way to get the encoding from a writer.
                encoding=SecuritySupport.getSystemProperty("file.encoding","UTF8");
                if(null!=encoding){
                    /**
                     * See if the mime type is equal to UTF8.  If you don't
                     * do that, then  convertJava2MimeEncoding will convert
                     * 8859_1 to "ISO-8859-1", which is not what we want,
                     * I think, and I don't think I want to alter the tables
                     * to convert everything to UTF-8.
                     */
                    String jencoding=
                            (encoding.equalsIgnoreCase("Cp1252")
                                    ||encoding.equalsIgnoreCase("ISO8859_1")
                                    ||encoding.equalsIgnoreCase("8859_1")
                                    ||encoding.equalsIgnoreCase("UTF8"))
                                    ?DEFAULT_MIME_ENCODING
                                    :convertJava2MimeEncoding(encoding);
                    encoding=
                            (null!=jencoding)?jencoding:DEFAULT_MIME_ENCODING;
                }else{
                    encoding=DEFAULT_MIME_ENCODING;
                }
            }catch(SecurityException se){
                encoding=DEFAULT_MIME_ENCODING;
            }
        }else{
            encoding=convertJava2MimeEncoding(encoding);
        }
        return encoding;
    }

    private static String convertJava2MimeEncoding(String encoding){
        final EncodingInfo enc=
                _encodingInfos.getEncodingFromJavaKey(toUpperCaseFast(encoding));
        if(null!=enc)
            return enc.name;
        return encoding;
    }

    public static String convertMime2JavaEncoding(String encoding){
        final EncodingInfo info=_encodingInfos.findEncoding(toUpperCaseFast(encoding));
        return info!=null?info.javaName:encoding;
    }

    static boolean isHighUTF16Surrogate(char ch){
        return ('\uD800'<=ch&&ch<='\uDBFF');
    }

    static boolean isLowUTF16Surrogate(char ch){
        return ('\uDC00'<=ch&&ch<='\uDFFF');
    }

    static int toCodePoint(char highSurrogate,char lowSurrogate){
        int codePoint=
                ((highSurrogate-0xd800)<<10)
                        +(lowSurrogate-0xdc00)
                        +0x10000;
        return codePoint;
    }

    static int toCodePoint(char ch){
        int codePoint=ch;
        return codePoint;
    }

    // Using an inner static class here prevent initialization races
    // where the hash maps could be used before they were populated.
    //
    private final static class EncodingInfos{
        // These maps are final and not modified after initialization.
        private final Map<String,EncodingInfo> _encodingTableKeyJava=new HashMap<>();
        private final Map<String,EncodingInfo> _encodingTableKeyMime=new HashMap<>();
        // This map will be added to after initialization: make sure it's
        // thread-safe. This map should not be used frequently - only in cases
        // where the mapping requested was not declared in the Encodings.properties
        // file.
        private final Map<String,EncodingInfo> _encodingDynamicTable=
                Collections.synchronizedMap(new HashMap<String,EncodingInfo>());

        private EncodingInfos(){
            loadEncodingInfo();
        }

        private void loadEncodingInfo(){
            try{
                // load (java name)->(preferred mime name) mapping.
                final Properties props=loadProperties();
                // create instances of EncodingInfo from the loaded mapping
                Enumeration keys=props.keys();
                Map<String,EncodingInfo> canonicals=new HashMap<>();
                while(keys.hasMoreElements()){
                    final String javaName=(String)keys.nextElement();
                    final String[] mimes=parseMimeTypes(props.getProperty(javaName));
                    final String charsetName=findCharsetNameFor(javaName,mimes);
                    if(charsetName!=null){
                        final String kj=toUpperCaseFast(javaName);
                        final String kc=toUpperCaseFast(charsetName);
                        for(int i=0;i<mimes.length;++i){
                            final String mimeName=mimes[i];
                            final String km=toUpperCaseFast(mimeName);
                            EncodingInfo info=new EncodingInfo(mimeName,charsetName);
                            _encodingTableKeyMime.put(km,info);
                            if(!canonicals.containsKey(kc)){
                                // canonicals will map the charset name to
                                //   the info containing the prefered mime name
                                //   (the preferred mime name is the first mime
                                //   name in the list).
                                canonicals.put(kc,info);
                                _encodingTableKeyJava.put(kc,info);
                            }
                            _encodingTableKeyJava.put(kj,info);
                        }
                    }else{
                        // None of the java or mime names on the line were
                        // recognized => this charset is not supported?
                    }
                }
                // Fix up the _encodingTableKeyJava so that the info mapped to
                // the java name contains the preferred mime name.
                // (a given java name can correspond to several mime name,
                //  but we want the _encodingTableKeyJava to point to the
                //  preferred mime name).
                for(Entry<String,EncodingInfo> e : _encodingTableKeyJava.entrySet()){
                    e.setValue(canonicals.get(toUpperCaseFast(e.getValue().javaName)));
                }
            }catch(MalformedURLException mue){
                throw new com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException(mue);
            }catch(IOException ioe){
                throw new com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException(ioe);
            }
        }

        // Loads the Properties resource containing the mapping:
        //    java charset name -> preferred mime name
        // and returns it.
        private Properties loadProperties() throws MalformedURLException, IOException{
            Properties props=new Properties();
            try(InputStream is=openEncodingsFileStream()){
                if(is!=null){
                    props.load(is);
                }else{
                    // Seems to be no real need to force failure here, let the
                    // system do its best... The issue is not really very critical,
                    // and the output will be in any case _correct_ though maybe not
                    // always human-friendly... :)
                    // But maybe report/log the resource problem?
                    // Any standard ways to report/log errors (in static context)?
                }
            }
            return props;
        }

        // Opens the file/resource containing java charset name -> preferred mime
        // name mapping and returns it as an InputStream.
        private InputStream openEncodingsFileStream() throws MalformedURLException, IOException{
            String urlString=null;
            InputStream is=null;
            try{
                urlString=SecuritySupport.getSystemProperty(ENCODINGS_PROP,"");
            }catch(SecurityException e){
            }
            if(urlString!=null&&urlString.length()>0){
                URL url=new URL(urlString);
                is=url.openStream();
            }
            if(is==null){
                is=SecuritySupport.getResourceAsStream(ENCODINGS_FILE);
            }
            return is;
        }

        // Parses the mime list associated to a java charset name.
        // The first mime name in the list is supposed to be the preferred
        // mime name.
        private String[] parseMimeTypes(String val){
            int pos=val.indexOf(' ');
            //int lastPrintable;
            if(pos<0){
                // Maybe report/log this problem?
                //  "Last printable character not defined for encoding " +
                //  mimeName + " (" + val + ")" ...
                return new String[]{val};
                //lastPrintable = 0x00FF;
            }
            //lastPrintable =
            //    Integer.decode(val.substring(pos).trim()).intValue();
            StringTokenizer st=
                    new StringTokenizer(val.substring(0,pos),",");
            String[] values=new String[st.countTokens()];
            for(int i=0;st.hasMoreTokens();i++){
                values[i]=st.nextToken();
            }
            return values;
        }

        // This method here attempts to find the canonical charset name for the
        // the set javaName+mimeNames - which are supposed to all refer to the
        // same charset.
        // For that it attempts to load the charset using the javaName, and if
        // not found, attempts again using each of the mime names in turn.
        // If the charset could be loaded from the javaName, then the javaName
        // itself is returned as charset name. Otherwise, each of the mime names
        // is tried in turn, until a charset can be loaded from one of the names,
        // and the loaded charset's canonical name is returned.
        // If no charset can be loaded from either the javaName or one of the
        // mime names, then null is returned.
        //
        // Note that the returned name is the 'java' name that will be used in
        // instances of EncodingInfo.
        // This is important because EncodingInfo uses that 'java name' later on
        // in calls to String.getBytes(javaName).
        // As it happens, sometimes only one element of the set mime names/javaName
        // is known by Charset: sometimes only one of the mime names is known,
        // sometime only the javaName is known, sometimes all are known.
        //
        // By using this method here, we fix the problem where one of the mime
        // names is known but the javaName is unknown, by associating the charset
        // loaded from one of the mime names with the unrecognized javaName.
        //
        // When none of the mime names or javaName are known - there's not much we can
        // do... It can mean that this encoding is not supported for this
        // OS. If such a charset is ever use it will result in having all characters
        // escaped.
        //
        private String findCharsetNameFor(String javaName,String[] mimes){
            String cs=findCharsetNameFor(javaName);
            if(cs!=null) return javaName;
            for(String m : mimes){
                cs=findCharsetNameFor(m);
                if(cs!=null) break;
            }
            return cs;
        }

        // This method here attempts to find the canonical charset name for the
        // the given name - which is supposed to be either a java name or a mime
        // name.
        // For that, it attempts to load the charset using the given name, and
        // then returns the charset's canonical name.
        // If the charset could not be loaded from the given name,
        // the method returns null.
        private String findCharsetNameFor(String name){
            try{
                return Charset.forName(name).name();
            }catch(Exception x){
                return null;
            }
        }

        EncodingInfo findEncoding(String normalizedEncoding){
            EncodingInfo info=_encodingTableKeyJava.get(normalizedEncoding);
            if(info==null){
                info=_encodingTableKeyMime.get(normalizedEncoding);
            }
            if(info==null){
                info=_encodingDynamicTable.get(normalizedEncoding);
            }
            return info;
        }

        EncodingInfo getEncodingFromMimeKey(String normalizedMimeName){
            return _encodingTableKeyMime.get(normalizedMimeName);
        }

        EncodingInfo getEncodingFromJavaKey(String normalizedJavaName){
            return _encodingTableKeyJava.get(normalizedJavaName);
        }

        void putEncoding(String key,EncodingInfo info){
            _encodingDynamicTable.put(key,info);
        }
    }
}
