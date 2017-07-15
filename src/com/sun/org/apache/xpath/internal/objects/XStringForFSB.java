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
 * $Id: XStringForFSB.java,v 1.2.4.2 2005/09/14 20:46:27 jeffsuttor Exp $
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
 * $Id: XStringForFSB.java,v 1.2.4.2 2005/09/14 20:46:27 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.objects;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import com.sun.org.apache.xml.internal.utils.XMLCharacterRecognizer;
import com.sun.org.apache.xml.internal.utils.XMLString;
import com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

public class XStringForFSB extends XString{
    static final long serialVersionUID=-1533039186550674548L;
    protected String m_strCache=null;
    protected int m_hash=0;
    int m_start;
    int m_length;

    public XStringForFSB(FastStringBuffer val,int start,int length){
        super(val);
        m_start=start;
        m_length=length;
        if(null==val)
            throw new IllegalArgumentException(
                    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_FASTSTRINGBUFFER_CANNOT_BE_NULL,null));
    }

    private XStringForFSB(String val){
        super(val);
        throw new IllegalArgumentException(
                XSLMessages.createXPATHMessage(XPATHErrorResources.ER_FSB_CANNOT_TAKE_STRING,null)); // "XStringForFSB can not take a string for an argument!");
    }

    public boolean hasString(){
        return (null!=m_strCache);
    }

    public double toDouble(){
        if(m_length==0)
            return Double.NaN;
        int i;
        char c;
        String valueString=fsb().getString(m_start,m_length);
        // The following are permitted in the Double.valueOf, but not by the XPath spec:
        // - a plus sign
        // - The use of e or E to indicate exponents
        // - trailing f, F, d, or D
        // See function comments; not sure if this is slower than actually doing the
        // conversion ourselves (as was before).
        for(i=0;i<m_length;i++)
            if(!XMLCharacterRecognizer.isWhiteSpace(valueString.charAt(i)))
                break;
        if(i==m_length) return Double.NaN;
        if(valueString.charAt(i)=='-')
            i++;
        for(;i<m_length;i++){
            c=valueString.charAt(i);
            if(c!='.'&&(c<'0'||c>'9'))
                break;
        }
        for(;i<m_length;i++)
            if(!XMLCharacterRecognizer.isWhiteSpace(valueString.charAt(i)))
                break;
        if(i!=m_length)
            return Double.NaN;
        try{
            return Double.parseDouble(valueString);
        }catch(NumberFormatException nfe){
            // This should catch double periods, empty strings.
            return Double.NaN;
        }
    }

    public String str(){
        if(null==m_strCache){
            m_strCache=fsb().getString(m_start,m_length);
//      strCount++;
//
//      RuntimeException e = new RuntimeException("Bad!  Bad!");
//      java.io.CharArrayWriter writer = new java.io.CharArrayWriter();
//      java.io.PrintWriter pw = new java.io.PrintWriter(writer);
//
//      e.printStackTrace(pw);
//
//      String str = writer.toString();
//
//      str = str.substring(0, 600);
//
//      if (null == xtable.get(str))
//      {
//        xtable.put(str, str);
//        System.out.println(str);
//      }
//      System.out.println("strCount: " + strCount);
//      throw e;
//      e.printStackTrace();
            // System.exit(-1);
        }
        return m_strCache;
    }
//  /** NEEDSDOC Field strCount */
//  public static int strCount = 0;
//
//  /** NEEDSDOC Field xtable */
//  static java.util.Hashtable xtable = new java.util.Hashtable();

    public FastStringBuffer fsb(){
        return ((FastStringBuffer)m_obj);
    }

    public void dispatchCharactersEvents(org.xml.sax.ContentHandler ch)
            throws org.xml.sax.SAXException{
        fsb().sendSAXcharacters(ch,m_start,m_length);
    }

    public void dispatchAsComment(org.xml.sax.ext.LexicalHandler lh)
            throws org.xml.sax.SAXException{
        fsb().sendSAXComment(lh,m_start,m_length);
    }

    public int length(){
        return m_length;
    }

    public char charAt(int index){
        return fsb().charAt(m_start+index);
    }

    public void getChars(int srcBegin,int srcEnd,char dst[],int dstBegin){
        // %OPT% Need to call this on FSB when it is implemented.
        // %UNTESTED% (I don't think anyone calls this yet?)
        int n=srcEnd-srcBegin;
        if(n>m_length)
            n=m_length;
        if(n>(dst.length-dstBegin))
            n=(dst.length-dstBegin);
        int end=srcBegin+m_start+n;
        int d=dstBegin;
        FastStringBuffer fsb=fsb();
        for(int i=srcBegin+m_start;i<end;i++){
            dst[d++]=fsb.charAt(i);
        }
    }

    public boolean equals(XObject obj2){
        if(this==obj2){
            return true;
        }
        if(obj2.getType()==XObject.CLASS_NUMBER)
            return obj2.equals(this);
        String str=obj2.str();
        int n=m_length;
        if(n==str.length()){
            FastStringBuffer fsb=fsb();
            int i=m_start;
            int j=0;
            while(n--!=0){
                if(fsb.charAt(i)!=str.charAt(j)){
                    return false;
                }
                i++;
                j++;
            }
            return true;
        }
        return false;
    }

    public boolean equals(String anotherString){
        int n=m_length;
        if(n==anotherString.length()){
            FastStringBuffer fsb=fsb();
            int i=m_start;
            int j=0;
            while(n--!=0){
                if(fsb.charAt(i)!=anotherString.charAt(j)){
                    return false;
                }
                i++;
                j++;
            }
            return true;
        }
        return false;
    }

    public boolean equals(XMLString obj2){
        if(this==obj2){
            return true;
        }
        int n=m_length;
        if(n==obj2.length()){
            FastStringBuffer fsb=fsb();
            int i=m_start;
            int j=0;
            while(n--!=0){
                if(fsb.charAt(i)!=obj2.charAt(j)){
                    return false;
                }
                i++;
                j++;
            }
            return true;
        }
        return false;
    }

    public boolean equals(Object obj2){
        if(null==obj2)
            return false;
        if(obj2 instanceof XNumber)
            return obj2.equals(this);
            // In order to handle the 'all' semantics of
            // nodeset comparisons, we always call the
            // nodeset function.
        else if(obj2 instanceof XNodeSet)
            return obj2.equals(this);
        else if(obj2 instanceof XStringForFSB)
            return equals((XMLString)obj2);
        else
            return equals(obj2.toString());
    }

    public boolean equalsIgnoreCase(String anotherString){
        return (m_length==anotherString.length())
                ?str().equalsIgnoreCase(anotherString):false;
    }

    public int compareTo(XMLString xstr){
        int len1=m_length;
        int len2=xstr.length();
        int n=Math.min(len1,len2);
        FastStringBuffer fsb=fsb();
        int i=m_start;
        int j=0;
        while(n--!=0){
            char c1=fsb.charAt(i);
            char c2=xstr.charAt(j);
            if(c1!=c2){
                return c1-c2;
            }
            i++;
            j++;
        }
        return len1-len2;
    }

    public int compareToIgnoreCase(XMLString xstr){
        int len1=m_length;
        int len2=xstr.length();
        int n=Math.min(len1,len2);
        FastStringBuffer fsb=fsb();
        int i=m_start;
        int j=0;
        while(n--!=0){
            char c1=Character.toLowerCase(fsb.charAt(i));
            char c2=Character.toLowerCase(xstr.charAt(j));
            if(c1!=c2){
                return c1-c2;
            }
            i++;
            j++;
        }
        return len1-len2;
    }

    public boolean startsWith(XMLString prefix,int toffset){
        FastStringBuffer fsb=fsb();
        int to=m_start+toffset;
        int tlim=m_start+m_length;
        int po=0;
        int pc=prefix.length();
        // Note: toffset might be near -1>>>1.
        if((toffset<0)||(toffset>m_length-pc)){
            return false;
        }
        while(--pc>=0){
            if(fsb.charAt(to)!=prefix.charAt(po)){
                return false;
            }
            to++;
            po++;
        }
        return true;
    }

    public boolean startsWith(XMLString prefix){
        return startsWith(prefix,0);
    }

    public int hashCode(){
        // Commenting this out because in JDK1.1.8 and VJ++
        // we don't match XMLStrings. Defaulting to the super
        // causes us to create a string, but at this point
        // this only seems to get called in key processing.
        // Maybe we can live with it?
/**
 int h = m_hash;

 if (h == 0)
 {
 int off = m_start;
 int len = m_length;
 FastStringBuffer fsb = fsb();

 for (int i = 0; i < len; i++)
 {
 h = 31 * h + fsb.charAt(off);

 off++;
 }

 m_hash = h;
 }
 */
        return super.hashCode(); // h;
    }

    public int indexOf(int ch){
        return indexOf(ch,0);
    }

    public int indexOf(int ch,int fromIndex){
        int max=m_start+m_length;
        FastStringBuffer fsb=fsb();
        if(fromIndex<0){
            fromIndex=0;
        }else if(fromIndex>=m_length){
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }
        for(int i=m_start+fromIndex;i<max;i++){
            if(fsb.charAt(i)==ch){
                return i-m_start;
            }
        }
        return -1;
    }

    public XMLString substring(int beginIndex){
        int len=m_length-beginIndex;
        if(len<=0)
            return XString.EMPTYSTRING;
        else{
            int start=m_start+beginIndex;
            return new XStringForFSB(fsb(),start,len);
        }
    }

    public XMLString substring(int beginIndex,int endIndex){
        int len=endIndex-beginIndex;
        if(len>m_length)
            len=m_length;
        if(len<=0)
            return XString.EMPTYSTRING;
        else{
            int start=m_start+beginIndex;
            return new XStringForFSB(fsb(),start,len);
        }
    }

    public XMLString concat(String str){
        // %OPT% Make an FSB here?
        return new XString(str().concat(str));
    }

    public XMLString trim(){
        return fixWhiteSpace(true,true,false);
    }

    public XMLString fixWhiteSpace(boolean trimHead,boolean trimTail,
                                   boolean doublePunctuationSpaces){
        int end=m_length+m_start;
        char[] buf=new char[m_length];
        FastStringBuffer fsb=fsb();
        boolean edit=false;
        /** replace S to ' '. and ' '+ -> single ' '. */
        int d=0;
        boolean pres=false;
        for(int s=m_start;s<end;s++){
            char c=fsb.charAt(s);
            if(isSpace(c)){
                if(!pres){
                    if(' '!=c){
                        edit=true;
                    }
                    buf[d++]=' ';
                    if(doublePunctuationSpaces&&(d!=0)){
                        char prevChar=buf[d-1];
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
        return edit?xsf.newstr(buf,start,d-start):this;
    }

    private static boolean isSpace(char ch){
        return XMLCharacterRecognizer.isWhiteSpace(ch);  // Take the easy way out for now.
    }

    public Object object(){
        return str();
    }

    public void appendToFsb(FastStringBuffer fsb){
        // %OPT% !!! FSB has to be updated to take partial fsb's for append.
        fsb.append(str());
    }
}
