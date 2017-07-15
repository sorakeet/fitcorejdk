/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: UnsupportedElement.java,v 1.2.4.1 2005/09/05 09:26:51 pvedula Exp $
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * $Id: UnsupportedElement.java,v 1.2.4.1 2005/09/05 09:26:51 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import com.sun.org.apache.bcel.internal.generic.InstructionList;
import com.sun.org.apache.bcel.internal.generic.PUSH;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.*;

import java.util.List;
import java.util.Vector;

final class UnsupportedElement extends SyntaxTreeNode{
    private Vector _fallbacks=null;
    private ErrorMsg _message=null;
    private boolean _isExtension=false;

    public UnsupportedElement(String uri,String prefix,String local,boolean isExtension){
        super(uri,prefix,local);
        _isExtension=isExtension;
    }

    public void setErrorMessage(ErrorMsg message){
        _message=message;
    }

    public void parseContents(Parser parser){
        processFallbacks(parser);
    }

    private void processFallbacks(Parser parser){
        List<SyntaxTreeNode> children=getContents();
        if(children!=null){
            final int count=children.size();
            for(int i=0;i<count;i++){
                SyntaxTreeNode child=children.get(i);
                if(child instanceof Fallback){
                    Fallback fallback=(Fallback)child;
                    fallback.activate();
                    fallback.parseContents(parser);
                    if(_fallbacks==null){
                        _fallbacks=new Vector();
                    }
                    _fallbacks.addElement(child);
                }
            }
        }
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError{
        if(_fallbacks!=null){
            int count=_fallbacks.size();
            for(int i=0;i<count;i++){
                Fallback fallback=(Fallback)_fallbacks.elementAt(i);
                fallback.typeCheck(stable);
            }
        }
        return Type.Void;
    }

    public void translate(ClassGenerator classGen,MethodGenerator methodGen){
        if(_fallbacks!=null){
            int count=_fallbacks.size();
            for(int i=0;i<count;i++){
                Fallback fallback=(Fallback)_fallbacks.elementAt(i);
                fallback.translate(classGen,methodGen);
            }
        }
        // We only go into the else block in forward-compatibility mode, when
        // the unsupported element has no fallback.
        else{
            // If the unsupported element does not have any fallback child, then
            // at runtime, a runtime error should be raised when the unsupported
            // element is instantiated. Otherwise, no error is thrown.
            ConstantPoolGen cpg=classGen.getConstantPool();
            InstructionList il=methodGen.getInstructionList();
            final int unsupportedElem=cpg.addMethodref(BASIS_LIBRARY_CLASS,"unsupported_ElementF",
                    "("+STRING_SIG+"Z)V");
            il.append(new PUSH(cpg,getQName().toString()));
            il.append(new PUSH(cpg,_isExtension));
            il.append(new INVOKESTATIC(unsupportedElem));
        }
    }

    public void display(int indent){
        indent(indent);
        Util.println("Unsupported element = "+_qname.getNamespace()+
                ":"+_qname.getLocalPart());
        displayContents(indent+IndentIncrement);
    }
}
