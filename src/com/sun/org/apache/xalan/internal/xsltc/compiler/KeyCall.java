/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2006 The Apache Software Foundation.
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
 * $Id: KeyCall.java,v 1.7 2006/06/19 19:49:04 spericas Exp $
 */
/**
 * Copyright 2001-2006 The Apache Software Foundation.
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
 * $Id: KeyCall.java,v 1.7 2006/06/19 19:49:04 spericas Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import com.sun.org.apache.bcel.internal.generic.InstructionList;
import com.sun.org.apache.bcel.internal.generic.PUSH;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.*;

import java.util.Vector;

final class KeyCall extends FunctionCall{
    private Expression _name;
    private Expression _value;
    private Type _valueType; // The value's data type
    private QName _resolvedQName=null;

    public KeyCall(QName fname,Vector arguments){
        super(fname,arguments);
        switch(argumentCount()){
            case 1:
                _name=null;
                _value=argument(0);
                break;
            case 2:
                _name=argument(0);
                _value=argument(1);
                break;
            default:
                _name=_value=null;
                break;
        }
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError{
        final Type returnType=super.typeCheck(stable);
        // Run type check on the key name (first argument) - must be a string,
        // and if it is not it must be converted to one using string() rules.
        if(_name!=null){
            final Type nameType=_name.typeCheck(stable);
            if(_name instanceof LiteralExpr){
                final LiteralExpr literal=(LiteralExpr)_name;
                _resolvedQName=
                        getParser().getQNameIgnoreDefaultNs(literal.getValue());
            }else if(nameType instanceof StringType==false){
                _name=new CastExpr(_name,Type.String);
            }
        }
        // Run type check on the value for this key. This value can be of
        // any data type, so this should never cause any type-check errors.
        // If the value is a reference, then we have to defer the decision
        // of how to process it until run-time.
        // If the value is known not to be a node-set, then it should be
        // converted to a string before the lookup is done. If the value is
        // known to be a node-set then this process (convert to string, then
        // do lookup) should be applied to every node in the set, and the
        // result from all lookups should be added to the resulting node-set.
        _valueType=_value.typeCheck(stable);
        if(_valueType!=Type.NodeSet
                &&_valueType!=Type.Reference
                &&_valueType!=Type.String){
            _value=new CastExpr(_value,Type.String);
            _valueType=_value.typeCheck(stable);
        }
        // If in a top-level element, create dependency to the referenced key
        addParentDependency();
        return returnType;
    }

    public void addParentDependency(){
        // If name unknown statically, there's nothing we can do
        if(_resolvedQName==null) return;
        SyntaxTreeNode node=this;
        while(node!=null&&node instanceof TopLevelElement==false){
            node=node.getParent();
        }
        TopLevelElement parent=(TopLevelElement)node;
        if(parent!=null){
            parent.addDependency(getSymbolTable().getKey(_resolvedQName));
        }
    }

    public void translate(ClassGenerator classGen,
                          MethodGenerator methodGen){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        // Returns the KeyIndex object of a given name
        final int getKeyIndex=cpg.addMethodref(TRANSLET_CLASS,
                "getKeyIndex",
                "(Ljava/lang/String;)"+
                        KEY_INDEX_SIG);
        // KeyIndex.setDom(Dom, node) => void
        final int keyDom=cpg.addMethodref(KEY_INDEX_CLASS,
                "setDom",
                "("+DOM_INTF_SIG+"I)V");
        // Initialises a KeyIndex to return nodes with specific values
        final int getKeyIterator=
                cpg.addMethodref(KEY_INDEX_CLASS,
                        "getKeyIndexIterator",
                        "("+_valueType.toSignature()+"Z)"
                                +KEY_INDEX_ITERATOR_SIG);
        // Initialise the index specified in the first parameter of key()
        il.append(classGen.loadTranslet());
        if(_name==null){
            il.append(new PUSH(cpg,"##id"));
        }else if(_resolvedQName!=null){
            il.append(new PUSH(cpg,_resolvedQName.toString()));
        }else{
            _name.translate(classGen,methodGen);
        }
        // Generate following byte code:
        //
        //   KeyIndex ki = translet.getKeyIndex(_name)
        //   ki.setDom(translet.dom);
        //   ki.getKeyIndexIterator(_value, true)  - for key()
        //        OR
        //   ki.getKeyIndexIterator(_value, false)  - for id()
        il.append(new INVOKEVIRTUAL(getKeyIndex));
        il.append(DUP);
        il.append(methodGen.loadDOM());
        il.append(methodGen.loadCurrentNode());
        il.append(new INVOKEVIRTUAL(keyDom));
        _value.translate(classGen,methodGen);
        il.append((_name!=null)?ICONST_1:ICONST_0);
        il.append(new INVOKEVIRTUAL(getKeyIterator));
    }
}
