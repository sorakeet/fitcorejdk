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
 * $Id: ExsltStrings.java,v 1.1.2.1 2005/08/01 02:08:48 jeffsuttor Exp $
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
 * $Id: ExsltStrings.java,v 1.1.2.1 2005/08/01 02:08:48 jeffsuttor Exp $
 */
package com.sun.org.apache.xalan.internal.lib;

import com.sun.org.apache.xpath.internal.NodeSet;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.StringTokenizer;

public class ExsltStrings extends ExsltBase{
    static final String JDK_DEFAULT_DOM="com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl";

    public static String align(String targetStr,String paddingStr){
        return align(targetStr,paddingStr,"left");
    }

    public static String align(String targetStr,String paddingStr,String type){
        if(targetStr.length()>=paddingStr.length())
            return targetStr.substring(0,paddingStr.length());
        if(type.equals("right")){
            return paddingStr.substring(0,paddingStr.length()-targetStr.length())+targetStr;
        }else if(type.equals("center")){
            int startIndex=(paddingStr.length()-targetStr.length())/2;
            return paddingStr.substring(0,startIndex)+targetStr+paddingStr.substring(startIndex+targetStr.length());
        }
        // Default is left
        else{
            return targetStr+paddingStr.substring(targetStr.length());
        }
    }

    public static String concat(NodeList nl){
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<nl.getLength();i++){
            Node node=nl.item(i);
            String value=toString(node);
            if(value!=null&&value.length()>0)
                sb.append(value);
        }
        return sb.toString();
    }

    public static String padding(double length){
        return padding(length," ");
    }

    public static String padding(double length,String pattern){
        if(pattern==null||pattern.length()==0)
            return "";
        StringBuffer sb=new StringBuffer();
        int len=(int)length;
        int numAdded=0;
        int index=0;
        while(numAdded<len){
            if(index==pattern.length())
                index=0;
            sb.append(pattern.charAt(index));
            index++;
            numAdded++;
        }
        return sb.toString();
    }

    public static NodeList split(String str){
        return split(str," ");
    }

    public static NodeList split(String str,String pattern){
        NodeSet resultSet=new NodeSet();
        resultSet.setShouldCacheNodes(true);
        boolean done=false;
        int fromIndex=0;
        int matchIndex=0;
        String token=null;
        while(!done&&fromIndex<str.length()){
            matchIndex=str.indexOf(pattern,fromIndex);
            if(matchIndex>=0){
                token=str.substring(fromIndex,matchIndex);
                fromIndex=matchIndex+pattern.length();
            }else{
                done=true;
                token=str.substring(fromIndex);
            }
            Document doc=getDocument();
            synchronized(doc){
                Element element=doc.createElement("token");
                Text text=doc.createTextNode(token);
                element.appendChild(text);
                resultSet.addNode(element);
            }
        }
        return resultSet;
    }

    private static Document getDocument(){
        try{
            if(System.getSecurityManager()==null){
                return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            }else{
                return DocumentBuilderFactory.newInstance(JDK_DEFAULT_DOM,null).newDocumentBuilder().newDocument();
            }
        }catch(ParserConfigurationException pce){
            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(pce);
        }
    }

    public static NodeList tokenize(String toTokenize){
        return tokenize(toTokenize," \t\n\r");
    }

    public static NodeList tokenize(String toTokenize,String delims){
        NodeSet resultSet=new NodeSet();
        if(delims!=null&&delims.length()>0){
            StringTokenizer lTokenizer=new StringTokenizer(toTokenize,delims);
            Document doc=getDocument();
            synchronized(doc){
                while(lTokenizer.hasMoreTokens()){
                    Element element=doc.createElement("token");
                    element.appendChild(doc.createTextNode(lTokenizer.nextToken()));
                    resultSet.addNode(element);
                }
            }
        }
        // If the delimiter is an empty string, create one token Element for
        // every single character.
        else{
            Document doc=getDocument();
            synchronized(doc){
                for(int i=0;i<toTokenize.length();i++){
                    Element element=doc.createElement("token");
                    element.appendChild(doc.createTextNode(toTokenize.substring(i,i+1)));
                    resultSet.addNode(element);
                }
            }
        }
        return resultSet;
    }
}
