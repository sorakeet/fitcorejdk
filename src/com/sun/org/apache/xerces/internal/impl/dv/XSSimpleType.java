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

import com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;

public interface XSSimpleType extends XSSimpleTypeDefinition{
    public static final short WS_PRESERVE=0;
    public static final short WS_REPLACE=1;
    public static final short WS_COLLAPSE=2;
    public static final short PRIMITIVE_STRING=1;
    public static final short PRIMITIVE_BOOLEAN=2;
    public static final short PRIMITIVE_DECIMAL=3;
    public static final short PRIMITIVE_FLOAT=4;
    public static final short PRIMITIVE_DOUBLE=5;
    public static final short PRIMITIVE_DURATION=6;
    public static final short PRIMITIVE_DATETIME=7;
    public static final short PRIMITIVE_TIME=8;
    public static final short PRIMITIVE_DATE=9;
    public static final short PRIMITIVE_GYEARMONTH=10;
    public static final short PRIMITIVE_GYEAR=11;
    public static final short PRIMITIVE_GMONTHDAY=12;
    public static final short PRIMITIVE_GDAY=13;
    public static final short PRIMITIVE_GMONTH=14;
    public static final short PRIMITIVE_HEXBINARY=15;
    public static final short PRIMITIVE_BASE64BINARY=16;
    public static final short PRIMITIVE_ANYURI=17;
    public static final short PRIMITIVE_QNAME=18;
    public static final short PRIMITIVE_PRECISIONDECIMAL=19;
    public static final short PRIMITIVE_NOTATION=20;

    public short getPrimitiveKind();

    public Object validate(String content,ValidationContext context,ValidatedInfo validatedInfo)
            throws InvalidDatatypeValueException;

    public Object validate(Object content,ValidationContext context,ValidatedInfo validatedInfo)
            throws InvalidDatatypeValueException;

    public void validate(ValidationContext context,ValidatedInfo validatedInfo)
            throws InvalidDatatypeValueException;

    public void applyFacets(XSFacets facets,short presentFacet,short fixedFacet,ValidationContext context)
            throws InvalidDatatypeFacetException;

    public boolean isEqual(Object value1,Object value2);
    //public short compare(Object value1, Object value2);

    public boolean isIDType();

    public short getWhitespace() throws DatatypeException;
}
