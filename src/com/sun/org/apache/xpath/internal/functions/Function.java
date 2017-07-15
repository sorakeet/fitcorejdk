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
 * $Id: Function.java,v 1.2.4.1 2005/09/14 20:18:43 jeffsuttor Exp $
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
 * $Id: Function.java,v 1.2.4.1 2005/09/14 20:18:43 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.functions;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.compiler.Compiler;
import com.sun.org.apache.xpath.internal.objects.XObject;

public abstract class Function extends Expression{
    static final long serialVersionUID=6927661240854599768L;

    public void setArg(Expression arg,int argNum)
            throws WrongNumberArgsException{
        // throw new WrongNumberArgsException(XSLMessages.createXPATHMessage("zero", null));
        reportWrongNumberArgs();
    }

    protected void reportWrongNumberArgs() throws WrongNumberArgsException{
        throw new WrongNumberArgsException(XSLMessages.createXPATHMessage("zero",null));
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException{
        if(argNum!=0)
            reportWrongNumberArgs();
    }

    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException{
        // Programmer's assert.  (And, no, I don't want the method to be abstract).
        System.out.println("Error! Function.execute should not be called!");
        return null;
    }

    public boolean deepEquals(Expression expr){
        if(!isSameClass(expr))
            return false;
        return true;
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        if(visitor.visitFunction(owner,this)){
            callArgVisitors(visitor);
        }
    }

    public void callArgVisitors(XPathVisitor visitor){
    }

    public void postCompileStep(Compiler compiler){
        // no default action
    }
}
