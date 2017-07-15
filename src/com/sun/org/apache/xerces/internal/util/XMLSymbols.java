/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002,2004 The Apache Software Foundation.
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
 * Copyright 2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.util;

public class XMLSymbols{
    //==========================
    // Commonly used strings
    //==========================
    public final static String EMPTY_STRING="".intern();
    //==========================
    // Namespace prefixes/uris
    //==========================
    public final static String PREFIX_XML="xml".intern();
    public final static String PREFIX_XMLNS="xmlns".intern();
    //==========================
    // DTD symbols
    //==========================
    public static final String fANYSymbol="ANY".intern();
    public static final String fCDATASymbol="CDATA".intern();
    public static final String fIDSymbol="ID".intern();
    public static final String fIDREFSymbol="IDREF".intern();
    public static final String fIDREFSSymbol="IDREFS".intern();
    public static final String fENTITYSymbol="ENTITY".intern();
    public static final String fENTITIESSymbol="ENTITIES".intern();
    public static final String fNMTOKENSymbol="NMTOKEN".intern();
    public static final String fNMTOKENSSymbol="NMTOKENS".intern();
    public static final String fNOTATIONSymbol="NOTATION".intern();
    public static final String fENUMERATIONSymbol="ENUMERATION".intern();
    public static final String fIMPLIEDSymbol="#IMPLIED".intern();
    public static final String fREQUIREDSymbol="#REQUIRED".intern();
    public static final String fFIXEDSymbol="#FIXED".intern();
    // public constructor.
    public XMLSymbols(){
    }
}
