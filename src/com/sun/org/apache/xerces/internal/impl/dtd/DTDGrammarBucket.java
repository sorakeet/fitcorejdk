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

import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;

import java.util.HashMap;
import java.util.Map;

public class DTDGrammarBucket{
    // REVISIT:  make this class smarter and *way* more complete!
    //
    // Data
    //
    protected Map<XMLDTDDescription,DTDGrammar> fGrammars;
    // the unique grammar from fGrammars (or that we're
    // building) that is used in validation.
    protected DTDGrammar fActiveGrammar;
    // is the "active" grammar standalone?
    protected boolean fIsStandalone;
    //
    // Constructors
    //

    public DTDGrammarBucket(){
        fGrammars=new HashMap<>();
    } // <init>()
    //
    // Public methods
    //

    public void putGrammar(DTDGrammar grammar){
        XMLDTDDescription desc=(XMLDTDDescription)grammar.getGrammarDescription();
        fGrammars.put(desc,grammar);
    } // putGrammar(DTDGrammar)

    // retrieve a DTDGrammar given an XMLDTDDescription
    public DTDGrammar getGrammar(XMLGrammarDescription desc){
        return fGrammars.get((XMLDTDDescription)desc);
    } // putGrammar(DTDGrammar)

    public void clear(){
        fGrammars.clear();
        fActiveGrammar=null;
        fIsStandalone=false;
    } // clear()

    boolean getStandalone(){
        return fIsStandalone;
    }

    // is the active grammar standalone?  This must live here because
    // at the time the validator discovers this we don't yet know
    // what the active grammar should be (no info about root)
    void setStandalone(boolean standalone){
        fIsStandalone=standalone;
    }

    DTDGrammar getActiveGrammar(){
        return fActiveGrammar;
    }

    // set the "active" grammar:
    void setActiveGrammar(DTDGrammar grammar){
        fActiveGrammar=grammar;
    }
} // class DTDGrammarBucket
