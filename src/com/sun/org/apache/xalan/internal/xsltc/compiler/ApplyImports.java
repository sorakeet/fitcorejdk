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
 * $Id: ApplyImports.java,v 1.2.4.1 2005/09/13 12:22:02 pvedula Exp $
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
 * $Id: ApplyImports.java,v 1.2.4.1 2005/09/13 12:22:02 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import com.sun.org.apache.bcel.internal.generic.InstructionList;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.*;

final class ApplyImports extends Instruction{
    private QName _modeName;
    private int _precedence;

    public boolean hasWithParams(){
        return hasContents();
    }

    public void parseContents(Parser parser){
        // Indicate to the top-level stylesheet that all templates must be
        // compiled into separate methods.
        Stylesheet stylesheet=getStylesheet();
        stylesheet.setTemplateInlining(false);
        // Get the mode we are currently in (might not be any)
        Template template=getTemplate();
        _modeName=template.getModeName();
        _precedence=template.getImportPrecedence();
        // Get the method name for <xsl:apply-imports/> in this mode
        stylesheet=parser.getTopLevelStylesheet();
        parseChildren(parser);  // with-params
    }

    public void display(int indent){
        indent(indent);
        Util.println("ApplyTemplates");
        indent(indent+IndentIncrement);
        if(_modeName!=null){
            indent(indent+IndentIncrement);
            Util.println("mode "+_modeName);
        }
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError{
        typeCheckContents(stable);              // with-params
        return Type.Void;
    }

    public void translate(ClassGenerator classGen,MethodGenerator methodGen){
        final Stylesheet stylesheet=classGen.getStylesheet();
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        final int current=methodGen.getLocalIndex("current");
        // Push the arguments that are passed to applyTemplates()
        il.append(classGen.loadTranslet());
        il.append(methodGen.loadDOM());
        il.append(methodGen.loadIterator());
        il.append(methodGen.loadHandler());
        il.append(methodGen.loadCurrentNode());
        // Push a new parameter frame in case imported template might expect
        // parameters.  The apply-imports has nothing that it can pass.
        if(stylesheet.hasLocalParams()){
            il.append(classGen.loadTranslet());
            final int pushFrame=cpg.addMethodref(TRANSLET_CLASS,
                    PUSH_PARAM_FRAME,
                    PUSH_PARAM_FRAME_SIG);
            il.append(new INVOKEVIRTUAL(pushFrame));
        }
        // Get the [min,max> precedence of all templates imported under the
        // current stylesheet
        final int maxPrecedence=_precedence;
        final int minPrecedence=getMinPrecedence(maxPrecedence);
        final Mode mode=stylesheet.getMode(_modeName);
        // Get name of appropriate apply-templates function for this
        // xsl:apply-imports instruction
        String functionName=mode.functionName(minPrecedence,maxPrecedence);
        // Construct the translet class-name and the signature of the method
        final String className=classGen.getStylesheet().getClassName();
        final String signature=classGen.getApplyTemplatesSigForImport();
        final int applyTemplates=cpg.addMethodref(className,
                functionName,
                signature);
        il.append(new INVOKEVIRTUAL(applyTemplates));
        // Pop any parameter frame that was pushed above.
        if(stylesheet.hasLocalParams()){
            il.append(classGen.loadTranslet());
            final int pushFrame=cpg.addMethodref(TRANSLET_CLASS,
                    POP_PARAM_FRAME,
                    POP_PARAM_FRAME_SIG);
            il.append(new INVOKEVIRTUAL(pushFrame));
        }
    }

    private int getMinPrecedence(int max){
        // Move to root of include tree
        Stylesheet includeRoot=getStylesheet();
        while(includeRoot._includedFrom!=null){
            includeRoot=includeRoot._includedFrom;
        }
        return includeRoot.getMinimumDescendantPrecedence();
    }
}
