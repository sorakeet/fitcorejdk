/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.impl.dtd;

import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import com.sun.org.apache.xerces.internal.xni.XNIException;

final class BalancedDTDGrammar extends DTDGrammar{
    //
    // Data
    //
    private boolean fMixed;
    private int fDepth=0;
    private short[] fOpStack=null;
    private int[][] fGroupIndexStack;
    private int[] fGroupIndexStackSizes;
    //
    // Constructors
    //

    public BalancedDTDGrammar(SymbolTable symbolTable,XMLDTDDescription desc){
        super(symbolTable,desc);
    } // BalancedDTDGrammar(SymbolTable,XMLDTDDescription)
    //
    // Public methods
    //

    public final void endDTD(Augmentations augs) throws XNIException{
        super.endDTD(augs);
        fOpStack=null;
        fGroupIndexStack=null;
        fGroupIndexStackSizes=null;
    } // endDTD()

    public final void startContentModel(String elementName,Augmentations augs)
            throws XNIException{
        fDepth=0;
        initializeContentModelStacks();
        super.startContentModel(elementName,augs);
    } // startContentModel(String)

    public final void startGroup(Augmentations augs) throws XNIException{
        ++fDepth;
        initializeContentModelStacks();
        fMixed=false;
    } // startGroup()

    public final void pcdata(Augmentations augs) throws XNIException{
        fMixed=true;
    } // pcdata()

    public final void element(String elementName,Augmentations augs) throws XNIException{
        addToCurrentGroup(addUniqueLeafNode(elementName));
    } // element(String)

    public final void separator(short separator,Augmentations augs) throws XNIException{
        if(separator==XMLDTDContentModelHandler.SEPARATOR_CHOICE){
            fOpStack[fDepth]=XMLContentSpec.CONTENTSPECNODE_CHOICE;
        }else if(separator==XMLDTDContentModelHandler.SEPARATOR_SEQUENCE){
            fOpStack[fDepth]=XMLContentSpec.CONTENTSPECNODE_SEQ;
        }
    } // separator(short)

    public final void occurrence(short occurrence,Augmentations augs) throws XNIException{
        if(!fMixed){
            int currentIndex=fGroupIndexStackSizes[fDepth]-1;
            if(occurrence==XMLDTDContentModelHandler.OCCURS_ZERO_OR_ONE){
                fGroupIndexStack[fDepth][currentIndex]=addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE,fGroupIndexStack[fDepth][currentIndex],-1);
            }else if(occurrence==XMLDTDContentModelHandler.OCCURS_ZERO_OR_MORE){
                fGroupIndexStack[fDepth][currentIndex]=addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE,fGroupIndexStack[fDepth][currentIndex],-1);
            }else if(occurrence==XMLDTDContentModelHandler.OCCURS_ONE_OR_MORE){
                fGroupIndexStack[fDepth][currentIndex]=addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE,fGroupIndexStack[fDepth][currentIndex],-1);
            }
        }
    } // occurrence(short)

    public final void endGroup(Augmentations augs) throws XNIException{
        final int length=fGroupIndexStackSizes[fDepth];
        final int group=length>0?addContentSpecNodes(0,length-1):addUniqueLeafNode(null);
        --fDepth;
        addToCurrentGroup(group);
    } // endGroup()
    //
    // Protected methods
    //

    protected final void addContentSpecToElement(XMLElementDecl elementDecl){
        int contentSpec=fGroupIndexStackSizes[0]>0?fGroupIndexStack[0][0]:-1;
        setContentSpecIndex(fCurrentElementIndex,contentSpec);
    }
    //
    // Private methods
    //

    private void addToCurrentGroup(int contentSpec){
        int[] currentGroup=fGroupIndexStack[fDepth];
        int length=fGroupIndexStackSizes[fDepth]++;
        if(currentGroup==null){
            currentGroup=new int[8];
            fGroupIndexStack[fDepth]=currentGroup;
        }else if(length==currentGroup.length){
            int[] newGroup=new int[currentGroup.length*2];
            System.arraycopy(currentGroup,0,newGroup,0,currentGroup.length);
            currentGroup=newGroup;
            fGroupIndexStack[fDepth]=currentGroup;
        }
        currentGroup[length]=contentSpec;
    } // addToCurrentGroup(int)

    private void initializeContentModelStacks(){
        if(fOpStack==null){
            fOpStack=new short[8];
            fGroupIndexStack=new int[8][];
            fGroupIndexStackSizes=new int[8];
        }else if(fDepth==fOpStack.length){
            short[] newOpStack=new short[fDepth*2];
            System.arraycopy(fOpStack,0,newOpStack,0,fDepth);
            fOpStack=newOpStack;
            int[][] newGroupIndexStack=new int[fDepth*2][];
            System.arraycopy(fGroupIndexStack,0,newGroupIndexStack,0,fDepth);
            fGroupIndexStack=newGroupIndexStack;
            int[] newGroupIndexStackLengths=new int[fDepth*2];
            System.arraycopy(fGroupIndexStackSizes,0,newGroupIndexStackLengths,0,fDepth);
            fGroupIndexStackSizes=newGroupIndexStackLengths;
        }
        fOpStack[fDepth]=-1;
        fGroupIndexStackSizes[fDepth]=0;
    } // initializeContentModelStacks()

    private int addContentSpecNodes(int begin,int end){
        if(begin==end){
            return fGroupIndexStack[fDepth][begin];
        }
        final int middle=(begin+end)>>>1;
        return addContentSpecNode(fOpStack[fDepth],
                addContentSpecNodes(begin,middle),
                addContentSpecNodes(middle+1,end));
    } // addContentSpecNodes(int,int)
} // class BalancedDTDGrammar
