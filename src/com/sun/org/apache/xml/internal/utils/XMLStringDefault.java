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
 * $Id: XMLStringDefault.java,v 1.2.4.1 2005/09/15 08:16:02 suresh_emailid Exp $
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
 * $Id: XMLStringDefault.java,v 1.2.4.1 2005/09/15 08:16:02 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import java.util.Locale;

public class XMLStringDefault implements XMLString{
    private String m_str;

    public XMLStringDefault(String str){
        m_str=str;
    }

    public void dispatchCharactersEvents(org.xml.sax.ContentHandler ch)
            throws org.xml.sax.SAXException{
    }

    public void dispatchAsComment(org.xml.sax.ext.LexicalHandler lh)
            throws org.xml.sax.SAXException{
    }

    public XMLString fixWhiteSpace(boolean trimHead,
                                   boolean trimTail,
                                   boolean doublePunctuationSpaces){
        return new XMLStringDefault(m_str.trim());
    }

    public int length(){
        return m_str.length();
    }

    public char charAt(int index){
        return m_str.charAt(index);
    }

    public void getChars(int srcBegin,int srcEnd,char dst[],
                         int dstBegin){
        int destIndex=dstBegin;
        for(int i=srcBegin;i<srcEnd;i++){
            dst[destIndex++]=m_str.charAt(i);
        }
    }

    public boolean equals(XMLString anObject){
        return m_str.equals(anObject.toString());
    }

    public boolean equals(String obj2){
        return m_str.equals(obj2);
    }

    public boolean equalsIgnoreCase(String anotherString){
        return m_str.equalsIgnoreCase(anotherString);
    }

    public int compareTo(XMLString anotherString){
        return m_str.compareTo(anotherString.toString());
    }

    public int compareToIgnoreCase(XMLString str){
        return m_str.compareToIgnoreCase(str.toString());
    }

    public boolean startsWith(String prefix,int toffset){
        return m_str.startsWith(prefix,toffset);
    }

    public boolean startsWith(XMLString prefix,int toffset){
        return m_str.startsWith(prefix.toString(),toffset);
    }

    public boolean startsWith(String prefix){
        return m_str.startsWith(prefix);
    }

    public boolean startsWith(XMLString prefix){
        return m_str.startsWith(prefix.toString());
    }

    public boolean endsWith(String suffix){
        return m_str.endsWith(suffix);
    }

    public int indexOf(int ch){
        return m_str.indexOf(ch);
    }

    public int indexOf(int ch,int fromIndex){
        return m_str.indexOf(ch,fromIndex);
    }

    public int lastIndexOf(int ch){
        return m_str.lastIndexOf(ch);
    }

    public int lastIndexOf(int ch,int fromIndex){
        return m_str.lastIndexOf(ch,fromIndex);
    }

    public int indexOf(String str){
        return m_str.indexOf(str);
    }

    public int indexOf(XMLString str){
        return m_str.indexOf(str.toString());
    }

    public int indexOf(String str,int fromIndex){
        return m_str.indexOf(str,fromIndex);
    }

    public int lastIndexOf(String str){
        return m_str.lastIndexOf(str);
    }

    public int lastIndexOf(String str,int fromIndex){
        return m_str.lastIndexOf(str,fromIndex);
    }

    public XMLString substring(int beginIndex){
        return new XMLStringDefault(m_str.substring(beginIndex));
    }

    public XMLString substring(int beginIndex,int endIndex){
        return new XMLStringDefault(m_str.substring(beginIndex,endIndex));
    }

    public XMLString concat(String str){
        return new XMLStringDefault(m_str.concat(str));
    }

    public XMLString toLowerCase(Locale locale){
        return new XMLStringDefault(m_str.toLowerCase(locale));
    }

    public XMLString toLowerCase(){
        return new XMLStringDefault(m_str.toLowerCase());
    }

    public XMLString toUpperCase(Locale locale){
        return new XMLStringDefault(m_str.toUpperCase(locale));
    }

    public XMLString toUpperCase(){
        return new XMLStringDefault(m_str.toUpperCase());
    }

    public XMLString trim(){
        return new XMLStringDefault(m_str.trim());
    }

    public boolean hasString(){
        return true;
    }

    public double toDouble(){
        try{
            return Double.valueOf(m_str).doubleValue();
        }catch(NumberFormatException nfe){
            return Double.NaN;
        }
    }

    public int hashCode(){
        return m_str.hashCode();
    }

    public boolean equals(Object anObject){
        return m_str.equals(anObject);
    }

    public String toString(){
        return m_str;
    }
}
