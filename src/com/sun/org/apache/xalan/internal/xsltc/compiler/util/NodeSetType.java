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
 * $Id: NodeSetType.java,v 1.2.4.1 2005/09/05 11:21:45 pvedula Exp $
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
 * $Id: NodeSetType.java,v 1.2.4.1 2005/09/05 11:21:45 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import com.sun.org.apache.bcel.internal.generic.*;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import com.sun.org.apache.xalan.internal.xsltc.compiler.FlowList;

public final class NodeSetType extends Type{
    protected NodeSetType(){
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            BooleanType type){
        final InstructionList il=methodGen.getInstructionList();
        FlowList falsel=translateToDesynthesized(classGen,methodGen,type);
        il.append(ICONST_1);
        final BranchHandle truec=il.append(new GOTO(null));
        falsel.backPatch(il.append(ICONST_0));
        truec.setTarget(il.append(NOP));
    }    public String toString(){
        return "node-set";
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            StringType type){
        final InstructionList il=methodGen.getInstructionList();
        getFirstNode(classGen,methodGen);
        il.append(DUP);
        final BranchHandle falsec=il.append(new IFLT(null));
        Type.Node.translateTo(classGen,methodGen,type);
        final BranchHandle truec=il.append(new GOTO(null));
        falsec.setTarget(il.append(POP));
        il.append(new PUSH(classGen.getConstantPool(),""));
        truec.setTarget(il.append(NOP));
    }    public boolean identicalTo(Type other){
        return this==other;
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            RealType type){
        translateTo(classGen,methodGen,Type.String);
        Type.String.translateTo(classGen,methodGen,Type.Real);
    }    public String toSignature(){
        return NODE_ITERATOR_SIG;
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            NodeType type){
        getFirstNode(classGen,methodGen);
    }    public com.sun.org.apache.bcel.internal.generic.Type toJCType(){
        return new com.sun.org.apache.bcel.internal.generic.ObjectType(NODE_ITERATOR);
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            ObjectType type){
        methodGen.getInstructionList().append(NOP);
    }    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            Type type){
        if(type==Type.String){
            translateTo(classGen,methodGen,(StringType)type);
        }else if(type==Type.Boolean){
            translateTo(classGen,methodGen,(BooleanType)type);
        }else if(type==Type.Real){
            translateTo(classGen,methodGen,(RealType)type);
        }else if(type==Type.Node){
            translateTo(classGen,methodGen,(NodeType)type);
        }else if(type==Type.Reference){
            translateTo(classGen,methodGen,(ReferenceType)type);
        }else if(type==Type.Object){
            translateTo(classGen,methodGen,(ObjectType)type);
        }else{
            ErrorMsg err=new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
                    toString(),type.toString());
            classGen.getParser().reportError(Constants.FATAL,err);
        }
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            ReferenceType type){
        methodGen.getInstructionList().append(NOP);
    }    public void translateFrom(ClassGenerator classGen,
                              MethodGenerator methodGen,Class clazz){
        InstructionList il=methodGen.getInstructionList();
        ConstantPoolGen cpg=classGen.getConstantPool();
        if(clazz.getName().equals("org.w3c.dom.NodeList")){
            // w3c NodeList is on the stack from the external Java function call.
            // call BasisFunction to consume NodeList and leave Iterator on
            //    the stack.
            il.append(classGen.loadTranslet());   // push translet onto stack
            il.append(methodGen.loadDOM());       // push DOM onto stack
            final int convert=cpg.addMethodref(BASIS_LIBRARY_CLASS,
                    "nodeList2Iterator",
                    "("
                            +"Lorg/w3c/dom/NodeList;"
                            +TRANSLET_INTF_SIG
                            +DOM_INTF_SIG
                            +")"+NODE_ITERATOR_SIG);
            il.append(new INVOKESTATIC(convert));
        }else if(clazz.getName().equals("org.w3c.dom.Node")){
            // w3c Node is on the stack from the external Java function call.
            // call BasisLibrary.node2Iterator() to consume Node and leave
            // Iterator on the stack.
            il.append(classGen.loadTranslet());   // push translet onto stack
            il.append(methodGen.loadDOM());       // push DOM onto stack
            final int convert=cpg.addMethodref(BASIS_LIBRARY_CLASS,
                    "node2Iterator",
                    "("
                            +"Lorg/w3c/dom/Node;"
                            +TRANSLET_INTF_SIG
                            +DOM_INTF_SIG
                            +")"+NODE_ITERATOR_SIG);
            il.append(new INVOKESTATIC(convert));
        }else{
            ErrorMsg err=new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
                    toString(),clazz.getName());
            classGen.getParser().reportError(Constants.FATAL,err);
        }
    }

    private void getFirstNode(ClassGenerator classGen,MethodGenerator methodGen){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        il.append(new INVOKEINTERFACE(cpg.addInterfaceMethodref(NODE_ITERATOR,
                NEXT,
                NEXT_SIG),1));
    }









    public FlowList translateToDesynthesized(ClassGenerator classGen,
                                             MethodGenerator methodGen,
                                             BooleanType type){
        final InstructionList il=methodGen.getInstructionList();
        getFirstNode(classGen,methodGen);
        return new FlowList(il.append(new IFLT(null)));
    }



    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            Class clazz){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        final String className=clazz.getName();
        il.append(methodGen.loadDOM());
        il.append(SWAP);
        if(className.equals("org.w3c.dom.Node")){
            int index=cpg.addInterfaceMethodref(DOM_INTF,
                    MAKE_NODE,
                    MAKE_NODE_SIG2);
            il.append(new INVOKEINTERFACE(index,2));
        }else if(className.equals("org.w3c.dom.NodeList")||
                className.equals("java.lang.Object")){
            int index=cpg.addInterfaceMethodref(DOM_INTF,
                    MAKE_NODE_LIST,
                    MAKE_NODE_LIST_SIG2);
            il.append(new INVOKEINTERFACE(index,2));
        }else if(className.equals("java.lang.String")){
            int next=cpg.addInterfaceMethodref(NODE_ITERATOR,
                    "next","()I");
            int index=cpg.addInterfaceMethodref(DOM_INTF,
                    GET_NODE_VALUE,
                    "(I)"+STRING_SIG);
            // Get next node from the iterator
            il.append(new INVOKEINTERFACE(next,1));
            // Get the node's string value (from the DOM)
            il.append(new INVOKEINTERFACE(index,2));
        }else{
            ErrorMsg err=new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
                    toString(),className);
            classGen.getParser().reportError(Constants.FATAL,err);
        }
    }



    public void translateBox(ClassGenerator classGen,
                             MethodGenerator methodGen){
        translateTo(classGen,methodGen,Type.Reference);
    }

    public void translateUnBox(ClassGenerator classGen,
                               MethodGenerator methodGen){
        methodGen.getInstructionList().append(NOP);
    }

    public String getClassName(){
        return (NODE_ITERATOR);
    }

    public Instruction LOAD(int slot){
        return new ALOAD(slot);
    }

    public Instruction STORE(int slot){
        return new ASTORE(slot);
    }
}
