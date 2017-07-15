/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2003,2004 The Apache Software Foundation.
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
 * Copyright 2003,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.xs;

public interface XSComplexTypeDefinition extends XSTypeDefinition{
    // Content Model Types
    public static final short CONTENTTYPE_EMPTY=0;
    public static final short CONTENTTYPE_SIMPLE=1;
    public static final short CONTENTTYPE_ELEMENT=2;
    public static final short CONTENTTYPE_MIXED=3;

    public short getDerivationMethod();

    public boolean getAbstract();

    public XSObjectList getAttributeUses();

    public XSWildcard getAttributeWildcard();

    public short getContentType();

    public XSSimpleTypeDefinition getSimpleType();

    public XSParticle getParticle();

    public boolean isProhibitedSubstitution(short restriction);

    public short getProhibitedSubstitutions();

    public XSObjectList getAnnotations();
}
