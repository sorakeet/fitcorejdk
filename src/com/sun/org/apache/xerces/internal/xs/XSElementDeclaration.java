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

public interface XSElementDeclaration extends XSTerm{
    public XSTypeDefinition getTypeDefinition();

    public short getScope();

    public XSComplexTypeDefinition getEnclosingCTDefinition();

    public short getConstraintType();

    public String getConstraintValue();

    public Object getActualVC()
            throws XSException;

    public short getActualVCType()
            throws XSException;

    public ShortList getItemValueTypes()
            throws XSException;

    public boolean getNillable();

    public XSNamedMap getIdentityConstraints();

    public XSElementDeclaration getSubstitutionGroupAffiliation();

    public boolean isSubstitutionGroupExclusion(short exclusion);

    public short getSubstitutionGroupExclusions();

    public boolean isDisallowedSubstitution(short disallowed);

    public short getDisallowedSubstitutions();

    public boolean getAbstract();

    public XSAnnotation getAnnotation();

    public XSObjectList getAnnotations();
}
