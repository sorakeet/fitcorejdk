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
 * $Id: Type.java,v 1.8 2007/03/28 16:51:19 joehw Exp $
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
 * $Id: Type.java,v 1.8 2007/03/28 16:51:19 joehw Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import com.sun.org.apache.bcel.internal.generic.Instruction;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import com.sun.org.apache.xalan.internal.xsltc.compiler.FlowList;
import com.sun.org.apache.xalan.internal.xsltc.compiler.NodeTest;

public abstract class Type implements Constants{
    public static final Type Int=new IntType();
    public static final Type Real=new RealType();
    public static final Type Boolean=new BooleanType();
    public static final Type NodeSet=new NodeSetType();
    public static final Type String=new StringType();
    public static final Type ResultTree=new ResultTreeType();
    public static final Type Reference=new ReferenceType();
    public static final Type Void=new VoidType();
    public static final Type Object=new ObjectType(java.lang.Object.class);
    public static final Type ObjectString=new ObjectType(java.lang.String.class);
    public static final Type Node=new NodeType(NodeTest.ANODE);
    public static final Type Root=new NodeType(NodeTest.ROOT);
    public static final Type Element=new NodeType(NodeTest.ELEMENT);
    public static final Type Attribute=new NodeType(NodeTest.ATTRIBUTE);
    public static final Type Text=new NodeType(NodeTest.TEXT);
    public static final Type Comment=new NodeType(NodeTest.COMMENT);
    public static final Type Processing_Instruction=new NodeType(NodeTest.PI);

    public static Type newObjectType(String javaClassName){
        if(javaClassName=="java.lang.Object"){
            return Type.Object;
        }else if(javaClassName=="java.lang.String"){
            return Type.ObjectString;
        }else{
            //
            java.security.AccessControlContext acc=java.security.AccessController.getContext();
            acc.checkPermission(new RuntimePermission("getContextClassLoader"));
            return new ObjectType(javaClassName);
        }
    }

    public static Type newObjectType(Class clazz){
        if(clazz==java.lang.Object.class){
            return Type.Object;
        }else if(clazz==java.lang.String.class){
            return Type.ObjectString;
        }else{
            return new ObjectType(clazz);
        }
    }

    public abstract boolean identicalTo(Type other);

    public boolean isNumber(){
        return false;
    }

    public boolean implementedAsMethod(){
        return false;
    }

    public boolean isSimple(){
        return false;
    }

    public abstract com.sun.org.apache.bcel.internal.generic.Type toJCType();

    public int distanceTo(Type type){
        return type==this?0:Integer.MAX_VALUE;
    }

    public abstract String toSignature();

    public FlowList translateToDesynthesized(ClassGenerator classGen,
                                             MethodGenerator methodGen,
                                             Type type){
        FlowList fl=null;
        if(type==Type.Boolean){
            fl=translateToDesynthesized(classGen,methodGen,
                    (BooleanType)type);
        }else{
            translateTo(classGen,methodGen,type);
        }
        return fl;
    }

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            Type type){
        ErrorMsg err=new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
                toString(),type.toString());
        classGen.getParser().reportError(Constants.FATAL,err);
    }

    public FlowList translateToDesynthesized(ClassGenerator classGen,
                                             MethodGenerator methodGen,
                                             BooleanType type){
        ErrorMsg err=new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
                toString(),type.toString());
        classGen.getParser().reportError(Constants.FATAL,err);
        return null;
    }

    public abstract String toString();

    public void translateTo(ClassGenerator classGen,MethodGenerator methodGen,
                            Class clazz){
        ErrorMsg err=new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
                toString(),clazz.getClass().toString());
        classGen.getParser().reportError(Constants.FATAL,err);
    }

    public void translateFrom(ClassGenerator classGen,MethodGenerator methodGen,
                              Class clazz){
        ErrorMsg err=new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
                clazz.getClass().toString(),toString());
        classGen.getParser().reportError(Constants.FATAL,err);
    }

    public void translateBox(ClassGenerator classGen,
                             MethodGenerator methodGen){
        ErrorMsg err=new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
                toString(),"["+toString()+"]");
        classGen.getParser().reportError(Constants.FATAL,err);
    }

    public void translateUnBox(ClassGenerator classGen,
                               MethodGenerator methodGen){
        ErrorMsg err=new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
                "["+toString()+"]",toString());
        classGen.getParser().reportError(Constants.FATAL,err);
    }

    public String getClassName(){
        return (EMPTYSTRING);
    }

    public Instruction ADD(){
        return null;            // should never be called
    }

    public Instruction SUB(){
        return null;            // should never be called
    }

    public Instruction MUL(){
        return null;            // should never be called
    }

    public Instruction DIV(){
        return null;            // should never be called
    }

    public Instruction REM(){
        return null;            // should never be called
    }

    public Instruction NEG(){
        return null;            // should never be called
    }

    public Instruction LOAD(int slot){
        return null;            // should never be called
    }

    public Instruction STORE(int slot){
        return null;            // should never be called
    }

    public Instruction POP(){
        return POP;
    }

    public BranchInstruction GT(boolean tozero){
        return null;            // should never be called
    }

    public BranchInstruction GE(boolean tozero){
        return null;            // should never be called
    }

    public BranchInstruction LT(boolean tozero){
        return null;            // should never be called
    }

    public BranchInstruction LE(boolean tozero){
        return null;            // should never be called
    }

    public Instruction CMP(boolean less){
        return null;            // should never be called
    }

    public Instruction DUP(){
        return DUP;     // default
    }
}
