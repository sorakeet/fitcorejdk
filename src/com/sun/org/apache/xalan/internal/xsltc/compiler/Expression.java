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
 * $Id: Expression.java,v 1.2.4.1 2005/09/01 14:17:51 pvedula Exp $
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
 * $Id: Expression.java,v 1.2.4.1 2005/09/01 14:17:51 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.*;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.*;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;

import java.util.Vector;

abstract class Expression extends SyntaxTreeNode{
    protected Type _type;
    protected FlowList _trueList=new FlowList();
    protected FlowList _falseList=new FlowList();

    public Type getType(){
        return _type;
    }

    public abstract String toString();

    public boolean hasPositionCall(){
        return false;           // default should be 'false' for StepPattern
    }

    public boolean hasLastCall(){
        return false;
    }

    public Object evaluateAtCompileTime(){
        return null;
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError{
        return typeCheckContents(stable);
    }

    public void translate(ClassGenerator classGen,MethodGenerator methodGen){
        ErrorMsg msg=new ErrorMsg(ErrorMsg.NOT_IMPLEMENTED_ERR,
                getClass(),this);
        getParser().reportError(FATAL,msg);
    }

    public final InstructionList compile(ClassGenerator classGen,
                                         MethodGenerator methodGen){
        final InstructionList result, save=methodGen.getInstructionList();
        methodGen.setInstructionList(result=new InstructionList());
        translate(classGen,methodGen);
        methodGen.setInstructionList(save);
        return result;
    }

    public void translateDesynthesized(ClassGenerator classGen,
                                       MethodGenerator methodGen){
        translate(classGen,methodGen);
        if(_type instanceof BooleanType){
            desynthesize(classGen,methodGen);
        }
    }

    public void desynthesize(ClassGenerator classGen,
                             MethodGenerator methodGen){
        final InstructionList il=methodGen.getInstructionList();
        _falseList.add(il.append(new IFEQ(null)));
    }

    public void startIterator(ClassGenerator classGen,
                              MethodGenerator methodGen){
        // Ignore if type is not node-set
        if(_type instanceof NodeSetType==false){
            return;
        }
        // setStartNode() should not be called if expr is a variable ref
        Expression expr=this;
        if(expr instanceof CastExpr){
            expr=((CastExpr)expr).getExpr();
        }
        if(expr instanceof VariableRefBase==false){
            final InstructionList il=methodGen.getInstructionList();
            il.append(methodGen.loadContextNode());
            il.append(methodGen.setStartNode());
        }
    }

    public void synthesize(ClassGenerator classGen,MethodGenerator methodGen){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        _trueList.backPatch(il.append(ICONST_1));
        final BranchHandle truec=il.append(new GOTO_W(null));
        _falseList.backPatch(il.append(ICONST_0));
        truec.setTarget(il.append(NOP));
    }

    public FlowList getFalseList(){
        return _falseList;
    }

    public FlowList getTrueList(){
        return _trueList;
    }

    public void backPatchFalseList(InstructionHandle ih){
        _falseList.backPatch(ih);
    }

    public void backPatchTrueList(InstructionHandle ih){
        _trueList.backPatch(ih);
    }

    public MethodType lookupPrimop(SymbolTable stable,String op,
                                   MethodType ctype){
        MethodType result=null;
        final Vector primop=stable.lookupPrimop(op);
        if(primop!=null){
            final int n=primop.size();
            int minDistance=Integer.MAX_VALUE;
            for(int i=0;i<n;i++){
                final MethodType ptype=(MethodType)primop.elementAt(i);
                // Skip if different arity
                if(ptype.argsCount()!=ctype.argsCount()){
                    continue;
                }
                // The first method with the right arity is the default
                if(result==null){
                    result=ptype;             // default method
                }
                // Check if better than last one found
                final int distance=ctype.distanceTo(ptype);
                if(distance<minDistance){
                    minDistance=distance;
                    result=ptype;
                }
            }
        }
        return result;
    }
}
