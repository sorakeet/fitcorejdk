/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.xni.parser;

import com.sun.org.apache.xerces.internal.util.Status;
import com.sun.org.apache.xerces.internal.xni.XNIException;

public class XMLConfigurationException
        extends XNIException{
    static final long serialVersionUID=-5437427404547669188L;
    //
    // Data
    //
    protected Status fType;
    protected String fIdentifier;
    //
    // Constructors
    //

    public XMLConfigurationException(Status type,String identifier){
        super(identifier);
        fType=type;
        fIdentifier=identifier;
    } // <init>(short,String)

    public XMLConfigurationException(Status type,String identifier,
                                     String message){
        super(message);
        fType=type;
        fIdentifier=identifier;
    } // <init>(short,String,String)
    //
    // Public methods
    //

    public Status getType(){
        return fType;
    } // getType():short

    public String getIdentifier(){
        return fIdentifier;
    } // getIdentifier():String
} // class XMLConfigurationException
