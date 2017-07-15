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
 * $Id: XStringForChars.java,v 1.2.4.1 2005/09/14 20:46:27 jeffsuttor Exp $
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
 * $Id: XStringForChars.java,v 1.2.4.1 2005/09/14 20:46:27 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.objects;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

public class XStringForChars extends XString{
    static final long serialVersionUID=-2235248887220850467L;
    protected String m_strCache=null;
    int m_start;
    int m_length;

    public XStringForChars(char[] val,int start,int length){
        super(val);
        m_start=start;
        m_length=length;
        if(null==val)
            throw new IllegalArgumentException(
                    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_FASTSTRINGBUFFER_CANNOT_BE_NULL,null)); //"The FastStringBuffer argument can not be null!!");
    }

    private XStringForChars(String val){
        super(val);
        throw new IllegalArgumentException(
                XSLMessages.createXPATHMessage(XPATHErrorResources.ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING,null)); //"XStringForChars can not take a string for an argument!");
    }

    public FastStringBuffer fsb(){
        throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS,null)); //"fsb() not supported for XStringForChars!");
    }

    public boolean hasString(){
        return (null!=m_strCache);
    }

    public String str(){
        if(null==m_strCache)
            m_strCache=new String((char[])m_obj,m_start,m_length);
        return m_strCache;
    }

    public void dispatchCharactersEvents(org.xml.sax.ContentHandler ch)
            throws org.xml.sax.SAXException{
        ch.characters((char[])m_obj,m_start,m_length);
    }

    public void dispatchAsComment(org.xml.sax.ext.LexicalHandler lh)
            throws org.xml.sax.SAXException{
        lh.comment((char[])m_obj,m_start,m_length);
    }

    public int length(){
        return m_length;
    }

    public char charAt(int index){
        return ((char[])m_obj)[index+m_start];
    }

    public void getChars(int srcBegin,int srcEnd,char dst[],int dstBegin){
        System.arraycopy((char[])m_obj,m_start+srcBegin,dst,dstBegin,srcEnd);
    }

    public Object object(){
        return str();
    }

    public void appendToFsb(FastStringBuffer fsb){
        fsb.append((char[])m_obj,m_start,m_length);
    }
}
