/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: If.java,v 1.2.4.1 2005/09/01 15:39:47 pvedula Exp $
 */
/**
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: If.java,v 1.2.4.1 2005/09/01 15:39:47 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import com.sun.org.apache.bcel.internal.generic.InstructionList;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.*;

final class If extends Instruction{
    private Expression _test;
    private boolean _ignore=false;

    public void parseContents(Parser parser){
        // Parse the "test" expression
        _test=parser.parseExpression(this,"test",null);
        // Make sure required attribute(s) have been set
        if(_test.isDummy()){
            reportError(this,parser,ErrorMsg.REQUIRED_ATTR_ERR,"test");
            return;
        }
        // Ignore xsl:if when test is false (function-available() and
        // element-available())
        Object result=_test.evaluateAtCompileTime();
        if(result!=null&&result instanceof Boolean){
            _ignore=!((Boolean)result).booleanValue();
        }
        parseChildren(parser);
    }

    public void display(int indent){
        indent(indent);
        Util.println("If");
        indent(indent+IndentIncrement);
        System.out.print("test ");
        Util.println(_test.toString());
        displayContents(indent+IndentIncrement);
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError{
        // Type-check the "test" expression
        if(_test.typeCheck(stable) instanceof BooleanType==false){
            _test=new CastExpr(_test,Type.Boolean);
        }
        // Type check the element contents
        if(!_ignore){
            typeCheckContents(stable);
        }
        return Type.Void;
    }

    public void translate(ClassGenerator classGen,MethodGenerator methodGen){
        final InstructionList il=methodGen.getInstructionList();
        _test.translateDesynthesized(classGen,methodGen);
        // remember end of condition
        final InstructionHandle truec=il.getEnd();
        if(!_ignore){
            translateContents(classGen,methodGen);
        }
        _test.backPatchFalseList(il.append(NOP));
        _test.backPatchTrueList(truec.getNext());
    }
}
