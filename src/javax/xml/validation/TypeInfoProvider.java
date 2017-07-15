/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.validation;

import org.w3c.dom.TypeInfo;

public abstract class TypeInfoProvider{
    protected TypeInfoProvider(){
    }

    public abstract TypeInfo getElementTypeInfo();

    public abstract TypeInfo getAttributeTypeInfo(int index);

    public abstract boolean isIdAttribute(int index);

    public abstract boolean isSpecified(int index);
}
