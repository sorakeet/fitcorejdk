/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2005 The Apache Software Foundation.
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
 * $Id: Step.java,v 1.6 2006/06/06 22:34:34 spericas Exp $
 */
/**
 * Copyright 2001-2005 The Apache Software Foundation.
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
 * $Id: Step.java,v 1.6 2006/06/06 22:34:34 spericas Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.*;
import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.*;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import com.sun.org.apache.xml.internal.dtm.Axis;
import com.sun.org.apache.xml.internal.dtm.DTM;

import java.util.Vector;

final class Step extends RelativeLocationPath{
    private int _axis;
    private Vector _predicates;
    private boolean _hadPredicates=false;
    private int _nodeType;

    public Step(int axis,int nodeType,Vector predicates){
        _axis=axis;
        _nodeType=nodeType;
        _predicates=predicates;
    }

    public void setParser(Parser parser){
        super.setParser(parser);
        if(_predicates!=null){
            final int n=_predicates.size();
            for(int i=0;i<n;i++){
                final Predicate exp=(Predicate)_predicates.elementAt(i);
                exp.setParser(parser);
                exp.setParent(this);
            }
        }
    }

    public int getAxis(){
        return _axis;
    }

    public void setAxis(int axis){
        _axis=axis;
    }

    public int getNodeType(){
        return _nodeType;
    }

    public Vector getPredicates(){
        return _predicates;
    }

    public void addPredicates(Vector predicates){
        if(_predicates==null){
            _predicates=predicates;
        }else{
            _predicates.addAll(predicates);
        }
    }

    private boolean isPredicate(){
        SyntaxTreeNode parent=this;
        while(parent!=null){
            parent=parent.getParent();
            if(parent instanceof Predicate) return true;
        }
        return false;
    }

    public boolean isAbbreviatedDDot(){
        return _nodeType==NodeTest.ANODE&&_axis==Axis.PARENT;
    }

    private void translateStep(ClassGenerator classGen,
                               MethodGenerator methodGen,
                               int predicateIndex){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        if(predicateIndex>=0){
            translatePredicates(classGen,methodGen,predicateIndex);
        }else{
            int star=0;
            String name=null;
            final XSLTC xsltc=getParser().getXSLTC();
            if(_nodeType>=DTM.NTYPES){
                final Vector ni=xsltc.getNamesIndex();
                name=(String)ni.elementAt(_nodeType-DTM.NTYPES);
                star=name.lastIndexOf('*');
            }
            // If it is an attribute, but not '@*', '@pre:*' or '@node()',
            // and has no parent
            if(_axis==Axis.ATTRIBUTE&&_nodeType!=NodeTest.ATTRIBUTE
                    &&_nodeType!=NodeTest.ANODE&&!hasParentPattern()
                    &&star==0){
                int iter=cpg.addInterfaceMethodref(DOM_INTF,
                        "getTypedAxisIterator",
                        "(II)"+NODE_ITERATOR_SIG);
                il.append(methodGen.loadDOM());
                il.append(new PUSH(cpg,Axis.ATTRIBUTE));
                il.append(new PUSH(cpg,_nodeType));
                il.append(new INVOKEINTERFACE(iter,3));
                return;
            }
            SyntaxTreeNode parent=getParent();
            // Special case for '.'
            if(isAbbreviatedDot()){
                if(_type==Type.Node){
                    // Put context node on stack if using Type.Node
                    il.append(methodGen.loadContextNode());
                }else{
                    if(parent instanceof ParentLocationPath){
                        // Wrap the context node in a singleton iterator if not.
                        int init=cpg.addMethodref(SINGLETON_ITERATOR,
                                "<init>",
                                "("+NODE_SIG+")V");
                        il.append(new NEW(cpg.addClass(SINGLETON_ITERATOR)));
                        il.append(DUP);
                        il.append(methodGen.loadContextNode());
                        il.append(new INVOKESPECIAL(init));
                    }else{
                        // DOM.getAxisIterator(int axis);
                        int git=cpg.addInterfaceMethodref(DOM_INTF,
                                "getAxisIterator",
                                "(I)"+NODE_ITERATOR_SIG);
                        il.append(methodGen.loadDOM());
                        il.append(new PUSH(cpg,_axis));
                        il.append(new INVOKEINTERFACE(git,2));
                    }
                }
                return;
            }
            // Special case for /foo/**/bar
            if((parent instanceof ParentLocationPath)&&
                    (parent.getParent() instanceof ParentLocationPath)){
                if((_nodeType==NodeTest.ELEMENT)&&(!_hadPredicates)){
                    _nodeType=NodeTest.ANODE;
                }
            }
            // "ELEMENT" or "*" or "@*" or ".." or "@attr" with a parent.
            switch(_nodeType){
                case NodeTest.ATTRIBUTE:
                    _axis=Axis.ATTRIBUTE;
                case NodeTest.ANODE:
                    // DOM.getAxisIterator(int axis);
                    int git=cpg.addInterfaceMethodref(DOM_INTF,
                            "getAxisIterator",
                            "(I)"+NODE_ITERATOR_SIG);
                    il.append(methodGen.loadDOM());
                    il.append(new PUSH(cpg,_axis));
                    il.append(new INVOKEINTERFACE(git,2));
                    break;
                default:
                    if(star>1){
                        final String namespace;
                        if(_axis==Axis.ATTRIBUTE)
                            namespace=name.substring(0,star-2);
                        else
                            namespace=name.substring(0,star-1);
                        final int nsType=xsltc.registerNamespace(namespace);
                        final int ns=cpg.addInterfaceMethodref(DOM_INTF,
                                "getNamespaceAxisIterator",
                                "(II)"+NODE_ITERATOR_SIG);
                        il.append(methodGen.loadDOM());
                        il.append(new PUSH(cpg,_axis));
                        il.append(new PUSH(cpg,nsType));
                        il.append(new INVOKEINTERFACE(ns,3));
                        break;
                    }
                case NodeTest.ELEMENT:
                    // DOM.getTypedAxisIterator(int axis, int type);
                    final int ty=cpg.addInterfaceMethodref(DOM_INTF,
                            "getTypedAxisIterator",
                            "(II)"+NODE_ITERATOR_SIG);
                    // Get the typed iterator we're after
                    il.append(methodGen.loadDOM());
                    il.append(new PUSH(cpg,_axis));
                    il.append(new PUSH(cpg,_nodeType));
                    il.append(new INVOKEINTERFACE(ty,3));
                    break;
            }
        }
    }

    public void translatePredicates(ClassGenerator classGen,
                                    MethodGenerator methodGen,
                                    int predicateIndex){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        int idx=0;
        if(predicateIndex<0){
            translateStep(classGen,methodGen,predicateIndex);
        }else{
            final Predicate predicate=(Predicate)_predicates.get(predicateIndex--);
            // Special case for predicates that can use the NodeValueIterator
            // instead of an auxiliary class. Certain path/predicates pairs
            // are translated into a base path, on top of which we place a
            // node value iterator that tests for the desired value:
            //   foo[@attr = 'str']  ->  foo/@attr + test(value='str')
            //   foo[bar = 'str']    ->  foo/bar + test(value='str')
            //   foo/bar[. = 'str']  ->  foo/bar + test(value='str')
            if(predicate.isNodeValueTest()){
                Step step=predicate.getStep();
                il.append(methodGen.loadDOM());
                // If the predicate's Step is simply '.' we translate this Step
                // and place the node test on top of the resulting iterator
                if(step.isAbbreviatedDot()){
                    translateStep(classGen,methodGen,predicateIndex);
                    il.append(new ICONST(DOM.RETURN_CURRENT));
                }
                // Otherwise we create a parent location path with this Step and
                // the predicates Step, and place the node test on top of that
                else{
                    ParentLocationPath path=new ParentLocationPath(this,step);
                    _parent=step._parent=path;      // Force re-parenting
                    try{
                        path.typeCheck(getParser().getSymbolTable());
                    }catch(TypeCheckError e){
                    }
                    translateStep(classGen,methodGen,predicateIndex);
                    path.translateStep(classGen,methodGen);
                    il.append(new ICONST(DOM.RETURN_PARENT));
                }
                predicate.translate(classGen,methodGen);
                idx=cpg.addInterfaceMethodref(DOM_INTF,
                        GET_NODE_VALUE_ITERATOR,
                        GET_NODE_VALUE_ITERATOR_SIG);
                il.append(new INVOKEINTERFACE(idx,5));
            }
            // Handle '//*[n]' expression
            else if(predicate.isNthDescendant()){
                il.append(methodGen.loadDOM());
                // il.append(new ICONST(NodeTest.ELEMENT));
                il.append(new PUSH(cpg,predicate.getPosType()));
                predicate.translate(classGen,methodGen);
                il.append(new ICONST(0));
                idx=cpg.addInterfaceMethodref(DOM_INTF,
                        "getNthDescendant",
                        "(IIZ)"+NODE_ITERATOR_SIG);
                il.append(new INVOKEINTERFACE(idx,4));
            }
            // Handle 'elem[n]' expression
            else if(predicate.isNthPositionFilter()){
                idx=cpg.addMethodref(NTH_ITERATOR_CLASS,
                        "<init>",
                        "("+NODE_ITERATOR_SIG+"I)V");
                // Backwards branches are prohibited if an uninitialized object
                // is on the stack by section 4.9.4 of the JVM Specification,
                // 2nd Ed.  We don't know whether this code might contain
                // backwards branches, so we mustn't create the new object until
                // after we've created the suspect arguments to its constructor.
                // Instead we calculate the values of the arguments to the
                // constructor first, store them in temporary variables, create
                // the object and reload the arguments from the temporaries to
                // avoid the problem.
                translatePredicates(classGen,methodGen,predicateIndex); // recursive call
                LocalVariableGen iteratorTemp
                        =methodGen.addLocalVariable("step_tmp1",
                        Util.getJCRefType(NODE_ITERATOR_SIG),
                        null,null);
                iteratorTemp.setStart(
                        il.append(new ASTORE(iteratorTemp.getIndex())));
                predicate.translate(classGen,methodGen);
                LocalVariableGen predicateValueTemp
                        =methodGen.addLocalVariable("step_tmp2",
                        Util.getJCRefType("I"),
                        null,null);
                predicateValueTemp.setStart(
                        il.append(new ISTORE(predicateValueTemp.getIndex())));
                il.append(new NEW(cpg.addClass(NTH_ITERATOR_CLASS)));
                il.append(DUP);
                iteratorTemp.setEnd(
                        il.append(new ALOAD(iteratorTemp.getIndex())));
                predicateValueTemp.setEnd(
                        il.append(new ILOAD(predicateValueTemp.getIndex())));
                il.append(new INVOKESPECIAL(idx));
            }else{
                idx=cpg.addMethodref(CURRENT_NODE_LIST_ITERATOR,
                        "<init>",
                        "("
                                +NODE_ITERATOR_SIG
                                +CURRENT_NODE_LIST_FILTER_SIG
                                +NODE_SIG
                                +TRANSLET_SIG
                                +")V");
                // Backwards branches are prohibited if an uninitialized object
                // is on the stack by section 4.9.4 of the JVM Specification,
                // 2nd Ed.  We don't know whether this code might contain
                // backwards branches, so we mustn't create the new object until
                // after we've created the suspect arguments to its constructor.
                // Instead we calculate the values of the arguments to the
                // constructor first, store them in temporary variables, create
                // the object and reload the arguments from the temporaries to
                // avoid the problem.
                translatePredicates(classGen,methodGen,predicateIndex); // recursive call
                LocalVariableGen iteratorTemp
                        =methodGen.addLocalVariable("step_tmp1",
                        Util.getJCRefType(NODE_ITERATOR_SIG),
                        null,null);
                iteratorTemp.setStart(
                        il.append(new ASTORE(iteratorTemp.getIndex())));
                predicate.translateFilter(classGen,methodGen);
                LocalVariableGen filterTemp
                        =methodGen.addLocalVariable("step_tmp2",
                        Util.getJCRefType(CURRENT_NODE_LIST_FILTER_SIG),
                        null,null);
                filterTemp.setStart(
                        il.append(new ASTORE(filterTemp.getIndex())));
                // create new CurrentNodeListIterator
                il.append(new NEW(cpg.addClass(CURRENT_NODE_LIST_ITERATOR)));
                il.append(DUP);
                iteratorTemp.setEnd(
                        il.append(new ALOAD(iteratorTemp.getIndex())));
                filterTemp.setEnd(il.append(new ALOAD(filterTemp.getIndex())));
                il.append(methodGen.loadCurrentNode());
                il.append(classGen.loadTranslet());
                if(classGen.isExternal()){
                    final String className=classGen.getClassName();
                    il.append(new CHECKCAST(cpg.addClass(className)));
                }
                il.append(new INVOKESPECIAL(idx));
            }
        }
    }

    public String toString(){
        final StringBuffer buffer=new StringBuffer("step(\"");
        buffer.append(Axis.getNames(_axis)).append("\", ").append(_nodeType);
        if(_predicates!=null){
            final int n=_predicates.size();
            for(int i=0;i<n;i++){
                final Predicate pred=(Predicate)_predicates.elementAt(i);
                buffer.append(", ").append(pred.toString());
            }
        }
        return buffer.append(')').toString();
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError{
        // Save this value for later - important for testing for special
        // combinations of steps and patterns than can be optimised
        _hadPredicates=hasPredicates();
        // Special case for '.'
        //   in the case where '.' has a context such as book/.
        //   or .[false()] we can not optimize the nodeset to a single node.
        if(isAbbreviatedDot()){
            _type=(hasParentPattern()||hasPredicates()||hasParentLocationPath())?
                    Type.NodeSet:Type.Node;
        }else{
            _type=Type.NodeSet;
        }
        // Type check all predicates (expressions applied to the step)
        if(_predicates!=null){
            final int n=_predicates.size();
            for(int i=0;i<n;i++){
                final Expression pred=(Expression)_predicates.elementAt(i);
                pred.typeCheck(stable);
            }
        }
        // Return either Type.Node or Type.NodeSet
        return _type;
    }

    private boolean hasParentPattern(){
        final SyntaxTreeNode parent=getParent();
        return (parent instanceof ParentPattern||
                parent instanceof ParentLocationPath||
                parent instanceof UnionPathExpr||
                parent instanceof FilterParentPath);
    }

    private boolean hasParentLocationPath(){
        return getParent() instanceof ParentLocationPath;
    }

    private boolean hasPredicates(){
        return _predicates!=null&&_predicates.size()>0;
    }

    public boolean isAbbreviatedDot(){
        return _nodeType==NodeTest.ANODE&&_axis==Axis.SELF;
    }

    public void translate(ClassGenerator classGen,MethodGenerator methodGen){
        translateStep(classGen,methodGen,hasPredicates()?_predicates.size()-1:-1);
    }
}
