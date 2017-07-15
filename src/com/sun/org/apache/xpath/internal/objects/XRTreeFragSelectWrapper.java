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
 * $Id: XRTreeFragSelectWrapper.java,v 1.2.4.1 2005/09/15 02:02:35 jeffsuttor Exp $
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
 * $Id: XRTreeFragSelectWrapper.java,v 1.2.4.1 2005/09/15 02:02:35 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.objects;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.utils.XMLString;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

public class XRTreeFragSelectWrapper extends XRTreeFrag implements Cloneable{
    static final long serialVersionUID=-6526177905590461251L;

    public XRTreeFragSelectWrapper(Expression expr){
        super(expr);
    }

    public XObject execute(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        XObject m_selected;
        m_selected=((Expression)m_obj).execute(xctxt);
        m_selected.allowDetachToRelease(m_allowRelease);
        if(m_selected.getType()==CLASS_STRING)
            return m_selected;
        else
            return new XString(m_selected.str());
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        ((Expression)m_obj).fixupVariables(vars,globalsSize);
    }

    public void detach(){
        throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,null)); //"detach() not supported by XRTreeFragSelectWrapper!");
    }

    public int getType(){
        return CLASS_STRING;
    }

    public double num()
            throws javax.xml.transform.TransformerException{
        throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,null)); //"num() not supported by XRTreeFragSelectWrapper!");
    }

    public XMLString xstr(){
        throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,null)); //"xstr() not supported by XRTreeFragSelectWrapper!");
    }

    public String str(){
        throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,null)); //"str() not supported by XRTreeFragSelectWrapper!");
    }

    public int rtf(){
        throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,null)); //"rtf() not supported by XRTreeFragSelectWrapper!");
    }

    public DTMIterator asNodeIterator(){
        throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,null)); //"asNodeIterator() not supported by XRTreeFragSelectWrapper!");
    }
}
