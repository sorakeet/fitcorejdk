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
 * $Id: FuncExtFunction.java,v 1.2.4.2 2005/09/14 20:18:43 jeffsuttor Exp $
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
 * $Id: FuncExtFunction.java,v 1.2.4.2 2005/09/14 20:18:43 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.functions;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xpath.internal.*;
import com.sun.org.apache.xpath.internal.objects.XNull;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import com.sun.org.apache.xpath.internal.res.XPATHMessages;

import java.util.Vector;

public class FuncExtFunction extends Function{
    static final long serialVersionUID=5196115554693708718L;
    String m_namespace;
    String m_extensionName;
    Object m_methodKey;
    Vector m_argVec=new Vector();

    public FuncExtFunction(String namespace,
                           String extensionName,Object methodKey){
        //try{throw new Exception("FuncExtFunction() " + namespace + " " + extensionName);} catch (Exception e){e.printStackTrace();}
        m_namespace=namespace;
        m_extensionName=extensionName;
        m_methodKey=methodKey;
    }

    public void fixupVariables(Vector vars,int globalsSize){
        if(null!=m_argVec){
            int nArgs=m_argVec.size();
            for(int i=0;i<nArgs;i++){
                Expression arg=(Expression)m_argVec.elementAt(i);
                arg.fixupVariables(vars,globalsSize);
            }
        }
    }

    public void exprSetParent(ExpressionNode n){
        super.exprSetParent(n);
        int nArgs=m_argVec.size();
        for(int i=0;i<nArgs;i++){
            Expression arg=(Expression)m_argVec.elementAt(i);
            arg.exprSetParent(n);
        }
    }

    public String getNamespace(){
        return m_namespace;
    }

    public String getFunctionName(){
        return m_extensionName;
    }

    public Object getMethodKey(){
        return m_methodKey;
    }

    public Expression getArg(int n){
        if(n>=0&&n<m_argVec.size())
            return (Expression)m_argVec.elementAt(n);
        else
            return null;
    }

    public int getArgCount(){
        return m_argVec.size();
    }

    public void setArg(Expression arg,int argNum)
            throws WrongNumberArgsException{
        m_argVec.addElement(arg);
        arg.exprSetParent(this);
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException{
    }

    protected void reportWrongNumberArgs() throws WrongNumberArgsException{
        String fMsg=XSLMessages.createXPATHMessage(
                XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION,
                new Object[]{"Programmer's assertion:  the method FunctionMultiArgs.reportWrongNumberArgs() should never be called."});
        throw new RuntimeException(fMsg);
    }

    public XObject execute(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        if(xctxt.isSecureProcessing())
            throw new javax.xml.transform.TransformerException(
                    XPATHMessages.createXPATHMessage(
                            XPATHErrorResources.ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED,
                            new Object[]{toString()}));
        XObject result;
        Vector argVec=new Vector();
        int nArgs=m_argVec.size();
        for(int i=0;i<nArgs;i++){
            Expression arg=(Expression)m_argVec.elementAt(i);
            XObject xobj=arg.execute(xctxt);
            /**
             * Should cache the arguments for func:function
             */
            xobj.allowDetachToRelease(false);
            argVec.addElement(xobj);
        }
        //dml
        ExtensionsProvider extProvider=(ExtensionsProvider)xctxt.getOwnerObject();
        Object val=extProvider.extFunction(this,argVec);
        if(null!=val){
            result=XObject.create(val,xctxt);
        }else{
            result=new XNull();
        }
        return result;
    }

    public void callArgVisitors(XPathVisitor visitor){
        for(int i=0;i<m_argVec.size();i++){
            Expression exp=(Expression)m_argVec.elementAt(i);
            exp.callVisitors(new ArgExtOwner(exp),visitor);
        }
    }

    public String toString(){
        if(m_namespace!=null&&m_namespace.length()>0)
            return "{"+m_namespace+"}"+m_extensionName;
        else
            return m_extensionName;
    }

    class ArgExtOwner implements ExpressionOwner{
        Expression m_exp;

        ArgExtOwner(Expression exp){
            m_exp=exp;
        }

        public Expression getExpression(){
            return m_exp;
        }

        public void setExpression(Expression exp){
            exp.exprSetParent(FuncExtFunction.this);
            m_exp=exp;
        }
    }
}
