/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002,2004,2005 The Apache Software Foundation.
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
 * Copyright 2002,2004,2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.xni.grammars;

import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;

public interface XMLSchemaDescription extends XMLGrammarDescription{
    // used to indicate what triggered the call
    public final static short CONTEXT_INCLUDE=0;
    public final static short CONTEXT_REDEFINE=1;
    public final static short CONTEXT_IMPORT=2;
    public final static short CONTEXT_PREPARSE=3;
    public final static short CONTEXT_INSTANCE=4;
    public final static short CONTEXT_ELEMENT=5;
    public final static short CONTEXT_ATTRIBUTE=6;
    public final static short CONTEXT_XSITYPE=7;

    public short getContextType();

    public String getTargetNamespace();

    public String[] getLocationHints();

    public QName getTriggeringComponent();

    public QName getEnclosingElementName();

    public XMLAttributes getAttributes();
} // XSDDescription
