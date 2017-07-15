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
 * $Id: Predicate.java,v 1.2.4.1 2005/09/12 11:02:18 pvedula Exp $
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
 * $Id: Predicate.java,v 1.2.4.1 2005/09/12 11:02:18 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.classfile.Field;
import com.sun.org.apache.bcel.internal.generic.*;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.*;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ReferenceType;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import com.sun.org.apache.xalan.internal.xsltc.runtime.Operators;

import java.util.ArrayList;

final class Predicate extends Expression implements Closure{
    int _ptype=-1;
    private Expression _exp=null;
    private boolean _canOptimize=true;
    private boolean _nthPositionFilter=false;
    private boolean _nthDescendant=false;
    private String _className=null;
    private ArrayList _closureVars=null;
    private Closure _parentClosure=null;
    private Expression _value=null;
    private Step _step=null;

    public Predicate(Expression exp){
        _exp=exp;
        _exp.setParent(this);
    }

    public void setParser(Parser parser){
        super.setParser(parser);
        _exp.setParser(parser);
    }

    public boolean isNthPositionFilter(){
        return _nthPositionFilter;
    }

    public boolean isNthDescendant(){
        return _nthDescendant;
    }

    public void dontOptimize(){
        _canOptimize=false;
    }

    public boolean inInnerClass(){
        return (_className!=null);
    }    public boolean hasPositionCall(){
        return _exp.hasPositionCall();
    }

    public Closure getParentClosure(){
        if(_parentClosure==null){
            SyntaxTreeNode node=getParent();
            do{
                if(node instanceof Closure){
                    _parentClosure=(Closure)node;
                    break;
                }
                if(node instanceof TopLevelElement){
                    break;      // way up in the tree
                }
                node=node.getParent();
            }while(node!=null);
        }
        return _parentClosure;
    }    public boolean hasLastCall(){
        return _exp.hasLastCall();
    }
    // -- Begin Closure interface --------------------

    public String getInnerClassName(){
        return _className;
    }

    public void addVariable(VariableRefBase variableRef){
        if(_closureVars==null){
            _closureVars=new ArrayList();
        }
        // Only one reference per variable
        if(!_closureVars.contains(variableRef)){
            _closureVars.add(variableRef);
            // Add variable to parent closure as well
            Closure parentClosure=getParentClosure();
            if(parentClosure!=null){
                parentClosure.addVariable(variableRef);
            }
        }
    }

    public int getPosType(){
        if(_ptype==-1){
            SyntaxTreeNode parent=getParent();
            if(parent instanceof StepPattern){
                _ptype=((StepPattern)parent).getNodeType();
            }else if(parent instanceof AbsoluteLocationPath){
                AbsoluteLocationPath path=(AbsoluteLocationPath)parent;
                Expression exp=path.getPath();
                if(exp instanceof Step){
                    _ptype=((Step)exp).getNodeType();
                }
            }else if(parent instanceof VariableRefBase){
                final VariableRefBase ref=(VariableRefBase)parent;
                final VariableBase var=ref.getVariable();
                final Expression exp=var.getExpression();
                if(exp instanceof Step){
                    _ptype=((Step)exp).getNodeType();
                }
            }else if(parent instanceof Step){
                _ptype=((Step)parent).getNodeType();
            }
        }
        return _ptype;
    }

    public boolean parentIsPattern(){
        return (getParent() instanceof Pattern);
    }
    // -- End Closure interface ----------------------

    public Expression getExpr(){
        return _exp;
    }

    public boolean isBooleanTest(){
        return (_exp instanceof BooleanExpr);
    }



    public String toString(){
        return "pred("+_exp+')';
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError{
        Type texp=_exp.typeCheck(stable);
        // We need explicit type information for reference types - no good!
        if(texp instanceof ReferenceType){
            _exp=new CastExpr(_exp,texp=Type.Real);
        }
        // A result tree fragment should not be cast directly to a number type,
        // but rather to a boolean value, and then to a numer (0 or 1).
        // Ref. section 11.2 of the XSLT 1.0 spec
        if(texp instanceof ResultTreeType){
            _exp=new CastExpr(_exp,Type.Boolean);
            _exp=new CastExpr(_exp,Type.Real);
            texp=_exp.typeCheck(stable);
        }
        // Numerical types will be converted to a position filter
        if(texp instanceof NumberType){
            // Cast any numerical types to an integer
            if(texp instanceof IntType==false){
                _exp=new CastExpr(_exp,Type.Int);
            }
            if(_canOptimize){
                // Nth position optimization. Expression must not depend on context
                _nthPositionFilter=
                        !_exp.hasLastCall()&&!_exp.hasPositionCall();
                // _nthDescendant optimization - only if _nthPositionFilter is on
                if(_nthPositionFilter){
                    SyntaxTreeNode parent=getParent();
                    _nthDescendant=(parent instanceof Step)&&
                            (parent.getParent() instanceof AbsoluteLocationPath);
                    return _type=Type.NodeSet;
                }
            }
            // Reset optimization flags
            _nthPositionFilter=_nthDescendant=false;
            // Otherwise, expand [e] to [position() = e]
            final QName position=
                    getParser().getQNameIgnoreDefaultNs("position");
            final PositionCall positionCall=
                    new PositionCall(position);
            positionCall.setParser(getParser());
            positionCall.setParent(this);
            _exp=new EqualityExpr(Operators.EQ,positionCall,
                    _exp);
            if(_exp.typeCheck(stable)!=Type.Boolean){
                _exp=new CastExpr(_exp,Type.Boolean);
            }
            return _type=Type.Boolean;
        }else{
            // All other types will be handled as boolean values
            if(texp instanceof BooleanType==false){
                _exp=new CastExpr(_exp,Type.Boolean);
            }
            return _type=Type.Boolean;
        }
    }

    private void compileFilter(ClassGenerator classGen,
                               MethodGenerator methodGen){
        TestGenerator testGen;
        LocalVariableGen local;
        FilterGenerator filterGen;
        _className=getXSLTC().getHelperClassName();
        filterGen=new FilterGenerator(_className,
                "java.lang.Object",
                toString(),
                ACC_PUBLIC|ACC_SUPER,
                new String[]{
                        CURRENT_NODE_LIST_FILTER
                },
                classGen.getStylesheet());
        final ConstantPoolGen cpg=filterGen.getConstantPool();
        final int length=(_closureVars==null)?0:_closureVars.size();
        // Add a new instance variable for each var in closure
        for(int i=0;i<length;i++){
            VariableBase var=((VariableRefBase)_closureVars.get(i)).getVariable();
            filterGen.addField(new Field(ACC_PUBLIC,
                    cpg.addUtf8(var.getEscapedName()),
                    cpg.addUtf8(var.getType().toSignature()),
                    null,cpg.getConstantPool()));
        }
        final InstructionList il=new InstructionList();
        testGen=new TestGenerator(ACC_PUBLIC|ACC_FINAL,
                com.sun.org.apache.bcel.internal.generic.Type.BOOLEAN,
                new com.sun.org.apache.bcel.internal.generic.Type[]{
                        com.sun.org.apache.bcel.internal.generic.Type.INT,
                        com.sun.org.apache.bcel.internal.generic.Type.INT,
                        com.sun.org.apache.bcel.internal.generic.Type.INT,
                        com.sun.org.apache.bcel.internal.generic.Type.INT,
                        Util.getJCRefType(TRANSLET_SIG),
                        Util.getJCRefType(NODE_ITERATOR_SIG)
                },
                new String[]{
                        "node",
                        "position",
                        "last",
                        "current",
                        "translet",
                        "iterator"
                },
                "test",_className,il,cpg);
        // Store the dom in a local variable
        local=testGen.addLocalVariable("document",
                Util.getJCRefType(DOM_INTF_SIG),
                null,null);
        final String className=classGen.getClassName();
        il.append(filterGen.loadTranslet());
        il.append(new CHECKCAST(cpg.addClass(className)));
        il.append(new GETFIELD(cpg.addFieldref(className,
                DOM_FIELD,DOM_INTF_SIG)));
        local.setStart(il.append(new ASTORE(local.getIndex())));
        // Store the dom index in the test generator
        testGen.setDomIndex(local.getIndex());
        _exp.translate(filterGen,testGen);
        il.append(IRETURN);
        filterGen.addEmptyConstructor(ACC_PUBLIC);
        filterGen.addMethod(testGen);
        getXSLTC().dumpClass(filterGen.getJavaClass());
    }



    public boolean isNodeValueTest(){
        if(!_canOptimize) return false;
        return (getStep()!=null&&getCompareValue()!=null);
    }

    public Step getStep(){
        // Returned cached value if called more than once
        if(_step!=null){
            return _step;
        }
        // Nothing to do if _exp is null
        if(_exp==null){
            return null;
        }
        // Ignore if not equality expression
        if(_exp instanceof EqualityExpr){
            EqualityExpr exp=(EqualityExpr)_exp;
            Expression left=exp.getLeft();
            Expression right=exp.getRight();
            // Unwrap and set _step if appropriate
            if(left instanceof CastExpr){
                left=((CastExpr)left).getExpr();
            }
            if(left instanceof Step){
                _step=(Step)left;
            }
            // Unwrap and set _step if appropriate
            if(right instanceof CastExpr){
                right=((CastExpr)right).getExpr();
            }
            if(right instanceof Step){
                _step=(Step)right;
            }
        }
        return _step;
    }

    public Expression getCompareValue(){
        // Returned cached value if called more than once
        if(_value!=null){
            return _value;
        }
        // Nothing to to do if _exp is null
        if(_exp==null){
            return null;
        }
        // Ignore if not an equality expression
        if(_exp instanceof EqualityExpr){
            EqualityExpr exp=(EqualityExpr)_exp;
            Expression left=exp.getLeft();
            Expression right=exp.getRight();
            // Return if left is literal string
            if(left instanceof LiteralExpr){
                _value=left;
                return _value;
            }
            // Return if left is a variable reference of type string
            if(left instanceof VariableRefBase&&
                    left.getType()==Type.String){
                _value=left;
                return _value;
            }
            // Return if right is literal string
            if(right instanceof LiteralExpr){
                _value=right;
                return _value;
            }
            // Return if left is a variable reference whose type is string
            if(right instanceof VariableRefBase&&
                    right.getType()==Type.String){
                _value=right;
                return _value;
            }
        }
        return null;
    }

    public void translateFilter(ClassGenerator classGen,
                                MethodGenerator methodGen){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        // Compile auxiliary class for filter
        compileFilter(classGen,methodGen);
        // Create new instance of filter
        il.append(new NEW(cpg.addClass(_className)));
        il.append(DUP);
        il.append(new INVOKESPECIAL(cpg.addMethodref(_className,
                "<init>","()V")));
        // Initialize closure variables
        final int length=(_closureVars==null)?0:_closureVars.size();
        for(int i=0;i<length;i++){
            VariableRefBase varRef=(VariableRefBase)_closureVars.get(i);
            VariableBase var=varRef.getVariable();
            Type varType=var.getType();
            il.append(DUP);
            // Find nearest closure implemented as an inner class
            Closure variableClosure=_parentClosure;
            while(variableClosure!=null){
                if(variableClosure.inInnerClass()) break;
                variableClosure=variableClosure.getParentClosure();
            }
            // Use getfield if in an inner class
            if(variableClosure!=null){
                il.append(ALOAD_0);
                il.append(new GETFIELD(
                        cpg.addFieldref(variableClosure.getInnerClassName(),
                                var.getEscapedName(),varType.toSignature())));
            }else{
                // Use a load of instruction if in translet class
                il.append(var.loadInstruction());
            }
            // Store variable in new closure
            il.append(new PUTFIELD(
                    cpg.addFieldref(_className,var.getEscapedName(),
                            varType.toSignature())));
        }
    }

    public void translate(ClassGenerator classGen,MethodGenerator methodGen){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        if(_nthPositionFilter||_nthDescendant){
            _exp.translate(classGen,methodGen);
        }else if(isNodeValueTest()&&(getParent() instanceof Step)){
            _value.translate(classGen,methodGen);
            il.append(new CHECKCAST(cpg.addClass(STRING_CLASS)));
            il.append(new PUSH(cpg,((EqualityExpr)_exp).getOp()));
        }else{
            translateFilter(classGen,methodGen);
        }
    }
}
