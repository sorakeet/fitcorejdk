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
 * $Id: VariableRefBase.java,v 1.5 2005/09/28 13:48:18 pvedula Exp $
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
 * $Id: VariableRefBase.java,v 1.5 2005/09/28 13:48:18 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

import java.util.Objects;

class VariableRefBase extends Expression{
    protected VariableBase _variable;
    protected Closure _closure=null;

    public VariableRefBase(VariableBase variable){
        _variable=variable;
        variable.addReference(this);
    }

    public VariableRefBase(){
        _variable=null;
    }

    public VariableBase getVariable(){
        return _variable;
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(this._variable);
    }

    @Override
    public boolean equals(Object obj){
        return obj==this||(obj instanceof VariableRefBase)
                &&(_variable==((VariableRefBase)obj)._variable);
    }

    @Override
    public String toString(){
        return "variable-ref("+_variable.getName()+'/'+_variable.getType()+')';
    }

    @Override
    public Type typeCheck(SymbolTable stable)
            throws TypeCheckError{
        // Returned cached type if available
        if(_type!=null) return _type;
        // Find nearest closure to add a variable reference
        if(_variable.isLocal()){
            SyntaxTreeNode node=getParent();
            do{
                if(node instanceof Closure){
                    _closure=(Closure)node;
                    break;
                }
                if(node instanceof TopLevelElement){
                    break;      // way up in the tree
                }
                node=node.getParent();
            }while(node!=null);
            if(_closure!=null){
                _closure.addVariable(this);
            }
        }
        // Attempt to get the cached variable type
        _type=_variable.getType();
        // If that does not work we must force a type-check (this is normally
        // only needed for globals in included/imported stylesheets
        if(_type==null){
            _variable.typeCheck(stable);
            _type=_variable.getType();
        }
        // If in a top-level element, create dependency to the referenced var
        addParentDependency();
        // Return the type of the referenced variable
        return _type;
    }

    public void addParentDependency(){
        SyntaxTreeNode node=this;
        while(node!=null&&node instanceof TopLevelElement==false){
            node=node.getParent();
        }
        TopLevelElement parent=(TopLevelElement)node;
        if(parent!=null){
            VariableBase var=_variable;
            if(_variable._ignore){
                if(_variable instanceof Variable){
                    var=parent.getSymbolTable()
                            .lookupVariable(_variable._name);
                }else if(_variable instanceof Param){
                    var=parent.getSymbolTable().lookupParam(_variable._name);
                }
            }
            parent.addDependency(var);
        }
    }
}
