/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute;

import java.io.Serializable;

public interface Attribute extends Serializable{
    public Class<? extends Attribute> getCategory();

    public String getName();
}
