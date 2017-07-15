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
 * $Id: BooleanCall.java,v 1.2.4.1 2005/09/01 11:43:50 pvedula Exp $
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
 * $Id: BooleanCall.java,v 1.2.4.1 2005/09/01 11:43:50 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

import java.util.Vector;

final class BooleanCall extends FunctionCall{
    private Expression _arg=null;

    public BooleanCall(QName fname,Vector arguments){
        super(fname,arguments);
        _arg=argument(0);
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError{
        _arg.typeCheck(stable);
        return _type=Type.Boolean;
    }

    public void translate(ClassGenerator classGen,MethodGenerator methodGen){
        _arg.translate(classGen,methodGen);
        final Type targ=_arg.getType();
        if(!targ.identicalTo(Type.Boolean)){
            _arg.startIterator(classGen,methodGen);
            targ.translateTo(classGen,methodGen,Type.Boolean);
        }
    }
}
