/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.validation;

import java.util.Vector;

public class ValidationManager{
    protected final Vector fVSs=new Vector();
    protected boolean fGrammarFound=false;
    // used by the DTD validator to tell other components that it has a
    // cached DTD in hand so there's no reason to
    // scan external subset or entity decls.
    protected boolean fCachedDTD=false;

    public final void addValidationState(ValidationState vs){
        fVSs.addElement(vs);
    }

    public final void setEntityState(EntityState state){
        for(int i=fVSs.size()-1;i>=0;i--){
            ((ValidationState)fVSs.elementAt(i)).setEntityState(state);
        }
    }

    public final boolean isGrammarFound(){
        return fGrammarFound;
    }

    public final void setGrammarFound(boolean grammar){
        fGrammarFound=grammar;
    }

    public final boolean isCachedDTD(){
        return fCachedDTD;
    } // isCachedDTD():  boolean

    public final void setCachedDTD(boolean cachedDTD){
        fCachedDTD=cachedDTD;
    } // setCachedDTD(boolean)

    public final void reset(){
        fVSs.removeAllElements();
        fGrammarFound=false;
        fCachedDTD=false;
    }
}
