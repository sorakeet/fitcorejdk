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
 * $Id: ContainsCall.java,v 1.2.4.1 2005/09/01 12:12:06 pvedula Exp $
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
 * $Id: ContainsCall.java,v 1.2.4.1 2005/09/01 12:12:06 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.org.apache.bcel.internal.generic.IFLT;
import com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import com.sun.org.apache.bcel.internal.generic.InstructionList;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.*;

import java.util.Vector;

final class ContainsCall extends FunctionCall{
    private Expression _base=null;
    private Expression _token=null;

    public ContainsCall(QName fname,Vector arguments){
        super(fname,arguments);
    }

    public boolean isBoolean(){
        return true;
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError{
        // Check that the function was passed exactly two arguments
        if(argumentCount()!=2){
            throw new TypeCheckError(ErrorMsg.ILLEGAL_ARG_ERR,getName(),this);
        }
        // The first argument must be a String, or cast to a String
        _base=argument(0);
        Type baseType=_base.typeCheck(stable);
        if(baseType!=Type.String)
            _base=new CastExpr(_base,Type.String);
        // The second argument must also be a String, or cast to a String
        _token=argument(1);
        Type tokenType=_token.typeCheck(stable);
        if(tokenType!=Type.String)
            _token=new CastExpr(_token,Type.String);
        return _type=Type.Boolean;
    }

    public void translateDesynthesized(ClassGenerator classGen,
                                       MethodGenerator methodGen){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        _base.translate(classGen,methodGen);
        _token.translate(classGen,methodGen);
        il.append(new INVOKEVIRTUAL(cpg.addMethodref(STRING_CLASS,
                "indexOf",
                "("+STRING_SIG+")I")));
        _falseList.add(il.append(new IFLT(null)));
    }

    public void translate(ClassGenerator classGen,MethodGenerator methodGen){
        translateDesynthesized(classGen,methodGen);
        synthesize(classGen,methodGen);
    }
}
