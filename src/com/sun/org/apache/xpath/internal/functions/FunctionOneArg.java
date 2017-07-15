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
 * $Id: FunctionOneArg.java,v 1.2.4.1 2005/09/14 20:18:45 jeffsuttor Exp $
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
 * $Id: FunctionOneArg.java,v 1.2.4.1 2005/09/14 20:18:45 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.functions;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathVisitor;

public class FunctionOneArg extends Function implements ExpressionOwner{
    static final long serialVersionUID=-5180174180765609758L;
    Expression m_arg0;

    public Expression getArg0(){
        return m_arg0;
    }

    public void setArg(Expression arg,int argNum)
            throws WrongNumberArgsException{
        if(0==argNum){
            m_arg0=arg;
            arg.exprSetParent(this);
        }else
            reportWrongNumberArgs();
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException{
        if(argNum!=1)
            reportWrongNumberArgs();
    }

    protected void reportWrongNumberArgs() throws WrongNumberArgsException{
        throw new WrongNumberArgsException(XSLMessages.createXPATHMessage("one",null));
    }

    public void callArgVisitors(XPathVisitor visitor){
        if(null!=m_arg0)
            m_arg0.callVisitors(this,visitor);
    }

    public boolean deepEquals(Expression expr){
        if(!super.deepEquals(expr))
            return false;
        if(null!=m_arg0){
            if(null==((FunctionOneArg)expr).m_arg0)
                return false;
            if(!m_arg0.deepEquals(((FunctionOneArg)expr).m_arg0))
                return false;
        }else if(null!=((FunctionOneArg)expr).m_arg0)
            return false;
        return true;
    }

    public boolean canTraverseOutsideSubtree(){
        return m_arg0.canTraverseOutsideSubtree();
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        if(null!=m_arg0)
            m_arg0.fixupVariables(vars,globalsSize);
    }

    public Expression getExpression(){
        return m_arg0;
    }

    public void setExpression(Expression exp){
        exp.exprSetParent(this);
        m_arg0=exp;
    }
}
