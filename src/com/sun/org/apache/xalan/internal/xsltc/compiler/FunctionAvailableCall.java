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
 * $Id: FunctionAvailableCall.java,v 1.2.4.1 2005/09/01 15:30:25 pvedula Exp $
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
 * $Id: FunctionAvailableCall.java,v 1.2.4.1 2005/09/01 15:30:25 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.org.apache.bcel.internal.generic.PUSH;
import com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;

final class FunctionAvailableCall extends FunctionCall{
    private Expression _arg;
    private String _nameOfFunct=null;
    private String _namespaceOfFunct=null;
    private boolean _isFunctionAvailable=false;

    public FunctionAvailableCall(QName fname,Vector arguments){
        super(fname,arguments);
        _arg=(Expression)arguments.elementAt(0);
        _type=null;
        if(_arg instanceof LiteralExpr){
            LiteralExpr arg=(LiteralExpr)_arg;
            _namespaceOfFunct=arg.getNamespace();
            _nameOfFunct=arg.getValue();
            if(!isInternalNamespace()){
                _isFunctionAvailable=hasMethods();
            }
        }
    }

    private boolean hasMethods(){
        LiteralExpr arg=(LiteralExpr)_arg;
        // Get the class name from the namespace uri
        String className=getClassNameFromUri(_namespaceOfFunct);
        // Get the method name from the argument to function-available
        String methodName=null;
        int colonIndex=_nameOfFunct.indexOf(":");
        if(colonIndex>0){
            String functionName=_nameOfFunct.substring(colonIndex+1);
            int lastDotIndex=functionName.lastIndexOf('.');
            if(lastDotIndex>0){
                methodName=functionName.substring(lastDotIndex+1);
                if(className!=null&&!className.equals(""))
                    className=className+"."+functionName.substring(0,lastDotIndex);
                else
                    className=functionName.substring(0,lastDotIndex);
            }else
                methodName=functionName;
        }else
            methodName=_nameOfFunct;
        if(className==null||methodName==null){
            return false;
        }
        // Replace the '-' characters in the method name
        if(methodName.indexOf('-')>0)
            methodName=replaceDash(methodName);
        try{
            final Class clazz=ObjectFactory.findProviderClass(className,true);
            if(clazz==null){
                return false;
            }
            final Method[] methods=clazz.getMethods();
            for(int i=0;i<methods.length;i++){
                final int mods=methods[i].getModifiers();
                if(Modifier.isPublic(mods)&&Modifier.isStatic(mods)
                        &&methods[i].getName().equals(methodName)){
                    return true;
                }
            }
        }catch(ClassNotFoundException e){
            return false;
        }
        return false;
    }

    private boolean isInternalNamespace(){
        return (_namespaceOfFunct==null||
                _namespaceOfFunct.equals(EMPTYSTRING)||
                _namespaceOfFunct.equals(TRANSLET_URI));
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError{
        if(_type!=null){
            return _type;
        }
        if(_arg instanceof LiteralExpr){
            return _type=Type.Boolean;
        }
        ErrorMsg err=new ErrorMsg(ErrorMsg.NEED_LITERAL_ERR,
                "function-available",this);
        throw new TypeCheckError(err);
    }

    public void translate(ClassGenerator classGen,MethodGenerator methodGen){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        methodGen.getInstructionList().append(new PUSH(cpg,getResult()));
    }

    public Object evaluateAtCompileTime(){
        return getResult()?Boolean.TRUE:Boolean.FALSE;
    }

    public boolean getResult(){
        if(_nameOfFunct==null){
            return false;
        }
        if(isInternalNamespace()){
            final Parser parser=getParser();
            _isFunctionAvailable=
                    parser.functionSupported(Util.getLocalName(_nameOfFunct));
        }
        return _isFunctionAvailable;
    }
}
