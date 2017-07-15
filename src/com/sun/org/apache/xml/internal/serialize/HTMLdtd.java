/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Aug 21, 2000:
//   Fixed bug in isElement and made HTMLdtd public.
//   Contributed by Eric SCHAEFFER" <eschaeffer@posterconseil.com>
package com.sun.org.apache.xml.internal.serialize;

import com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class HTMLdtd{
    public static final String HTMLPublicId="-//W3C//DTD HTML 4.01//EN";
    public static final String HTMLSystemId=
            "http://www.w3.org/TR/html4/strict.dtd";
    public static final String XHTMLPublicId=
            "-//W3C//DTD XHTML 1.0 Strict//EN";
    public static final String XHTMLSystemId=
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";
    private static final Map<String,String[]> _boolAttrs;
    private static final Map<String,Integer> _elemDefs;
    private static final String ENTITIES_RESOURCE="HTMLEntities.res";
    private static final int ONLY_OPENING=0x0001;
    private static final int ELEM_CONTENT=0x0002;
    private static final int PRESERVE=0x0004;
    private static final int OPT_CLOSING=0x0008;
    private static final int EMPTY=0x0010|ONLY_OPENING;
    private static final int ALLOWED_HEAD=0x0020;
    private static final int CLOSE_P=0x0040;
    private static final int CLOSE_DD_DT=0x0080;
    private static final int CLOSE_SELF=0x0100;
    private static final int CLOSE_TABLE=0x0200;
    private static final int CLOSE_TH_TD=0x04000;
    private static Map<Integer,String> _byChar;
    private static Map<String,Integer> _byName;

    static{
        _elemDefs=new HashMap<>();
        defineElement("ADDRESS",CLOSE_P);
        defineElement("AREA",EMPTY);
        defineElement("BASE",EMPTY|ALLOWED_HEAD);
        defineElement("BASEFONT",EMPTY);
        defineElement("BLOCKQUOTE",CLOSE_P);
        defineElement("BODY",OPT_CLOSING);
        defineElement("BR",EMPTY);
        defineElement("COL",EMPTY);
        defineElement("COLGROUP",ELEM_CONTENT|OPT_CLOSING|CLOSE_TABLE);
        defineElement("DD",OPT_CLOSING|ONLY_OPENING|CLOSE_DD_DT);
        defineElement("DIV",CLOSE_P);
        defineElement("DL",ELEM_CONTENT|CLOSE_P);
        defineElement("DT",OPT_CLOSING|ONLY_OPENING|CLOSE_DD_DT);
        defineElement("FIELDSET",CLOSE_P);
        defineElement("FORM",CLOSE_P);
        defineElement("FRAME",EMPTY|OPT_CLOSING);
        defineElement("H1",CLOSE_P);
        defineElement("H2",CLOSE_P);
        defineElement("H3",CLOSE_P);
        defineElement("H4",CLOSE_P);
        defineElement("H5",CLOSE_P);
        defineElement("H6",CLOSE_P);
        defineElement("HEAD",ELEM_CONTENT|OPT_CLOSING);
        defineElement("HR",EMPTY|CLOSE_P);
        defineElement("HTML",ELEM_CONTENT|OPT_CLOSING);
        defineElement("IMG",EMPTY);
        defineElement("INPUT",EMPTY);
        defineElement("ISINDEX",EMPTY|ALLOWED_HEAD);
        defineElement("LI",OPT_CLOSING|ONLY_OPENING|CLOSE_SELF);
        defineElement("LINK",EMPTY|ALLOWED_HEAD);
        defineElement("MAP",ALLOWED_HEAD);
        defineElement("META",EMPTY|ALLOWED_HEAD);
        defineElement("OL",ELEM_CONTENT|CLOSE_P);
        defineElement("OPTGROUP",ELEM_CONTENT);
        defineElement("OPTION",OPT_CLOSING|ONLY_OPENING|CLOSE_SELF);
        defineElement("P",OPT_CLOSING|CLOSE_P|CLOSE_SELF);
        defineElement("PARAM",EMPTY);
        defineElement("PRE",PRESERVE|CLOSE_P);
        defineElement("SCRIPT",ALLOWED_HEAD|PRESERVE);
        defineElement("NOSCRIPT",ALLOWED_HEAD|PRESERVE);
        defineElement("SELECT",ELEM_CONTENT);
        defineElement("STYLE",ALLOWED_HEAD|PRESERVE);
        defineElement("TABLE",ELEM_CONTENT|CLOSE_P);
        defineElement("TBODY",ELEM_CONTENT|OPT_CLOSING|CLOSE_TABLE);
        defineElement("TD",OPT_CLOSING|CLOSE_TH_TD);
        defineElement("TEXTAREA",PRESERVE);
        defineElement("TFOOT",ELEM_CONTENT|OPT_CLOSING|CLOSE_TABLE);
        defineElement("TH",OPT_CLOSING|CLOSE_TH_TD);
        defineElement("THEAD",ELEM_CONTENT|OPT_CLOSING|CLOSE_TABLE);
        defineElement("TITLE",ALLOWED_HEAD);
        defineElement("TR",ELEM_CONTENT|OPT_CLOSING|CLOSE_TABLE);
        defineElement("UL",ELEM_CONTENT|CLOSE_P);
        _boolAttrs=new HashMap<>();
        defineBoolean("AREA","href");
        defineBoolean("BUTTON","disabled");
        defineBoolean("DIR","compact");
        defineBoolean("DL","compact");
        defineBoolean("FRAME","noresize");
        defineBoolean("HR","noshade");
        defineBoolean("IMAGE","ismap");
        defineBoolean("INPUT",new String[]{"defaultchecked","checked","readonly","disabled"});
        defineBoolean("LINK","link");
        defineBoolean("MENU","compact");
        defineBoolean("OBJECT","declare");
        defineBoolean("OL","compact");
        defineBoolean("OPTGROUP","disabled");
        defineBoolean("OPTION",new String[]{"default-selected","selected","disabled"});
        defineBoolean("SCRIPT","defer");
        defineBoolean("SELECT",new String[]{"multiple","disabled"});
        defineBoolean("STYLE","disabled");
        defineBoolean("TD","nowrap");
        defineBoolean("TH","nowrap");
        defineBoolean("TEXTAREA",new String[]{"disabled","readonly"});
        defineBoolean("UL","compact");
        initialize();
    }

    public static boolean isEmptyTag(String tagName){
        return isElement(tagName,EMPTY);
    }

    private static boolean isElement(String name,int flag){
        Integer flags;
        flags=_elemDefs.get(name.toUpperCase(Locale.ENGLISH));
        if(flags==null)
            return false;
        else
            return ((flags.intValue()&flag)==flag);
    }

    public static boolean isElementContent(String tagName){
        return isElement(tagName,ELEM_CONTENT);
    }

    public static boolean isPreserveSpace(String tagName){
        return isElement(tagName,PRESERVE);
    }

    public static boolean isOptionalClosing(String tagName){
        return isElement(tagName,OPT_CLOSING);
    }

    public static boolean isOnlyOpening(String tagName){
        return isElement(tagName,ONLY_OPENING);
    }

    public static boolean isClosing(String tagName,String openTag){
        // Several elements are defined as closing the HEAD
        if(openTag.equalsIgnoreCase("HEAD"))
            return !isElement(tagName,ALLOWED_HEAD);
        // P closes iteself
        if(openTag.equalsIgnoreCase("P"))
            return isElement(tagName,CLOSE_P);
        // DT closes DD, DD closes DT
        if(openTag.equalsIgnoreCase("DT")||openTag.equalsIgnoreCase("DD"))
            return isElement(tagName,CLOSE_DD_DT);
        // LI and OPTION close themselves
        if(openTag.equalsIgnoreCase("LI")||openTag.equalsIgnoreCase("OPTION"))
            return isElement(tagName,CLOSE_SELF);
        // Each of these table sections closes all the others
        if(openTag.equalsIgnoreCase("THEAD")||openTag.equalsIgnoreCase("TFOOT")||
                openTag.equalsIgnoreCase("TBODY")||openTag.equalsIgnoreCase("TR")||
                openTag.equalsIgnoreCase("COLGROUP"))
            return isElement(tagName,CLOSE_TABLE);
        // TD closes TH and TH closes TD
        if(openTag.equalsIgnoreCase("TH")||openTag.equalsIgnoreCase("TD"))
            return isElement(tagName,CLOSE_TH_TD);
        return false;
    }

    public static boolean isURI(String tagName,String attrName){
        // Stupid checks.
        return (attrName.equalsIgnoreCase("href")||attrName.equalsIgnoreCase("src"));
    }

    public static boolean isBoolean(String tagName,String attrName){
        String[] attrNames;
        attrNames=_boolAttrs.get(tagName.toUpperCase(Locale.ENGLISH));
        if(attrNames==null)
            return false;
        for(int i=0;i<attrNames.length;++i)
            if(attrNames[i].equalsIgnoreCase(attrName))
                return true;
        return false;
    }

    public static int charFromName(String name){
        Object value;
        initialize();
        value=_byName.get(name);
        if(value!=null&&value instanceof Integer)
            return ((Integer)value).intValue();
        else
            return -1;
    }

    private static void initialize(){
        InputStream is=null;
        BufferedReader reader=null;
        int index;
        String name;
        String value;
        int code;
        String line;
        // Make sure not to initialize twice.
        if(_byName!=null)
            return;
        try{
            _byName=new HashMap<>();
            _byChar=new HashMap<>();
            is=HTMLdtd.class.getResourceAsStream(ENTITIES_RESOURCE);
            if(is==null){
                throw new RuntimeException(
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.SERIALIZER_DOMAIN,
                                "ResourceNotFound",new Object[]{ENTITIES_RESOURCE}));
            }
            reader=new BufferedReader(new InputStreamReader(is,"ASCII"));
            line=reader.readLine();
            while(line!=null){
                if(line.length()==0||line.charAt(0)=='#'){
                    line=reader.readLine();
                    continue;
                }
                index=line.indexOf(' ');
                if(index>1){
                    name=line.substring(0,index);
                    ++index;
                    if(index<line.length()){
                        value=line.substring(index);
                        index=value.indexOf(' ');
                        if(index>0)
                            value=value.substring(0,index);
                        code=Integer.parseInt(value);
                        defineEntity(name,(char)code);
                    }
                }
                line=reader.readLine();
            }
            is.close();
        }catch(Exception except){
            throw new RuntimeException(
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.SERIALIZER_DOMAIN,
                            "ResourceNotLoaded",new Object[]{ENTITIES_RESOURCE,except.toString()}));
        }finally{
            if(is!=null){
                try{
                    is.close();
                }catch(Exception except){
                }
            }
        }
    }

    private static void defineEntity(String name,char value){
        if(_byName.get(name)==null){
            _byName.put(name,new Integer(value));
            _byChar.put(new Integer(value),name);
        }
    }

    public static String fromChar(int value){
        if(value>0xffff)
            return null;
        String name;
        initialize();
        name=_byChar.get(value);
        return name;
    }

    private static void defineElement(String name,int flags){
        _elemDefs.put(name,flags);
    }

    private static void defineBoolean(String tagName,String attrName){
        defineBoolean(tagName,new String[]{attrName});
    }

    private static void defineBoolean(String tagName,String[] attrNames){
        _boolAttrs.put(tagName,attrNames);
    }
}
