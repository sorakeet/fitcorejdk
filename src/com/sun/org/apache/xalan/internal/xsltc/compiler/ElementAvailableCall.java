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
 * $Id: ElementAvailableCall.java,v 1.2.4.1 2005/09/01 14:13:01 pvedula Exp $
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
 * $Id: ElementAvailableCall.java,v 1.2.4.1 2005/09/01 14:13:01 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.org.apache.bcel.internal.generic.PUSH;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.*;

import java.util.Vector;

final class ElementAvailableCall extends FunctionCall{
    public ElementAvailableCall(QName fname,Vector arguments){
        super(fname,arguments);
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError{
        if(argument() instanceof LiteralExpr){
            return _type=Type.Boolean;
        }
        ErrorMsg err=new ErrorMsg(ErrorMsg.NEED_LITERAL_ERR,
                "element-available",this);
        throw new TypeCheckError(err);
    }

    public void translate(ClassGenerator classGen,MethodGenerator methodGen){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final boolean result=getResult();
        methodGen.getInstructionList().append(new PUSH(cpg,result));
    }

    public Object evaluateAtCompileTime(){
        return getResult()?Boolean.TRUE:Boolean.FALSE;
    }

    public boolean getResult(){
        try{
            final LiteralExpr arg=(LiteralExpr)argument();
            final String qname=arg.getValue();
            final int index=qname.indexOf(':');
            final String localName=(index>0)?
                    qname.substring(index+1):qname;
            return getParser().elementSupported(arg.getNamespace(),
                    localName);
        }catch(ClassCastException e){
            return false;
        }
    }
}
