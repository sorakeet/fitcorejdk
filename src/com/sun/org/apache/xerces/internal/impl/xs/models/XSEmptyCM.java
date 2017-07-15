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
 */
/**
 * Copyright 2001-2004 The Apache Software Foundation.
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
import com.sun.org.apache.xerces.internal.xni.QName;

import java.util.ArrayList;
import java.util.Vector;

public class XSEmptyCM implements XSCMValidator{
    //
    // Constants
    //
    // start the content model: did not see any children
    private static final short STATE_START=0;
    private static final Vector EMPTY=new Vector(0);
    //
    // Data
    //
    //
    // XSCMValidator methods
    //

    public int[] startContentModel(){
        return (new int[]{STATE_START});
    }

    public Object oneTransition(QName elementName,int[] currentState,SubstitutionGroupHandler subGroupHandler){
        // error state
        if(currentState[0]<0){
            currentState[0]=XSCMValidator.SUBSEQUENT_ERROR;
            return null;
        }
        currentState[0]=XSCMValidator.FIRST_ERROR;
        return null;
    }

    public boolean endContentModel(int[] currentState){
        boolean isFinal=false;
        int state=currentState[0];
        // restore content model state:
        // error
        if(state<0){
            return false;
        }
        return true;
    }

    public boolean checkUniqueParticleAttribution(SubstitutionGroupHandler subGroupHandler) throws XMLSchemaException{
        return false;
    }

    public Vector whatCanGoHere(int[] state){
        return EMPTY;
    }

    public ArrayList checkMinMaxBounds(){
        return null;
    }
} // class XSEmptyCM
