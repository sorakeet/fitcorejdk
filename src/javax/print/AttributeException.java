/**
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print;

import javax.print.attribute.Attribute;

public interface AttributeException{
    public Class[] getUnsupportedAttributes();

    public Attribute[] getUnsupportedValues();
}
