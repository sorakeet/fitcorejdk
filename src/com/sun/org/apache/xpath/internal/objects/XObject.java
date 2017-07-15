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
 * $Id: XObject.java,v 1.2.4.1 2005/09/14 20:34:45 jeffsuttor Exp $
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
 * $Id: XObject.java,v 1.2.4.1 2005/09/14 20:34:45 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.objects;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.utils.XMLString;
import com.sun.org.apache.xpath.internal.*;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

import java.io.Serializable;

public class XObject extends Expression implements Serializable, Cloneable{
    public static final int CLASS_NULL=-1;
    public static final int CLASS_UNKNOWN=0;
    public static final int CLASS_BOOLEAN=1;
    public static final int CLASS_NUMBER=2;
    public static final int CLASS_STRING=3;
    public static final int CLASS_NODESET=4;
    public static final int CLASS_RTREEFRAG=5;
    public static final int CLASS_UNRESOLVEDVARIABLE=600;
    static final long serialVersionUID=-821887098985662951L;
    protected Object m_obj;  // This may be NULL!!!

    public XObject(){
    }

    public XObject(Object obj){
        setObject(obj);
    }

    protected void setObject(Object obj){
        m_obj=obj;
    }

    static public XObject create(Object val){
        return XObjectFactory.create(val);
    }

    static public XObject create(Object val,XPathContext xctxt){
        return XObjectFactory.create(val,xctxt);
    }

    public XObject execute(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        return this;
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        // no-op
    }

    public boolean deepEquals(Expression expr){
        if(!isSameClass(expr))
            return false;
        // If equals at the expression level calls deepEquals, I think we're
        // still safe from infinite recursion since this object overrides
        // equals.  I hope.
        if(!this.equals((XObject)expr))
            return false;
        return true;
    }

    public void destruct(){
        if(null!=m_obj){
            allowDetachToRelease(true);
            detach();
            setObject(null);
        }
    }

    public void allowDetachToRelease(boolean allowRelease){
    }

    public void detach(){
    }

    public void reset(){
    }

    public void dispatchCharactersEvents(org.xml.sax.ContentHandler ch)
            throws org.xml.sax.SAXException{
        xstr().dispatchCharactersEvents(ch);
    }

    public XMLString xstr(){
        return XMLStringFactoryImpl.getFactory().newstr(str());
    }

    public String str(){
        return (m_obj!=null)?m_obj.toString():"";
    }

    public int getType(){
        return CLASS_UNKNOWN;
    }

    public double numWithSideEffects() throws javax.xml.transform.TransformerException{
        return num();
    }

    public double num() throws javax.xml.transform.TransformerException{
        error(XPATHErrorResources.ER_CANT_CONVERT_TO_NUMBER,
                new Object[]{getTypeString()});  //"Can not convert "+getTypeString()+" to a number");
        return 0.0;
    }

    public String getTypeString(){
        return "#UNKNOWN ("+object().getClass().getName()+")";
    }

    public Object object(){
        return m_obj;
    }

    protected void error(String msg,Object[] args)
            throws javax.xml.transform.TransformerException{
        String fmsg=XSLMessages.createXPATHMessage(msg,args);
        // boolean shouldThrow = support.problem(m_support.XPATHPROCESSOR,
        //                                      m_support.ERROR,
        //                                      null,
        //                                      null, fmsg, 0, 0);
        // if(shouldThrow)
        {
            throw new XPathException(fmsg,this);
        }
    }

    public boolean boolWithSideEffects() throws javax.xml.transform.TransformerException{
        return bool();
    }

    public boolean bool() throws javax.xml.transform.TransformerException{
        error(XPATHErrorResources.ER_CANT_CONVERT_TO_NUMBER,
                new Object[]{getTypeString()});  //"Can not convert "+getTypeString()+" to a number");
        return false;
    }

    public String toString(){
        return str();
    }

    public int rtf(XPathContext support){
        int result=rtf();
        if(DTM.NULL==result){
            DTM frag=support.createDocumentFragment();
            // %OPT%
            frag.appendTextChild(str());
            result=frag.getDocument();
        }
        return result;
    }

    public int rtf(){
        return DTM.NULL;
    }

    public DocumentFragment rtree(XPathContext support){
        DocumentFragment docFrag=null;
        int result=rtf();
        if(DTM.NULL==result){
            DTM frag=support.createDocumentFragment();
            // %OPT%
            frag.appendTextChild(str());
            docFrag=(DocumentFragment)frag.getNode(frag.getDocument());
        }else{
            DTM frag=support.getDTM(result);
            docFrag=(DocumentFragment)frag.getNode(frag.getDocument());
        }
        return docFrag;
    }

    public DocumentFragment rtree(){
        return null;
    }

    public DTMIterator iter() throws javax.xml.transform.TransformerException{
        error(XPATHErrorResources.ER_CANT_CONVERT_TO_NODELIST,
                new Object[]{getTypeString()});  //"Can not convert "+getTypeString()+" to a NodeList!");
        return null;
    }

    public XObject getFresh(){
        return this;
    }

    public NodeIterator nodeset() throws javax.xml.transform.TransformerException{
        error(XPATHErrorResources.ER_CANT_CONVERT_TO_NODELIST,
                new Object[]{getTypeString()});  //"Can not convert "+getTypeString()+" to a NodeList!");
        return null;
    }

    public NodeList nodelist() throws javax.xml.transform.TransformerException{
        error(XPATHErrorResources.ER_CANT_CONVERT_TO_NODELIST,
                new Object[]{getTypeString()});  //"Can not convert "+getTypeString()+" to a NodeList!");
        return null;
    }

    public NodeSetDTM mutableNodeset()
            throws javax.xml.transform.TransformerException{
        error(XPATHErrorResources.ER_CANT_CONVERT_TO_MUTABLENODELIST,
                new Object[]{getTypeString()});  //"Can not convert "+getTypeString()+" to a NodeSetDTM!");
        return (NodeSetDTM)m_obj;
    }

    public Object castToType(int t,XPathContext support)
            throws javax.xml.transform.TransformerException{
        Object result;
        switch(t){
            case CLASS_STRING:
                result=str();
                break;
            case CLASS_NUMBER:
                result=new Double(num());
                break;
            case CLASS_NODESET:
                result=iter();
                break;
            case CLASS_BOOLEAN:
                result=new Boolean(bool());
                break;
            case CLASS_UNKNOWN:
                result=m_obj;
                break;
            // %TBD%  What to do here?
            //    case CLASS_RTREEFRAG :
            //      result = rtree(support);
            //      break;
            default:
                error(XPATHErrorResources.ER_CANT_CONVERT_TO_TYPE,
                        new Object[]{getTypeString(),
                                Integer.toString(t)});  //"Can not convert "+getTypeString()+" to a type#"+t);
                result=null;
        }
        return result;
    }

    public boolean lessThan(XObject obj2)
            throws javax.xml.transform.TransformerException{
        // In order to handle the 'all' semantics of
        // nodeset comparisons, we always call the
        // nodeset function.  Because the arguments
        // are backwards, we call the opposite comparison
        // function.
        if(obj2.getType()==XObject.CLASS_NODESET)
            return obj2.greaterThan(this);
        return this.num()<obj2.num();
    }

    public boolean lessThanOrEqual(XObject obj2)
            throws javax.xml.transform.TransformerException{
        // In order to handle the 'all' semantics of
        // nodeset comparisons, we always call the
        // nodeset function.  Because the arguments
        // are backwards, we call the opposite comparison
        // function.
        if(obj2.getType()==XObject.CLASS_NODESET)
            return obj2.greaterThanOrEqual(this);
        return this.num()<=obj2.num();
    }

    public boolean greaterThan(XObject obj2)
            throws javax.xml.transform.TransformerException{
        // In order to handle the 'all' semantics of
        // nodeset comparisons, we always call the
        // nodeset function.  Because the arguments
        // are backwards, we call the opposite comparison
        // function.
        if(obj2.getType()==XObject.CLASS_NODESET)
            return obj2.lessThan(this);
        return this.num()>obj2.num();
    }

    public boolean greaterThanOrEqual(XObject obj2)
            throws javax.xml.transform.TransformerException{
        // In order to handle the 'all' semantics of
        // nodeset comparisons, we always call the
        // nodeset function.  Because the arguments
        // are backwards, we call the opposite comparison
        // function.
        if(obj2.getType()==XObject.CLASS_NODESET)
            return obj2.lessThanOrEqual(this);
        return this.num()>=obj2.num();
    }

    public boolean notEquals(XObject obj2)
            throws javax.xml.transform.TransformerException{
        // In order to handle the 'all' semantics of
        // nodeset comparisons, we always call the
        // nodeset function.
        if(obj2.getType()==XObject.CLASS_NODESET)
            return obj2.notEquals(this);
        return !equals(obj2);
    }

    public boolean equals(XObject obj2){
        // In order to handle the 'all' semantics of
        // nodeset comparisons, we always call the
        // nodeset function.
        if(obj2.getType()==XObject.CLASS_NODESET)
            return obj2.equals(this);
        if(null!=m_obj){
            return m_obj.equals(obj2.m_obj);
        }else{
            return obj2.m_obj==null;
        }
    }

    protected void error(String msg)
            throws javax.xml.transform.TransformerException{
        error(msg,null);
    }

    public void appendToFsb(com.sun.org.apache.xml.internal.utils.FastStringBuffer fsb){
        fsb.append(str());
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        assertion(false,"callVisitors should not be called for this object!!!");
    }
}
