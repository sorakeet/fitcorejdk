/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2003-2005 The Apache Software Foundation.
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
 * Copyright 2003-2005 The Apache Software Foundation.
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

public interface XSConstants{
    // XML Schema Components
    public static final short ATTRIBUTE_DECLARATION=1;
    public static final short ELEMENT_DECLARATION=2;
    public static final short TYPE_DEFINITION=3;
    public static final short ATTRIBUTE_USE=4;
    public static final short ATTRIBUTE_GROUP=5;
    public static final short MODEL_GROUP_DEFINITION=6;
    public static final short MODEL_GROUP=7;
    public static final short PARTICLE=8;
    public static final short WILDCARD=9;
    public static final short IDENTITY_CONSTRAINT=10;
    public static final short NOTATION_DECLARATION=11;
    public static final short ANNOTATION=12;
    public static final short FACET=13;
    public static final short MULTIVALUE_FACET=14;
    // Derivation constants
    public static final short DERIVATION_NONE=0;
    public static final short DERIVATION_EXTENSION=1;
    public static final short DERIVATION_RESTRICTION=2;
    public static final short DERIVATION_SUBSTITUTION=4;
    public static final short DERIVATION_UNION=8;
    public static final short DERIVATION_LIST=16;
    public static final short DERIVATION_EXTENSION_RESTRICTION_SUBSTITION=
            XSConstants.DERIVATION_EXTENSION
                    |XSConstants.DERIVATION_RESTRICTION
                    |XSConstants.DERIVATION_SUBSTITUTION;
    public static final short DERIVATION_ALL=
            XSConstants.DERIVATION_SUBSTITUTION
                    |XSConstants.DERIVATION_EXTENSION
                    |XSConstants.DERIVATION_RESTRICTION
                    |XSConstants.DERIVATION_LIST
                    |XSConstants.DERIVATION_UNION;
    // Scope
    public static final short SCOPE_ABSENT=0;
    public static final short SCOPE_GLOBAL=1;
    public static final short SCOPE_LOCAL=2;
    // Value Constraint
    public static final short VC_NONE=0;
    public static final short VC_DEFAULT=1;
    public static final short VC_FIXED=2;
    // Built-in types: primitive and derived
    public static final short ANYSIMPLETYPE_DT=1;
    public static final short STRING_DT=2;
    public static final short BOOLEAN_DT=3;
    public static final short DECIMAL_DT=4;
    public static final short FLOAT_DT=5;
    public static final short DOUBLE_DT=6;
    public static final short DURATION_DT=7;
    public static final short DATETIME_DT=8;
    public static final short TIME_DT=9;
    public static final short DATE_DT=10;
    public static final short GYEARMONTH_DT=11;
    public static final short GYEAR_DT=12;
    public static final short GMONTHDAY_DT=13;
    public static final short GDAY_DT=14;
    public static final short GMONTH_DT=15;
    public static final short HEXBINARY_DT=16;
    public static final short BASE64BINARY_DT=17;
    public static final short ANYURI_DT=18;
    public static final short QNAME_DT=19;
    public static final short NOTATION_DT=20;
    public static final short NORMALIZEDSTRING_DT=21;
    public static final short TOKEN_DT=22;
    public static final short LANGUAGE_DT=23;
    public static final short NMTOKEN_DT=24;
    public static final short NAME_DT=25;
    public static final short NCNAME_DT=26;
    public static final short ID_DT=27;
    public static final short IDREF_DT=28;
    public static final short ENTITY_DT=29;
    public static final short INTEGER_DT=30;
    public static final short NONPOSITIVEINTEGER_DT=31;
    public static final short NEGATIVEINTEGER_DT=32;
    public static final short LONG_DT=33;
    public static final short INT_DT=34;
    public static final short SHORT_DT=35;
    public static final short BYTE_DT=36;
    public static final short NONNEGATIVEINTEGER_DT=37;
    public static final short UNSIGNEDLONG_DT=38;
    public static final short UNSIGNEDINT_DT=39;
    public static final short UNSIGNEDSHORT_DT=40;
    public static final short UNSIGNEDBYTE_DT=41;
    public static final short POSITIVEINTEGER_DT=42;
    public static final short LISTOFUNION_DT=43;
    public static final short LIST_DT=44;
    public static final short UNAVAILABLE_DT=45;
}
