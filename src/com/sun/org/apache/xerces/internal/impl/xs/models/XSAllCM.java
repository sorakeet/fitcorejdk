/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.impl.xs.models;

import com.sun.org.apache.xerces.internal.impl.xs.SubstitutionGroupHandler;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaException;
import com.sun.org.apache.xerces.internal.impl.xs.XSConstraints;
import com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import com.sun.org.apache.xerces.internal.xni.QName;

import java.util.ArrayList;
import java.util.Vector;

public class XSAllCM implements XSCMValidator{
    //
    // Constants
    //
    // start the content model: did not see any children
    private static final short STATE_START=0;
    private static final short STATE_VALID=1;
    private static final short STATE_CHILD=1;
    //
    // Data
    //
    private XSElementDecl fAllElements[];
    private boolean fIsOptionalElement[];
    private boolean fHasOptionalContent=false;
    private int fNumElements=0;
    //
    // Constructors
    //

    public XSAllCM(boolean hasOptionalContent,int size){
        fHasOptionalContent=hasOptionalContent;
        fAllElements=new XSElementDecl[size];
        fIsOptionalElement=new boolean[size];
    }

    public void addElement(XSElementDecl element,boolean isOptional){
        fAllElements[fNumElements]=element;
        fIsOptionalElement[fNumElements]=isOptional;
        fNumElements++;
    }
    //
    // XSCMValidator methods
    //

    public int[] startContentModel(){
        int[] state=new int[fNumElements+1];
        for(int i=0;i<=fNumElements;i++){
            state[i]=STATE_START;
        }
        return state;
    }

    public Object oneTransition(QName elementName,int[] currentState,SubstitutionGroupHandler subGroupHandler){
        // error state
        if(currentState[0]<0){
            currentState[0]=XSCMValidator.SUBSEQUENT_ERROR;
            return findMatchingDecl(elementName,subGroupHandler);
        }
        // seen child
        currentState[0]=STATE_CHILD;
        Object matchingDecl=null;
        for(int i=0;i<fNumElements;i++){
            // we only try to look for a matching decl if we have not seen
            // this element yet.
            if(currentState[i+1]!=STATE_START)
                continue;
            matchingDecl=subGroupHandler.getMatchingElemDecl(elementName,fAllElements[i]);
            if(matchingDecl!=null){
                // found the decl, mark this element as "seen".
                currentState[i+1]=STATE_VALID;
                return matchingDecl;
            }
        }
        // couldn't find the decl, change to error state.
        currentState[0]=XSCMValidator.FIRST_ERROR;
        return findMatchingDecl(elementName,subGroupHandler);
    }

    // convinient method: when error occurs, to find a matching decl
    // from the candidate elements.
    Object findMatchingDecl(QName elementName,SubstitutionGroupHandler subGroupHandler){
        Object matchingDecl=null;
        for(int i=0;i<fNumElements;i++){
            matchingDecl=subGroupHandler.getMatchingElemDecl(elementName,fAllElements[i]);
            if(matchingDecl!=null)
                break;
        }
        return matchingDecl;
    }

    public boolean endContentModel(int[] currentState){
        int state=currentState[0];
        if(state==XSCMValidator.FIRST_ERROR||state==XSCMValidator.SUBSEQUENT_ERROR){
            return false;
        }
        // If <all> has minOccurs of zero and there are
        // no children to validate, it is trivially valid
        if(fHasOptionalContent&&state==STATE_START){
            return true;
        }
        for(int i=0;i<fNumElements;i++){
            // if one element is required, but not present, then error
            if(!fIsOptionalElement[i]&&currentState[i+1]==STATE_START)
                return false;
        }
        return true;
    }

    public boolean checkUniqueParticleAttribution(SubstitutionGroupHandler subGroupHandler) throws XMLSchemaException{
        // check whether there is conflict between any two leaves
        for(int i=0;i<fNumElements;i++){
            for(int j=i+1;j<fNumElements;j++){
                if(XSConstraints.overlapUPA(fAllElements[i],fAllElements[j],subGroupHandler)){
                    // REVISIT: do we want to report all errors? or just one?
                    throw new XMLSchemaException("cos-nonambig",new Object[]{fAllElements[i].toString(),
                            fAllElements[j].toString()});
                }
            }
        }
        return false;
    }

    public Vector whatCanGoHere(int[] state){
        Vector ret=new Vector();
        for(int i=0;i<fNumElements;i++){
            // we only try to look for a matching decl if we have not seen
            // this element yet.
            if(state[i+1]==STATE_START)
                ret.addElement(fAllElements[i]);
        }
        return ret;
    }

    public ArrayList checkMinMaxBounds(){
        return null;
    }
} // class XSAllCM
