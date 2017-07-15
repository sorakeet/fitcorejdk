/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.xni;

import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelSource;

public interface XMLDTDContentModelHandler{
    //
    // Constants
    //
    // separators
    public static final short SEPARATOR_CHOICE=0;
    public static final short SEPARATOR_SEQUENCE=1;
    // occurrence counts
    public static final short OCCURS_ZERO_OR_ONE=2;
    public static final short OCCURS_ZERO_OR_MORE=3;
    public static final short OCCURS_ONE_OR_MORE=4;
    //
    // XMLDTDContentModelHandler methods
    //

    public void startContentModel(String elementName,Augmentations augmentations)
            throws XNIException;

    public void any(Augmentations augmentations) throws XNIException;

    public void empty(Augmentations augmentations) throws XNIException;

    public void startGroup(Augmentations augmentations) throws XNIException;

    public void pcdata(Augmentations augmentations) throws XNIException;

    public void element(String elementName,Augmentations augmentations)
            throws XNIException;

    public void separator(short separator,Augmentations augmentations)
            throws XNIException;

    public void occurrence(short occurrence,Augmentations augmentations)
            throws XNIException;

    public void endGroup(Augmentations augmentations) throws XNIException;

    public void endContentModel(Augmentations augmentations) throws XNIException;

    // get content model source
    public XMLDTDContentModelSource getDTDContentModelSource();

    // set content model source
    public void setDTDContentModelSource(XMLDTDContentModelSource source);
} // interface XMLDTDContentModelHandler
