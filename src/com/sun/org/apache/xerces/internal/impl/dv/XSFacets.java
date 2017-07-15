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
package com.sun.org.apache.xerces.internal.impl.dv;

import com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import com.sun.org.apache.xerces.internal.xs.XSObjectList;

import java.util.Vector;

public class XSFacets{
    public int length;
    public int minLength;
    public int maxLength;
    public short whiteSpace;
    public int totalDigits;
    public int fractionDigits;
    public String pattern;
    public Vector enumeration;
    public Vector enumNSDecls;
    public String maxInclusive;
    public String maxExclusive;
    public String minInclusive;
    public String minExclusive;
    public XSAnnotation lengthAnnotation;
    public XSAnnotation minLengthAnnotation;
    public XSAnnotation maxLengthAnnotation;
    public XSAnnotation whiteSpaceAnnotation;
    public XSAnnotation totalDigitsAnnotation;
    public XSAnnotation fractionDigitsAnnotation;
    public XSObjectListImpl patternAnnotations;
    public XSObjectList enumAnnotations;
    public XSAnnotation maxInclusiveAnnotation;
    public XSAnnotation maxExclusiveAnnotation;
    public XSAnnotation minInclusiveAnnotation;
    public XSAnnotation minExclusiveAnnotation;

    public void reset(){
        lengthAnnotation=null;
        minLengthAnnotation=null;
        maxLengthAnnotation=null;
        whiteSpaceAnnotation=null;
        totalDigitsAnnotation=null;
        fractionDigitsAnnotation=null;
        patternAnnotations=null;
        enumAnnotations=null;
        maxInclusiveAnnotation=null;
        maxExclusiveAnnotation=null;
        minInclusiveAnnotation=null;
        minExclusiveAnnotation=null;
    }
}
