/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute;

public interface PrintJobAttributeSet extends AttributeSet{
    public boolean add(Attribute attribute);

    public boolean addAll(AttributeSet attributes);
}
