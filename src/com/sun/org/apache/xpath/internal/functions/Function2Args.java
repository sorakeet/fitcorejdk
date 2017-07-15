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
 * $Id: Function2Args.java,v 1.2.4.1 2005/09/14 20:18:46 jeffsuttor Exp $
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
 * $Id: Function2Args.java,v 1.2.4.1 2005/09/14 20:18:46 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.functions;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathVisitor;

public class Function2Args extends FunctionOneArg{
    static final long serialVersionUID=5574294996842710641L;
    Expression m_arg1;

    public Expression getArg1(){
        return m_arg1;
    }

    public void setArg(Expression arg,int argNum)
            throws WrongNumberArgsException{
        // System.out.println("argNum: "+argNum);
        if(argNum==0)
            super.setArg(arg,argNum);
        else if(1==argNum){
            m_arg1=arg;
            arg.exprSetParent(this);
        }else
            reportWrongNumberArgs();
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException{
        if(argNum!=2)
            reportWrongNumberArgs();
    }

    protected void reportWrongNumberArgs() throws WrongNumberArgsException{
        throw new WrongNumberArgsException(XSLMessages.createXPATHMessage("two",null));
    }

    public boolean canTraverseOutsideSubtree(){
        return super.canTraverseOutsideSubtree()
                ?true:m_arg1.canTraverseOutsideSubtree();
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        super.fixupVariables(vars,globalsSize);
        if(null!=m_arg1)
            m_arg1.fixupVariables(vars,globalsSize);
    }

    public void callArgVisitors(XPathVisitor visitor){
        super.callArgVisitors(visitor);
        if(null!=m_arg1)
            m_arg1.callVisitors(new Arg1Owner(),visitor);
    }

    public boolean deepEquals(Expression expr){
        if(!super.deepEquals(expr))
            return false;
        if(null!=m_arg1){
            if(null==((Function2Args)expr).m_arg1)
                return false;
            if(!m_arg1.deepEquals(((Function2Args)expr).m_arg1))
                return false;
        }else if(null!=((Function2Args)expr).m_arg1)
            return false;
        return true;
    }

    class Arg1Owner implements ExpressionOwner{
        public Expression getExpression(){
            return m_arg1;
        }

        public void setExpression(Expression exp){
            exp.exprSetParent(Function2Args.this);
            m_arg1=exp;
        }
    }
}
