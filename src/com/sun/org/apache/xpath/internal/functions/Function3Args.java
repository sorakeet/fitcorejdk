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
 * $Id: Function3Args.java,v 1.2.4.1 2005/09/14 20:18:42 jeffsuttor Exp $
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
 * $Id: Function3Args.java,v 1.2.4.1 2005/09/14 20:18:42 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.functions;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathVisitor;

public class Function3Args extends Function2Args{
    static final long serialVersionUID=7915240747161506646L;
    Expression m_arg2;

    public Expression getArg2(){
        return m_arg2;
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        super.fixupVariables(vars,globalsSize);
        if(null!=m_arg2)
            m_arg2.fixupVariables(vars,globalsSize);
    }

    public void setArg(Expression arg,int argNum)
            throws WrongNumberArgsException{
        if(argNum<2)
            super.setArg(arg,argNum);
        else if(2==argNum){
            m_arg2=arg;
            arg.exprSetParent(this);
        }else
            reportWrongNumberArgs();
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException{
        if(argNum!=3)
            reportWrongNumberArgs();
    }

    protected void reportWrongNumberArgs() throws WrongNumberArgsException{
        throw new WrongNumberArgsException(XSLMessages.createXPATHMessage("three",null));
    }

    public boolean canTraverseOutsideSubtree(){
        return super.canTraverseOutsideSubtree()
                ?true:m_arg2.canTraverseOutsideSubtree();
    }

    public void callArgVisitors(XPathVisitor visitor){
        super.callArgVisitors(visitor);
        if(null!=m_arg2)
            m_arg2.callVisitors(new Arg2Owner(),visitor);
    }

    public boolean deepEquals(Expression expr){
        if(!super.deepEquals(expr))
            return false;
        if(null!=m_arg2){
            if(null==((Function3Args)expr).m_arg2)
                return false;
            if(!m_arg2.deepEquals(((Function3Args)expr).m_arg2))
                return false;
        }else if(null!=((Function3Args)expr).m_arg2)
            return false;
        return true;
    }

    class Arg2Owner implements ExpressionOwner{
        public Expression getExpression(){
            return m_arg2;
        }

        public void setExpression(Expression exp){
            exp.exprSetParent(Function3Args.this);
            m_arg2=exp;
        }
    }
}
