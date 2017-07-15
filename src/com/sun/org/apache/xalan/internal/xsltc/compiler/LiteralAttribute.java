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
 * $Id: LiteralAttribute.java,v 1.2.4.1 2005/09/12 10:38:03 pvedula Exp $
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
 * $Id: LiteralAttribute.java,v 1.2.4.1 2005/09/12 10:38:03 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.org.apache.bcel.internal.generic.InstructionList;
import com.sun.org.apache.bcel.internal.generic.PUSH;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.*;
import com.sun.org.apache.xml.internal.serializer.ElemDesc;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;

final class LiteralAttribute extends Instruction{
    private final String _name;         // Attribute name (incl. prefix)
    private final AttributeValue _value; // Attribute value

    public LiteralAttribute(String name,String value,Parser parser,
                            SyntaxTreeNode parent){
        _name=name;
        setParent(parent);
        _value=AttributeValue.create(this,value,parser);
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError{
        _value.typeCheck(stable);
        typeCheckContents(stable);
        return Type.Void;
    }

    public void translate(ClassGenerator classGen,MethodGenerator methodGen){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        // push handler
        il.append(methodGen.loadHandler());
        // push attribute name - namespace prefix set by parent node
        il.append(new PUSH(cpg,_name));
        // push attribute value
        _value.translate(classGen,methodGen);
        // Generate code that calls SerializationHandler.addUniqueAttribute()
        // if all attributes are unique.
        SyntaxTreeNode parent=getParent();
        if(parent instanceof LiteralElement
                &&((LiteralElement)parent).allAttributesUnique()){
            int flags=0;
            boolean isHTMLAttrEmpty=false;
            ElemDesc elemDesc=((LiteralElement)parent).getElemDesc();
            // Set the HTML flags
            if(elemDesc!=null){
                if(elemDesc.isAttrFlagSet(_name,ElemDesc.ATTREMPTY)){
                    flags=flags|SerializationHandler.HTML_ATTREMPTY;
                    isHTMLAttrEmpty=true;
                }else if(elemDesc.isAttrFlagSet(_name,ElemDesc.ATTRURL)){
                    flags=flags|SerializationHandler.HTML_ATTRURL;
                }
            }
            if(_value instanceof SimpleAttributeValue){
                String attrValue=((SimpleAttributeValue)_value).toString();
                if(!hasBadChars(attrValue)&&!isHTMLAttrEmpty){
                    flags=flags|SerializationHandler.NO_BAD_CHARS;
                }
            }
            il.append(new PUSH(cpg,flags));
            il.append(methodGen.uniqueAttribute());
        }else{
            // call attribute
            il.append(methodGen.attribute());
        }
    }

    private boolean hasBadChars(String value){
        char[] chars=value.toCharArray();
        int size=chars.length;
        for(int i=0;i<size;i++){
            char ch=chars[i];
            if(ch<32||126<ch||ch=='<'||ch=='>'||ch=='&'||ch=='\"')
                return true;
        }
        return false;
    }

    protected boolean contextDependent(){
        return _value.contextDependent();
    }

    public void display(int indent){
        indent(indent);
        Util.println("LiteralAttribute name="+_name+" value="+_value);
    }

    public String getName(){
        return _name;
    }

    public AttributeValue getValue(){
        return _value;
    }
}
