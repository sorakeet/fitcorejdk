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
 * $Id: ResultTreeType.java,v 1.2.4.1 2005/09/05 11:30:01 pvedula Exp $
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
 * $Id: ResultTreeType.java,v 1.2.4.1 2005/09/05 11:30:01 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import com.sun.org.apache.bcel.internal.generic.*;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import com.sun.org.apache.xalan.internal.xsltc.compiler.FlowList;

public final class ResultTreeType extends Type{
    private final String _methodName;

    protected ResultTreeType(){
        _methodName=null;
    }

    public ResultTreeType(String methodName){
        _methodName=methodName;
    }

    public String toString(){
        return "result-tree";
    }

    public boolean identicalTo(Type other){
        return (other instanceof ResultTreeType);
    }

    public boolean implementedAsMethod(){
        return _methodName!=null;
    }

    public com.sun.org.apache.bcel.internal.generic.Type toJCType(){
        return Util.getJCRefType(toSignature());
    }

    public String toSignature(){
        return DOM_INTF_SIG;
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            Type type){
        if(type==Type.String){
            translateTo(classGen,methodGen,(StringType)type);
        }else if(type==Type.Boolean){
            translateTo(classGen,methodGen,(BooleanType)type);
        }else if(type==Type.Real){
            translateTo(classGen,methodGen,(RealType)type);
        }else if(type==Type.NodeSet){
            translateTo(classGen,methodGen,(NodeSetType)type);
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

    public FlowList translateToDesynthesized(ClassGenerator classGen,
                                             MethodGenerator methodGen,
                                             BooleanType type){
        final InstructionList il=methodGen.getInstructionList();
        translateTo(classGen,methodGen,Type.Boolean);
        return new FlowList(il.append(new IFEQ(null)));
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            Class clazz){
        final String className=clazz.getName();
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        if(className.equals("org.w3c.dom.Node")){
            translateTo(classGen,methodGen,Type.NodeSet);
            int index=cpg.addInterfaceMethodref(DOM_INTF,
                    MAKE_NODE,
                    MAKE_NODE_SIG2);
            il.append(new INVOKEINTERFACE(index,2));
        }else if(className.equals("org.w3c.dom.NodeList")){
            translateTo(classGen,methodGen,Type.NodeSet);
            int index=cpg.addInterfaceMethodref(DOM_INTF,
                    MAKE_NODE_LIST,
                    MAKE_NODE_LIST_SIG2);
            il.append(new INVOKEINTERFACE(index,2));
        }else if(className.equals("java.lang.Object")){
            il.append(NOP);
        }else if(className.equals("java.lang.String")){
            translateTo(classGen,methodGen,Type.String);
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
        return (DOM_INTF);
    }

    public Instruction LOAD(int slot){
        return new ALOAD(slot);
    }

    public Instruction STORE(int slot){
        return new ASTORE(slot);
    }

    public String getMethodName(){
        return _methodName;
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            BooleanType type){
        // A result tree is always 'true' when converted to a boolean value,
        // since the tree always has at least one node (the root).
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        il.append(POP);      // don't need the DOM reference
        il.append(ICONST_1); // push 'true' on the stack
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            StringType type){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        if(_methodName==null){
            int index=cpg.addInterfaceMethodref(DOM_INTF,
                    "getStringValue",
                    "()"+STRING_SIG);
            il.append(new INVOKEINTERFACE(index,1));
        }else{
            final String className=classGen.getClassName();
            final int current=methodGen.getLocalIndex("current");
            // Push required parameters
            il.append(classGen.loadTranslet());
            if(classGen.isExternal()){
                il.append(new CHECKCAST(cpg.addClass(className)));
            }
            il.append(DUP);
            il.append(new GETFIELD(cpg.addFieldref(className,"_dom",
                    DOM_INTF_SIG)));
            // Create a new instance of a StringValueHandler
            int index=cpg.addMethodref(STRING_VALUE_HANDLER,"<init>","()V");
            il.append(new NEW(cpg.addClass(STRING_VALUE_HANDLER)));
            il.append(DUP);
            il.append(DUP);
            il.append(new INVOKESPECIAL(index));
            // Store new Handler into a local variable
            final LocalVariableGen handler=
                    methodGen.addLocalVariable("rt_to_string_handler",
                            Util.getJCRefType(STRING_VALUE_HANDLER_SIG),
                            null,null);
            handler.setStart(il.append(new ASTORE(handler.getIndex())));
            // Call the method that implements this result tree
            index=cpg.addMethodref(className,_methodName,
                    "("+DOM_INTF_SIG+TRANSLET_OUTPUT_SIG+")V");
            il.append(new INVOKEVIRTUAL(index));
            // Restore new handler and call getValue()
            handler.setEnd(il.append(new ALOAD(handler.getIndex())));
            index=cpg.addMethodref(STRING_VALUE_HANDLER,
                    "getValue",
                    "()"+STRING_SIG);
            il.append(new INVOKEVIRTUAL(index));
        }
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            RealType type){
        translateTo(classGen,methodGen,Type.String);
        Type.String.translateTo(classGen,methodGen,Type.Real);
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            ReferenceType type){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        if(_methodName==null){
            il.append(NOP);
        }else{
            LocalVariableGen domBuilder, newDom;
            final String className=classGen.getClassName();
            final int current=methodGen.getLocalIndex("current");
            // Push required parameters
            il.append(classGen.loadTranslet());
            if(classGen.isExternal()){
                il.append(new CHECKCAST(cpg.addClass(className)));
            }
            il.append(methodGen.loadDOM());
            // Create new instance of DOM class (with RTF_INITIAL_SIZE nodes)
            il.append(methodGen.loadDOM());
            int index=cpg.addInterfaceMethodref(DOM_INTF,
                    "getResultTreeFrag",
                    "(IZ)"+DOM_INTF_SIG);
            il.append(new PUSH(cpg,RTF_INITIAL_SIZE));
            il.append(new PUSH(cpg,false));
            il.append(new INVOKEINTERFACE(index,3));
            il.append(DUP);
            // Store new DOM into a local variable
            newDom=methodGen.addLocalVariable("rt_to_reference_dom",
                    Util.getJCRefType(DOM_INTF_SIG),
                    null,null);
            il.append(new CHECKCAST(cpg.addClass(DOM_INTF_SIG)));
            newDom.setStart(il.append(new ASTORE(newDom.getIndex())));
            // Overwrite old handler with DOM handler
            index=cpg.addInterfaceMethodref(DOM_INTF,
                    "getOutputDomBuilder",
                    "()"+TRANSLET_OUTPUT_SIG);
            il.append(new INVOKEINTERFACE(index,1));
            //index = cpg.addMethodref(DOM_IMPL,
            //                   "getOutputDomBuilder",
            //                   "()" + TRANSLET_OUTPUT_SIG);
            //il.append(new INVOKEVIRTUAL(index));
            il.append(DUP);
            il.append(DUP);
            // Store DOM handler in a local in order to call endDocument()
            domBuilder=
                    methodGen.addLocalVariable("rt_to_reference_handler",
                            Util.getJCRefType(TRANSLET_OUTPUT_SIG),
                            null,null);
            domBuilder.setStart(il.append(new ASTORE(domBuilder.getIndex())));
            // Call startDocument on the new handler
            index=cpg.addInterfaceMethodref(TRANSLET_OUTPUT_INTERFACE,
                    "startDocument","()V");
            il.append(new INVOKEINTERFACE(index,1));
            // Call the method that implements this result tree
            index=cpg.addMethodref(className,
                    _methodName,
                    "("
                            +DOM_INTF_SIG
                            +TRANSLET_OUTPUT_SIG
                            +")V");
            il.append(new INVOKEVIRTUAL(index));
            // Call endDocument on the DOM handler
            domBuilder.setEnd(il.append(new ALOAD(domBuilder.getIndex())));
            index=cpg.addInterfaceMethodref(TRANSLET_OUTPUT_INTERFACE,
                    "endDocument","()V");
            il.append(new INVOKEINTERFACE(index,1));
            // Push the new DOM on the stack
            newDom.setEnd(il.append(new ALOAD(newDom.getIndex())));
        }
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            NodeSetType type){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        // Put an extra copy of the result tree (DOM) on the stack
        il.append(DUP);
        // DOM adapters containing a result tree are not initialised with
        // translet-type to DOM-type mapping. This must be done now for
        // XPath expressions and patterns to work for the iterator we create.
        il.append(classGen.loadTranslet()); // get names array
        il.append(new GETFIELD(cpg.addFieldref(TRANSLET_CLASS,
                NAMES_INDEX,
                NAMES_INDEX_SIG)));
        il.append(classGen.loadTranslet()); // get uris array
        il.append(new GETFIELD(cpg.addFieldref(TRANSLET_CLASS,
                URIS_INDEX,
                URIS_INDEX_SIG)));
        il.append(classGen.loadTranslet()); // get types array
        il.append(new GETFIELD(cpg.addFieldref(TRANSLET_CLASS,
                TYPES_INDEX,
                TYPES_INDEX_SIG)));
        il.append(classGen.loadTranslet()); // get namespaces array
        il.append(new GETFIELD(cpg.addFieldref(TRANSLET_CLASS,
                NAMESPACE_INDEX,
                NAMESPACE_INDEX_SIG)));
        // Pass the type mappings to the DOM adapter
        final int mapping=cpg.addInterfaceMethodref(DOM_INTF,
                "setupMapping",
                "(["+STRING_SIG+
                        "["+STRING_SIG+
                        "[I"+
                        "["+STRING_SIG+")V");
        il.append(new INVOKEINTERFACE(mapping,5));
        il.append(DUP);
        // Create an iterator for the root node of the DOM adapter
        final int iter=cpg.addInterfaceMethodref(DOM_INTF,
                "getIterator",
                "()"+NODE_ITERATOR_SIG);
        il.append(new INVOKEINTERFACE(iter,1));
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            ObjectType type){
        methodGen.getInstructionList().append(NOP);
    }
}
