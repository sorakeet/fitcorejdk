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
 * $Id: Variable.java,v 1.2.4.1 2005/09/14 21:24:33 jeffsuttor Exp $
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
 * $Id: Variable.java,v 1.2.4.1 2005/09/14 21:24:33 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.operations;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.utils.QName;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.axes.PathComponent;
import com.sun.org.apache.xpath.internal.axes.WalkerFactory;
import com.sun.org.apache.xpath.internal.objects.XNodeSet;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

import javax.xml.transform.TransformerException;

public class Variable extends Expression implements PathComponent{
    static final long serialVersionUID=-4334975375609297049L;
    static final java.lang.String PSUEDOVARNAMESPACE="http://xml.apache.org/xalan/psuedovar";
    protected QName m_qname;
    protected int m_index;
    protected boolean m_isGlobal=false;
    private boolean m_fixUpWasCalled=false;

    public int getIndex(){
        return m_index;
    }

    public void setIndex(int index){
        m_index=index;
    }

    public void setIsGlobal(boolean isGlobal){
        m_isGlobal=isGlobal;
    }

    public boolean getGlobal(){
        return m_isGlobal;
    }

    public QName getQName(){
        return m_qname;
    }

    public void setQName(QName qname){
        m_qname=qname;
    }

    public XObject execute(XPathContext xctxt)
            throws TransformerException{
        return execute(xctxt,false);
    }

    public XObject execute(XPathContext xctxt,boolean destructiveOK) throws TransformerException{
        com.sun.org.apache.xml.internal.utils.PrefixResolver xprefixResolver=xctxt.getNamespaceContext();
        XObject result;
        // Is the variable fetched always the same?
        // XObject result = xctxt.getVariable(m_qname);
        if(m_fixUpWasCalled){
            if(m_isGlobal)
                result=xctxt.getVarStack().getGlobalVariable(xctxt,m_index,destructiveOK);
            else
                result=xctxt.getVarStack().getLocalVariable(xctxt,m_index,destructiveOK);
        }else{
            result=xctxt.getVarStack().getVariableOrParam(xctxt,m_qname);
        }
        if(null==result){
            // This should now never happen...
            warn(xctxt,XPATHErrorResources.WG_ILLEGAL_VARIABLE_REFERENCE,
                    new Object[]{m_qname.getLocalPart()});  //"VariableReference given for variable out "+
            //      (new RuntimeException()).printStackTrace();
            //      error(xctxt, XPATHErrorResources.ER_COULDNOT_GET_VAR_NAMED,
            //            new Object[]{ m_qname.getLocalPart() });  //"Could not get variable named "+varName);
            result=new XNodeSet(xctxt.getDTMManager());
        }
        return result;
//    }
//    else
//    {
//      // Hack city... big time.  This is needed to evaluate xpaths from extensions,
//      // pending some bright light going off in my head.  Some sort of callback?
//      synchronized(this)
//      {
//              com.sun.org.apache.xalan.internal.templates.ElemVariable vvar= getElemVariable();
//              if(null != vvar)
//              {
//          m_index = vvar.getIndex();
//          m_isGlobal = vvar.getIsTopLevel();
//          m_fixUpWasCalled = true;
//          return execute(xctxt);
//              }
//      }
//      throw new javax.xml.transform.TransformerException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_VAR_NOT_RESOLVABLE, new Object[]{m_qname.toString()})); //"Variable not resolvable: "+m_qname);
//    }
    }

    // J2SE does not support Xalan interpretive
    public boolean isStableNumber(){
        return true;
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        m_fixUpWasCalled=true;
        int sz=vars.size();
        for(int i=vars.size()-1;i>=0;i--){
            QName qn=(QName)vars.elementAt(i);
            // System.out.println("qn: "+qn);
            if(qn.equals(m_qname)){
                if(i<globalsSize){
                    m_isGlobal=true;
                    m_index=i;
                }else{
                    m_index=i-globalsSize;
                }
                return;
            }
        }
        java.lang.String msg=XSLMessages.createXPATHMessage(XPATHErrorResources.ER_COULD_NOT_FIND_VAR,
                new Object[]{m_qname.toString()});
        TransformerException te=new TransformerException(msg,this);
        throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(te);
    }

    public boolean deepEquals(Expression expr){
        if(!isSameClass(expr))
            return false;
        if(!m_qname.equals(((Variable)expr).m_qname))
            return false;
        // J2SE does not support Xalan interpretive
        /**
         // We have to make sure that the qname really references
         // the same variable element.
         if(getElemVariable() != ((Variable)expr).getElemVariable())
         return false;
         */
        return true;
    }

    public int getAnalysisBits(){
        // J2SE does not support Xalan interpretive
        /**
         com.sun.org.apache.xalan.internal.templates.ElemVariable vvar = getElemVariable();
         if(null != vvar)
         {
         XPath xpath = vvar.getSelect();
         if(null != xpath)
         {
         Expression expr = xpath.getExpression();
         if(null != expr && expr instanceof PathComponent)
         {
         return ((PathComponent)expr).getAnalysisBits();
         }
         }
         }
         */
        return WalkerFactory.BIT_FILTER;
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        visitor.visitVariableRef(owner,this);
    }

    public boolean isPsuedoVarRef(){
        java.lang.String ns=m_qname.getNamespaceURI();
        if((null!=ns)&&ns.equals(PSUEDOVARNAMESPACE)){
            if(m_qname.getLocalName().startsWith("#"))
                return true;
        }
        return false;
    }
}
