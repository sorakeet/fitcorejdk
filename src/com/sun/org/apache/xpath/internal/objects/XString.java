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
 * $Id: XString.java,v 1.2.4.1 2005/09/14 20:47:20 jeffsuttor Exp $
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
 * $Id: XString.java,v 1.2.4.1 2005/09/14 20:47:20 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.objects;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.utils.XMLCharacterRecognizer;
import com.sun.org.apache.xml.internal.utils.XMLString;
import com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.XPathVisitor;

import java.util.Locale;

public class XString extends XObject implements XMLString{
    public static final XString EMPTYSTRING=new XString("");
    static final long serialVersionUID=2020470518395094525L;

    protected XString(Object val){
        super(val);
    }

    public XString(String val){
        super(val);
    }

    public void dispatchCharactersEvents(org.xml.sax.ContentHandler ch)
            throws org.xml.sax.SAXException{
        String str=str();
        ch.characters(str.toCharArray(),0,str.length());
    }

    public int getType(){
        return CLASS_STRING;
    }

    public String getTypeString(){
        return "#STRING";
    }

    public double num(){
        return toDouble();
    }

    public boolean bool(){
        return str().length()>0;
    }

    public XMLString xstr(){
        return this;
    }

    public String str(){
        return (null!=m_obj)?((String)m_obj):"";
    }

    public int rtf(XPathContext support){
        DTM frag=support.createDocumentFragment();
        frag.appendTextChild(str());
        return frag.getDocument();
    }

    public boolean equals(XObject obj2){
        // In order to handle the 'all' semantics of
        // nodeset comparisons, we always call the
        // nodeset function.
        int t=obj2.getType();
        try{
            if(XObject.CLASS_NODESET==t)
                return obj2.equals(this);
                // If at least one object to be compared is a boolean, then each object
                // to be compared is converted to a boolean as if by applying the
                // boolean function.
            else if(XObject.CLASS_BOOLEAN==t)
                return obj2.bool()==bool();
                // Otherwise, if at least one object to be compared is a number, then each object
                // to be compared is converted to a number as if by applying the number function.
            else if(XObject.CLASS_NUMBER==t)
                return obj2.num()==num();
        }catch(javax.xml.transform.TransformerException te){
            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(te);
        }
        // Otherwise, both objects to be compared are converted to strings as
        // if by applying the string function.
        return xstr().equals(obj2.xstr());
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        visitor.visitStringLiteral(owner,this);
    }

    public void dispatchAsComment(org.xml.sax.ext.LexicalHandler lh)
            throws org.xml.sax.SAXException{
        String str=str();
        lh.comment(str.toCharArray(),0,str.length());
    }

    public XMLString fixWhiteSpace(boolean trimHead,boolean trimTail,
                                   boolean doublePunctuationSpaces){
        // %OPT% !!!!!!!
        int len=this.length();
        char[] buf=new char[len];
        this.getChars(0,len,buf,0);
        boolean edit=false;
        int s;
        for(s=0;s<len;s++){
            if(isSpace(buf[s])){
                break;
            }
        }
        /** replace S to ' '. and ' '+ -> single ' '. */
        int d=s;
        boolean pres=false;
        for(;s<len;s++){
            char c=buf[s];
            if(isSpace(c)){
                if(!pres){
                    if(' '!=c){
                        edit=true;
                    }
                    buf[d++]=' ';
                    if(doublePunctuationSpaces&&(s!=0)){
                        char prevChar=buf[s-1];
                        if(!((prevChar=='.')||(prevChar=='!')
                                ||(prevChar=='?'))){
                            pres=true;
                        }
                    }else{
                        pres=true;
                    }
                }else{
                    edit=true;
                    pres=true;
                }
            }else{
                buf[d++]=c;
                pres=false;
            }
        }
        if(trimTail&&1<=d&&' '==buf[d-1]){
            edit=true;
            d--;
        }
        int start=0;
        if(trimHead&&0<d&&' '==buf[0]){
            edit=true;
            start++;
        }
        XMLStringFactory xsf=XMLStringFactoryImpl.getFactory();
        return edit?xsf.newstr(new String(buf,start,d-start)):this;
    }

    public int length(){
        return str().length();
    }

    public char charAt(int index){
        return str().charAt(index);
    }

    public void getChars(int srcBegin,int srcEnd,char dst[],int dstBegin){
        str().getChars(srcBegin,srcEnd,dst,dstBegin);
    }

    public boolean equals(XMLString obj2){
        if(obj2!=null){
            if(!obj2.hasString()){
                return obj2.equals(str());
            }else{
                return str().equals(obj2.toString());
            }
        }
        return false;
    }

    public boolean equals(String obj2){
        return str().equals(obj2);
    }

    public boolean equalsIgnoreCase(String anotherString){
        return str().equalsIgnoreCase(anotherString);
    }

    public int compareTo(XMLString xstr){
        int len1=this.length();
        int len2=xstr.length();
        int n=Math.min(len1,len2);
        int i=0;
        int j=0;
        while(n--!=0){
            char c1=this.charAt(i);
            char c2=xstr.charAt(j);
            if(c1!=c2){
                return c1-c2;
            }
            i++;
            j++;
        }
        return len1-len2;
    }

    public int compareToIgnoreCase(XMLString str){
        // %REVIEW%  Like it says, @since 1.2. Doesn't exist in earlier
        // versions of Java, hence we can't yet shell out to it. We can implement
        // it as character-by-character compare, but doing so efficiently
        // is likely to be (ahem) interesting.
        //
        // However, since nobody is actually _using_ this method yet:
        //    return str().compareToIgnoreCase(str.toString());
        throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(
                new NoSuchMethodException(
                        "Java 1.2 method, not yet implemented"));
    }

    public boolean startsWith(String prefix,int toffset){
        return str().startsWith(prefix,toffset);
    }

    public boolean startsWith(XMLString prefix,int toffset){
        int to=toffset;
        int tlim=this.length();
        int po=0;
        int pc=prefix.length();
        // Note: toffset might be near -1>>>1.
        if((toffset<0)||(toffset>tlim-pc)){
            return false;
        }
        while(--pc>=0){
            if(this.charAt(to)!=prefix.charAt(po)){
                return false;
            }
            to++;
            po++;
        }
        return true;
    }

    public boolean startsWith(String prefix){
        return startsWith(prefix,0);
    }

    public boolean startsWith(XMLString prefix){
        return startsWith(prefix,0);
    }

    public boolean endsWith(String suffix){
        return str().endsWith(suffix);
    }

    public int indexOf(int ch){
        return str().indexOf(ch);
    }

    public int indexOf(int ch,int fromIndex){
        return str().indexOf(ch,fromIndex);
    }

    public int lastIndexOf(int ch){
        return str().lastIndexOf(ch);
    }

    public int lastIndexOf(int ch,int fromIndex){
        return str().lastIndexOf(ch,fromIndex);
    }

    public int indexOf(String str){
        return str().indexOf(str);
    }

    public int indexOf(XMLString str){
        return str().indexOf(str.toString());
    }

    public int indexOf(String str,int fromIndex){
        return str().indexOf(str,fromIndex);
    }

    public int lastIndexOf(String str){
        return str().lastIndexOf(str);
    }

    public int lastIndexOf(String str,int fromIndex){
        return str().lastIndexOf(str,fromIndex);
    }

    public XMLString substring(int beginIndex){
        return new XString(str().substring(beginIndex));
    }

    public XMLString substring(int beginIndex,int endIndex){
        return new XString(str().substring(beginIndex,endIndex));
    }

    public XMLString concat(String str){
        // %REVIEW% Make an FSB here?
        return new XString(str().concat(str));
    }

    public XMLString toLowerCase(Locale locale){
        return new XString(str().toLowerCase(locale));
    }

    public XMLString toLowerCase(){
        return new XString(str().toLowerCase());
    }

    public XMLString toUpperCase(Locale locale){
        return new XString(str().toUpperCase(locale));
    }

    public XMLString toUpperCase(){
        return new XString(str().toUpperCase());
    }

    public XMLString trim(){
        return new XString(str().trim());
    }

    public boolean hasString(){
        return true;
    }

    public double toDouble(){
        /** XMLCharacterRecognizer.isWhiteSpace(char c) methods treats the following
         * characters as white space characters.
         * ht - horizontal tab, nl - newline , cr - carriage return and sp - space
         * trim() methods by default also takes care of these white space characters
         * So trim() method is used to remove leading and trailing white spaces.
         */
        XMLString s=trim();
        double result=Double.NaN;
        for(int i=0;i<s.length();i++){
            char c=s.charAt(i);
            if(c!='-'&&c!='.'&&(c<0X30||c>0x39)){
                // The character is not a '-' or a '.' or a digit
                // then return NaN because something is wrong.
                return result;
            }
        }
        try{
            result=Double.parseDouble(s.toString());
        }catch(NumberFormatException e){
        }
        return result;
    }

    private static boolean isSpace(char ch){
        return XMLCharacterRecognizer.isWhiteSpace(ch);  // Take the easy way out for now.
    }

    public int hashCode(){
        return str().hashCode();
    }

    public boolean equals(Object obj2){
        if(null==obj2)
            return false;
            // In order to handle the 'all' semantics of
            // nodeset comparisons, we always call the
            // nodeset function.
        else if(obj2 instanceof XNodeSet)
            return obj2.equals(this);
        else if(obj2 instanceof XNumber)
            return obj2.equals(this);
        else
            return str().equals(obj2.toString());
    }
}
