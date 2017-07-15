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
 * $Id: XRTreeFrag.java,v 1.2.4.1 2005/09/14 20:44:48 jeffsuttor Exp $
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
 * $Id: XRTreeFrag.java,v 1.2.4.1 2005/09/14 20:44:48 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.objects;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.utils.XMLString;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionNode;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.axes.RTFIterator;
import org.w3c.dom.NodeList;

public class XRTreeFrag extends XObject implements Cloneable{
    static final long serialVersionUID=-3201553822254911567L;
    protected boolean m_allowRelease=false;
    private DTMXRTreeFrag m_DTMXRTreeFrag;
    private int m_dtmRoot=DTM.NULL;
    private XMLString m_xmlStr=null;

    public XRTreeFrag(int root,XPathContext xctxt,ExpressionNode parent){
        super(null);
        exprSetParent(parent);
        initDTM(root,xctxt);
    }

    private final void initDTM(int root,XPathContext xctxt){
        m_dtmRoot=root;
        final DTM dtm=xctxt.getDTM(root);
        if(dtm!=null){
            m_DTMXRTreeFrag=xctxt.getDTMXRTreeFrag(xctxt.getDTMIdentity(dtm));
        }
    }

    public XRTreeFrag(int root,XPathContext xctxt){
        super(null);
        initDTM(root,xctxt);
    }

    public XRTreeFrag(Expression expr){
        super(expr);
    }

    public void allowDetachToRelease(boolean allowRelease){
        m_allowRelease=allowRelease;
    }

    public void detach(){
        if(m_allowRelease){
            m_DTMXRTreeFrag.destruct();
            setObject(null);
        }
    }

    public int getType(){
        return CLASS_RTREEFRAG;
    }

    public String getTypeString(){
        return "#RTREEFRAG";
    }

    public double num()
            throws javax.xml.transform.TransformerException{
        XMLString s=xstr();
        return s.toDouble();
    }

    public boolean bool(){
        return true;
    }

    public XMLString xstr(){
        if(null==m_xmlStr)
            m_xmlStr=m_DTMXRTreeFrag.getDTM().getStringValue(m_dtmRoot);
        return m_xmlStr;
    }

    public String str(){
        String str=m_DTMXRTreeFrag.getDTM().getStringValue(m_dtmRoot).toString();
        return (null==str)?"":str;
    }

    public int rtf(){
        return m_dtmRoot;
    }

    public Object object(){
        if(m_DTMXRTreeFrag.getXPathContext()!=null)
            return new com.sun.org.apache.xml.internal.dtm.ref.DTMNodeIterator((DTMIterator)(new com.sun.org.apache.xpath.internal.NodeSetDTM(m_dtmRoot,m_DTMXRTreeFrag.getXPathContext().getDTMManager())));
        else
            return super.object();
    }

    public boolean equals(XObject obj2){
        try{
            if(XObject.CLASS_NODESET==obj2.getType()){
                // In order to handle the 'all' semantics of
                // nodeset comparisons, we always call the
                // nodeset function.
                return obj2.equals(this);
            }else if(XObject.CLASS_BOOLEAN==obj2.getType()){
                return bool()==obj2.bool();
            }else if(XObject.CLASS_NUMBER==obj2.getType()){
                return num()==obj2.num();
            }else if(XObject.CLASS_NODESET==obj2.getType()){
                return xstr().equals(obj2.xstr());
            }else if(XObject.CLASS_STRING==obj2.getType()){
                return xstr().equals(obj2.xstr());
            }else if(XObject.CLASS_RTREEFRAG==obj2.getType()){
                // Probably not so good.  Think about this.
                return xstr().equals(obj2.xstr());
            }else{
                return super.equals(obj2);
            }
        }catch(javax.xml.transform.TransformerException te){
            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(te);
        }
    }

    public void appendToFsb(com.sun.org.apache.xml.internal.utils.FastStringBuffer fsb){
        XString xstring=(XString)xstr();
        xstring.appendToFsb(fsb);
    }

    public NodeList convertToNodeset(){
        if(m_obj instanceof NodeList)
            return (NodeList)m_obj;
        else
            return new com.sun.org.apache.xml.internal.dtm.ref.DTMNodeList(asNodeIterator());
    }

    public DTMIterator asNodeIterator(){
        return new RTFIterator(m_dtmRoot,m_DTMXRTreeFrag.getXPathContext().getDTMManager());
    }
}
