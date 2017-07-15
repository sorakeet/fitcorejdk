/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: TestSeq.java,v 1.2.4.1 2005/09/12 11:31:38 pvedula Exp $
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * $Id: TestSeq.java,v 1.2.4.1 2005/09/12 11:31:38 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.GOTO_W;
import com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import com.sun.org.apache.bcel.internal.generic.InstructionList;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;

import java.util.Map;
import java.util.Vector;

final class TestSeq{
    private int _kernelType;
    private Vector _patterns=null;
    private Mode _mode=null;
    private Template _default=null;
    private InstructionList _instructionList;
    private InstructionHandle _start=null;

    public TestSeq(Vector patterns,Mode mode){
        this(patterns,-2,mode);
    }

    public TestSeq(Vector patterns,int kernelType,Mode mode){
        _patterns=patterns;
        _kernelType=kernelType;
        _mode=mode;
    }

    public String toString(){
        final int count=_patterns.size();
        final StringBuffer result=new StringBuffer();
        for(int i=0;i<count;i++){
            final LocationPathPattern pattern=
                    (LocationPathPattern)_patterns.elementAt(i);
            if(i==0){
                result.append("Testseq for kernel ").append(_kernelType)
                        .append('\n');
            }
            result.append("   pattern ").append(i).append(": ")
                    .append(pattern.toString())
                    .append('\n');
        }
        return result.toString();
    }

    public InstructionList getInstructionList(){
        return _instructionList;
    }

    public double getPriority(){
        final Template template=(_patterns.size()==0)?_default
                :((Pattern)_patterns.elementAt(0)).getTemplate();
        return template.getPriority();
    }

    public int getPosition(){
        final Template template=(_patterns.size()==0)?_default
                :((Pattern)_patterns.elementAt(0)).getTemplate();
        return template.getPosition();
    }

    public void reduce(){
        final Vector newPatterns=new Vector();
        final int count=_patterns.size();
        for(int i=0;i<count;i++){
            final LocationPathPattern pattern=
                    (LocationPathPattern)_patterns.elementAt(i);
            // Reduce this pattern
            pattern.reduceKernelPattern();
            // Is this pattern fully reduced?
            if(pattern.isWildcard()){
                _default=pattern.getTemplate();
                break;          // Ignore following patterns
            }else{
                newPatterns.addElement(pattern);
            }
        }
        _patterns=newPatterns;
    }

    public void findTemplates(Map<Template,Object> templates){
        if(_default!=null){
            templates.put(_default,this);
        }
        for(int i=0;i<_patterns.size();i++){
            final LocationPathPattern pattern=
                    (LocationPathPattern)_patterns.elementAt(i);
            templates.put(pattern.getTemplate(),this);
        }
    }

    public InstructionHandle compile(ClassGenerator classGen,
                                     MethodGenerator methodGen,
                                     InstructionHandle continuation){
        // Returned cached value if already compiled
        if(_start!=null){
            return _start;
        }
        // If not patterns, then return handle for default template
        final int count=_patterns.size();
        if(count==0){
            return (_start=getTemplateHandle(_default));
        }
        // Init handle to jump when all patterns failed
        InstructionHandle fail=(_default==null)?continuation
                :getTemplateHandle(_default);
        // Compile all patterns in reverse order
        for(int n=count-1;n>=0;n--){
            final LocationPathPattern pattern=getPattern(n);
            final Template template=pattern.getTemplate();
            final InstructionList il=new InstructionList();
            // Patterns expect current node on top of stack
            il.append(methodGen.loadCurrentNode());
            // Apply the test-code compiled for the pattern
            InstructionList ilist=methodGen.getInstructionList(pattern);
            if(ilist==null){
                ilist=pattern.compile(classGen,methodGen);
                methodGen.addInstructionList(pattern,ilist);
            }
            // Make a copy of the instruction list for backpatching
            InstructionList copyOfilist=ilist.copy();
            FlowList trueList=pattern.getTrueList();
            if(trueList!=null){
                trueList=trueList.copyAndRedirect(ilist,copyOfilist);
            }
            FlowList falseList=pattern.getFalseList();
            if(falseList!=null){
                falseList=falseList.copyAndRedirect(ilist,copyOfilist);
            }
            il.append(copyOfilist);
            // On success branch to the template code
            final InstructionHandle gtmpl=getTemplateHandle(template);
            final InstructionHandle success=il.append(new GOTO_W(gtmpl));
            if(trueList!=null){
                trueList.backPatch(success);
            }
            if(falseList!=null){
                falseList.backPatch(fail);
            }
            // Next pattern's 'fail' target is this pattern's first instruction
            fail=il.getStart();
            // Append existing instruction list to the end of this one
            if(_instructionList!=null){
                il.append(_instructionList);
            }
            // Set current instruction list to be this one
            _instructionList=il;
        }
        return (_start=fail);
    }

    private InstructionHandle getTemplateHandle(Template template){
        return (InstructionHandle)_mode.getTemplateInstructionHandle(template);
    }

    private LocationPathPattern getPattern(int n){
        return (LocationPathPattern)_patterns.elementAt(n);
    }
}
