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
 * $Id: FunctionMultiArgs.java,v 1.2.4.1 2005/09/14 20:18:43 jeffsuttor Exp $
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
 * $Id: FunctionMultiArgs.java,v 1.2.4.1 2005/09/14 20:18:43 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.functions;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

public class FunctionMultiArgs extends Function3Args{
    static final long serialVersionUID=7117257746138417181L;
    Expression[] m_args;

    public Expression[] getArgs(){
        return m_args;
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        super.fixupVariables(vars,globalsSize);
        if(null!=m_args){
            for(int i=0;i<m_args.length;i++){
                m_args[i].fixupVariables(vars,globalsSize);
            }
        }
    }

    public void setArg(Expression arg,int argNum)
            throws WrongNumberArgsException{
        if(argNum<3)
            super.setArg(arg,argNum);
        else{
            if(null==m_args){
                m_args=new Expression[1];
                m_args[0]=arg;
            }else{
                // Slow but space conservative.
                Expression[] args=new Expression[m_args.length+1];
                System.arraycopy(m_args,0,args,0,m_args.length);
                args[m_args.length]=arg;
                m_args=args;
            }
            arg.exprSetParent(this);
        }
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException{
    }

    protected void reportWrongNumberArgs() throws WrongNumberArgsException{
        String fMsg=XSLMessages.createXPATHMessage(
                XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION,
                new Object[]{"Programmer's assertion:  the method FunctionMultiArgs.reportWrongNumberArgs() should never be called."});
        throw new RuntimeException(fMsg);
    }

    public boolean canTraverseOutsideSubtree(){
        if(super.canTraverseOutsideSubtree())
            return true;
        else{
            int n=m_args.length;
            for(int i=0;i<n;i++){
                if(m_args[i].canTraverseOutsideSubtree())
                    return true;
            }
            return false;
        }
    }

    public void callArgVisitors(XPathVisitor visitor){
        super.callArgVisitors(visitor);
        if(null!=m_args){
            int n=m_args.length;
            for(int i=0;i<n;i++){
                m_args[i].callVisitors(new ArgMultiOwner(i),visitor);
            }
        }
    }

    public boolean deepEquals(Expression expr){
        if(!super.deepEquals(expr))
            return false;
        FunctionMultiArgs fma=(FunctionMultiArgs)expr;
        if(null!=m_args){
            int n=m_args.length;
            if((null==fma)||(fma.m_args.length!=n))
                return false;
            for(int i=0;i<n;i++){
                if(!m_args[i].deepEquals(fma.m_args[i]))
                    return false;
            }
        }else if(null!=fma.m_args){
            return false;
        }
        return true;
    }

    class ArgMultiOwner implements ExpressionOwner{
        int m_argIndex;

        ArgMultiOwner(int index){
            m_argIndex=index;
        }

        public Expression getExpression(){
            return m_args[m_argIndex];
        }

        public void setExpression(Expression exp){
            exp.exprSetParent(FunctionMultiArgs.this);
            m_args[m_argIndex]=exp;
        }
    }
}
